package com.elysia.mooc.teaching.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import com.elysia.mooc.auth.mapper.SysUserMapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.teaching.constants.TeachingErrorCode;
import com.elysia.mooc.teaching.domain.dto.TeacherDashboardQuery;
import com.elysia.mooc.teaching.domain.dto.TeacherStudentProgressQuery;
import com.elysia.mooc.teaching.domain.enums.TeacherStudentRiskLevel;
import com.elysia.mooc.teaching.domain.po.TeacherCourseStatPO;
import com.elysia.mooc.teaching.domain.po.TeacherRevenueStatPO;
import com.elysia.mooc.teaching.domain.po.TeacherStudentProgressStatPO;
import com.elysia.mooc.teaching.domain.vo.TeacherCourseAnalysisVO;
import com.elysia.mooc.teaching.domain.vo.TeacherDashboardOverviewVO;
import com.elysia.mooc.teaching.domain.vo.TeacherStudentProgressVO;
import com.elysia.mooc.teaching.mapper.TeacherCourseStatMapper;
import com.elysia.mooc.teaching.mapper.TeacherRevenueStatMapper;
import com.elysia.mooc.teaching.mapper.TeacherStudentProgressStatMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 教师看板服务单元测试。 */
@ExtendWith(MockitoExtension.class)
class TeachingDashboardServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private TeacherCourseStatMapper teacherCourseStatMapper;

    @Mock
    private TeacherStudentProgressStatMapper teacherStudentProgressStatMapper;

    @Mock
    private TeacherRevenueStatMapper teacherRevenueStatMapper;

    @InjectMocks
    private TeachingDashboardServiceImpl teachingDashboardService;

    @Test
    void overviewShouldAggregateTeacherMetricsInDateRange() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(courseMapper.selectList(any())).thenReturn(List.of(
                course(3001L, 2L, "Java 入门", CourseStatus.PUBLISHED),
                course(3002L, 2L, "AI 工程", CourseStatus.PENDING)));
        when(teacherCourseStatMapper.selectList(any())).thenReturn(List.of(
                courseStat(3001L, 120, 36, "40.00"),
                courseStat(3002L, 80, 18, "20.00")));
        when(teacherRevenueStatMapper.selectList(any())).thenReturn(List.of(
                revenueStat(3001L, 2, "298.00", "0.00"),
                revenueStat(3002L, 1, "199.00", "10.00")));
        when(teacherStudentProgressStatMapper.selectList(any())).thenReturn(List.of(
                progress(3001L, 4L, "56.00", TeacherStudentRiskLevel.NORMAL),
                progress(3002L, 4L, "18.00", TeacherStudentRiskLevel.RISK),
                progress(3002L, 5L, "75.00", TeacherStudentRiskLevel.ATTENTION)));

        TeacherDashboardOverviewVO result = teachingDashboardService.getOverview(new TeacherDashboardQuery());

        assertThat(result.getCourseCount()).isEqualTo(2);
        assertThat(result.getActiveStudentCount()).isEqualTo(2);
        assertThat(result.getAverageCompletionRate()).isEqualByComparingTo("30.00");
        assertThat(result.getPaidOrderCount()).isEqualTo(3);
        assertThat(result.getIncomeAmount()).isEqualByComparingTo("497.00");
        assertThat(result.getRefundAmount()).isEqualByComparingTo("10.00");
        assertThat(result.getPendingReviewCourseCount()).isEqualTo(1);
        assertThat(result.getStartDate()).isEqualTo(LocalDate.now().minusDays(6));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void courseAnalysisShouldRejectOtherTeacherCourse() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 9L, "他人课程", CourseStatus.PUBLISHED));

        assertThatThrownBy(() -> teachingDashboardService.getCourseAnalysis(3001L, new TeacherDashboardQuery()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(TeachingErrorCode.TEACHING_COURSE_FORBIDDEN.code());
    }

    @Test
    void courseAnalysisShouldAggregateCourseAndRevenueStats() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, "Java 入门", CourseStatus.PUBLISHED));
        when(teacherCourseStatMapper.selectList(any())).thenReturn(List.of(
                courseStat(3001L, 120, 36, "42.50"),
                courseStat(3001L, 80, 20, "57.50")));
        when(teacherRevenueStatMapper.selectList(any())).thenReturn(List.of(
                revenueStat(3001L, 2, "298.00", "0.00"),
                revenueStat(3001L, 1, "199.00", "20.00")));

        TeacherDashboardQuery query = new TeacherDashboardQuery();
        query.setStartDate(LocalDate.of(2026, 5, 12));
        query.setEndDate(LocalDate.of(2026, 5, 18));

        TeacherCourseAnalysisVO result = teachingDashboardService.getCourseAnalysis(3001L, query);

        assertThat(result.getCourseId()).isEqualTo(3001L);
        assertThat(result.getCourseName()).isEqualTo("Java 入门");
        assertThat(result.getViewCount()).isEqualTo(200);
        assertThat(result.getLearnCount()).isEqualTo(56);
        assertThat(result.getCompletionRate()).isEqualByComparingTo("50.00");
        assertThat(result.getPaidOrderCount()).isEqualTo(3);
        assertThat(result.getIncomeAmount()).isEqualByComparingTo("497.00");
        assertThat(result.getRefundAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void listCourseStudentsShouldReturnPageResultAndStudentName() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, "Java 入门", CourseStatus.PUBLISHED));
        Page<TeacherStudentProgressStatPO> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(progress(3001L, 4L, "56.00", TeacherStudentRiskLevel.RISK)));
        when(teacherStudentProgressStatMapper.selectPage(any(), any())).thenReturn(page);
        when(sysUserMapper.selectBatchIds(any(Collection.class))).thenReturn(List.of(user(4L, "student", "学生")));

        TeacherStudentProgressQuery query = new TeacherStudentProgressQuery();
        query.setRiskLevel(TeacherStudentRiskLevel.RISK);

        PageResult<TeacherStudentProgressVO> result = teachingDashboardService.listCourseStudents(3001L, query);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getStudentName()).isEqualTo("学生");
        assertThat(result.getList().get(0).getRiskLevel()).isEqualTo(TeacherStudentRiskLevel.RISK);
        assertThat(result.getList().get(0).getRiskLevelDesc()).isEqualTo("高风险");
        assertThat(result.getList().get(0).getLatestLearnTime()).isNotNull();
        verify(teacherStudentProgressStatMapper).selectPage(any(), any());
    }

    @Test
    void studentRoleShouldNotAccessTeachingDashboardService() {
        when(userContextService.currentLoginUser()).thenReturn(new LoginUser(4L, "student", List.of("STUDENT"), List.of()));

        assertThatThrownBy(() -> teachingDashboardService.getOverview(new TeacherDashboardQuery()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(TeachingErrorCode.TEACHING_FORBIDDEN.code());
    }

    @Test
    void invalidDateRangeShouldReturnBusinessError() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        TeacherDashboardQuery query = new TeacherDashboardQuery();
        query.setStartDate(LocalDate.of(2026, 5, 19));
        query.setEndDate(LocalDate.of(2026, 5, 18));

        assertThatThrownBy(() -> teachingDashboardService.getOverview(query))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(TeachingErrorCode.TEACHING_DATE_RANGE_INVALID.code());
    }

    private LoginUser teacher() {
        return new LoginUser(2L, "teacher", List.of("TEACHER"), List.of());
    }

    private CoursePO course(Long id, Long teacherId, String title, CourseStatus status) {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setTeacherId(teacherId);
        course.setTitle(title);
        course.setStatus(status);
        return course;
    }

    private TeacherCourseStatPO courseStat(Long courseId, Integer viewCount, Integer learnCount, String completionRate) {
        TeacherCourseStatPO stat = new TeacherCourseStatPO();
        stat.setTeacherId(2L);
        stat.setCourseId(courseId);
        stat.setStatDate(LocalDate.now());
        stat.setViewCount(viewCount);
        stat.setLearnCount(learnCount);
        stat.setCompletionRate(new BigDecimal(completionRate));
        return stat;
    }

    private TeacherRevenueStatPO revenueStat(Long courseId, Integer paidOrderCount, String incomeAmount, String refundAmount) {
        TeacherRevenueStatPO stat = new TeacherRevenueStatPO();
        stat.setTeacherId(2L);
        stat.setCourseId(courseId);
        stat.setStatDate(LocalDate.now());
        stat.setPaidOrderCount(paidOrderCount);
        stat.setIncomeAmount(new BigDecimal(incomeAmount));
        stat.setRefundAmount(new BigDecimal(refundAmount));
        return stat;
    }

    private TeacherStudentProgressStatPO progress(
            Long courseId,
            Long studentId,
            String progressPercent,
            TeacherStudentRiskLevel riskLevel) {
        TeacherStudentProgressStatPO progress = new TeacherStudentProgressStatPO();
        progress.setId(studentId);
        progress.setTeacherId(2L);
        progress.setCourseId(courseId);
        progress.setStudentId(studentId);
        progress.setProgressPercent(new BigDecimal(progressPercent));
        progress.setLastLearnTime(LocalDateTime.now());
        progress.setRiskLevel(riskLevel);
        return progress;
    }

    private SysUserPO user(Long id, String username, String nickname) {
        SysUserPO user = new SysUserPO();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(nickname);
        return user;
    }
}
