package com.elysia.mooc.course.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.constants.CourseConstants;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.dto.CourseAuditLogQuery;
import com.elysia.mooc.course.domain.dto.OfflineCourseRequest;
import com.elysia.mooc.course.domain.dto.RejectCourseRequest;
import com.elysia.mooc.course.domain.dto.SubmitCourseAuditRequest;
import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseDifficulty;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CourseAuditLogPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.vo.CourseAuditLogVO;
import com.elysia.mooc.course.mapper.CourseAuditLogMapper;
import com.elysia.mooc.course.mapper.CourseChapterMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 课程审核状态机和日志测试。 */
@ExtendWith(MockitoExtension.class)
class CourseAuditServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseChapterMapper courseChapterMapper;

    @Mock
    private CourseSectionMapper courseSectionMapper;

    @Mock
    private CourseAuditLogMapper courseAuditLogMapper;

    @InjectMocks
    private CourseAuditServiceImpl courseAuditService;

    @Test
    void submitAuditShouldMoveDraftToPendingAndWriteLog() {
        when(userContextService.currentLoginUser()).thenReturn(teacher(2L));
        when(courseMapper.selectById(3006L)).thenReturn(course(3006L, 2L, CourseStatus.DRAFT));
        when(courseChapterMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(courseSectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        SubmitCourseAuditRequest request = new SubmitCourseAuditRequest();
        request.setRemark("  提交审核  ");

        courseAuditService.submitAudit(3006L, request);

        ArgumentCaptor<CoursePO> courseCaptor = ArgumentCaptor.forClass(CoursePO.class);
        verify(courseMapper).updateById(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getStatus()).isEqualTo(CourseStatus.PENDING);

        ArgumentCaptor<CourseAuditLogPO> logCaptor = ArgumentCaptor.forClass(CourseAuditLogPO.class);
        verify(courseAuditLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getBeforeStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(logCaptor.getValue().getAfterStatus()).isEqualTo(CourseStatus.PENDING);
        assertThat(logCaptor.getValue().getAuditAction()).isEqualTo(CourseAuditAction.SUBMIT);
        assertThat(logCaptor.getValue().getAuditComment()).isEqualTo("提交审核");
    }

    @Test
    void submitAuditShouldRejectOtherTeacherCourse() {
        when(userContextService.currentLoginUser()).thenReturn(teacher(2L));
        when(courseMapper.selectById(3006L)).thenReturn(course(3006L, 9L, CourseStatus.DRAFT));

        assertThatThrownBy(() -> courseAuditService.submitAudit(3006L, null))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(CourseErrorCode.COURSE_FORBIDDEN.code());
    }

    @Test
    void submitAuditShouldRejectPublishedCourse() {
        when(userContextService.currentLoginUser()).thenReturn(teacher(2L));
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, CourseStatus.PUBLISHED));

        assertThatThrownBy(() -> courseAuditService.submitAudit(3001L, null))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(CourseErrorCode.COURSE_STATUS_INVALID.code());
    }

    @Test
    void approveCourseShouldPublishAndSetPublishTime() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(courseMapper.selectById(3003L)).thenReturn(course(3003L, 2L, CourseStatus.PENDING));
        when(courseChapterMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(courseSectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        courseAuditService.approveCourse(3003L, null);

        ArgumentCaptor<CoursePO> courseCaptor = ArgumentCaptor.forClass(CoursePO.class);
        verify(courseMapper).updateById(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(courseCaptor.getValue().getPublishTime()).isNotNull();

        ArgumentCaptor<CourseAuditLogPO> logCaptor = ArgumentCaptor.forClass(CourseAuditLogPO.class);
        verify(courseAuditLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getAuditAction()).isEqualTo(CourseAuditAction.APPROVE);
    }

    @Test
    void approveCourseShouldRejectIncompleteCourse() {
        CoursePO course = course(3003L, 2L, CourseStatus.PENDING);
        course.setSummary(null);
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(courseMapper.selectById(3003L)).thenReturn(course);

        assertThatThrownBy(() -> courseAuditService.approveCourse(3003L, null))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(CourseErrorCode.COURSE_CONTENT_INCOMPLETE.code());
    }

    @Test
    void rejectCourseShouldMovePendingToRejected() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(courseMapper.selectById(3003L)).thenReturn(course(3003L, 2L, CourseStatus.PENDING));
        RejectCourseRequest request = new RejectCourseRequest();
        request.setReason("内容不完整");

        courseAuditService.rejectCourse(3003L, request);

        ArgumentCaptor<CoursePO> courseCaptor = ArgumentCaptor.forClass(CoursePO.class);
        verify(courseMapper).updateById(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getStatus()).isEqualTo(CourseStatus.REJECTED);
    }

    @Test
    void offlineCourseShouldKeepPublishTime() {
        LocalDateTime publishTime = LocalDateTime.now().minusDays(1);
        CoursePO course = course(3001L, 2L, CourseStatus.PUBLISHED);
        course.setPublishTime(publishTime);
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(courseMapper.selectById(3001L)).thenReturn(course);
        OfflineCourseRequest request = new OfflineCourseRequest();
        request.setReason("内容过期");

        courseAuditService.offlineCourse(3001L, request);

        ArgumentCaptor<CoursePO> courseCaptor = ArgumentCaptor.forClass(CoursePO.class);
        verify(courseMapper).updateById(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getStatus()).isEqualTo(CourseStatus.OFFLINE);
        assertThat(courseCaptor.getValue().getPublishTime()).isEqualTo(publishTime);
    }

    @Test
    void listAuditLogsShouldReturnPageResult() {
        when(userContextService.currentLoginUser()).thenReturn(teacher(2L));
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, CourseStatus.PUBLISHED));
        Page<CourseAuditLogPO> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(log(7001L, 3001L)));
        when(courseAuditLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        CourseAuditLogQuery query = new CourseAuditLogQuery();
        query.setPageNo(1);
        query.setPageSize(10);

        PageResult<CourseAuditLogVO> result = courseAuditService.listAuditLogs(3001L, query);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getAuditAction()).isEqualTo(CourseAuditAction.SUBMIT);
    }

    private LoginUser teacher(Long userId) {
        return new LoginUser(
                userId,
                "teacher",
                List.of(CourseConstants.ROLE_TEACHER),
                List.of(CourseConstants.PERMISSION_COURSE_PUBLISH));
    }

    private LoginUser admin() {
        return new LoginUser(1L, "admin", List.of(CourseConstants.ROLE_ADMIN), List.of());
    }

    private CoursePO course(Long id, Long teacherId, CourseStatus status) {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setTitle("测试课程");
        course.setSummary("测试简介");
        course.setCategoryId(1002L);
        course.setTeacherId(teacherId);
        course.setDifficulty(CourseDifficulty.BEGINNER);
        course.setStatus(status);
        return course;
    }

    private CourseAuditLogPO log(Long id, Long courseId) {
        CourseAuditLogPO log = new CourseAuditLogPO();
        log.setId(id);
        log.setCourseId(courseId);
        log.setBeforeStatus(CourseStatus.DRAFT);
        log.setAfterStatus(CourseStatus.PENDING);
        log.setAuditorId(2L);
        log.setAuditComment("提交审核");
        log.setAuditAction(CourseAuditAction.SUBMIT);
        return log;
    }
}
