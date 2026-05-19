package com.elysia.mooc.teaching.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import com.elysia.mooc.auth.mapper.SysUserMapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.teaching.constants.TeachingConstants;
import com.elysia.mooc.teaching.constants.TeachingErrorCode;
import com.elysia.mooc.teaching.domain.dto.TeacherDashboardQuery;
import com.elysia.mooc.teaching.domain.dto.TeacherStudentProgressQuery;
import com.elysia.mooc.teaching.domain.po.TeacherCourseStatPO;
import com.elysia.mooc.teaching.domain.po.TeacherRevenueStatPO;
import com.elysia.mooc.teaching.domain.po.TeacherStudentProgressStatPO;
import com.elysia.mooc.teaching.domain.vo.TeacherCourseAnalysisVO;
import com.elysia.mooc.teaching.domain.vo.TeacherDashboardOverviewVO;
import com.elysia.mooc.teaching.domain.vo.TeacherStudentProgressVO;
import com.elysia.mooc.teaching.mapper.TeacherCourseStatMapper;
import com.elysia.mooc.teaching.mapper.TeacherRevenueStatMapper;
import com.elysia.mooc.teaching.mapper.TeacherStudentProgressStatMapper;
import com.elysia.mooc.teaching.service.TeachingDashboardService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** 教师数据看板服务实现。 */
@Service
@RequiredArgsConstructor
public class TeachingDashboardServiceImpl implements TeachingDashboardService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final SysUserMapper sysUserMapper;
    private final TeacherCourseStatMapper teacherCourseStatMapper;
    private final TeacherStudentProgressStatMapper teacherStudentProgressStatMapper;
    private final TeacherRevenueStatMapper teacherRevenueStatMapper;

    /**
     * 查询当前教师最近一段时间的看板总览。
     * @param query 日期范围查询条件；未传时默认最近7天
     * @return 教师看板总览
     */
    @Override
    public TeacherDashboardOverviewVO getOverview(TeacherDashboardQuery query) {
        LoginUser teacher = requireTeacher();
        DateRange range = resolveDateRange(query);
        List<CoursePO> courses = listTeacherCourses(teacher.getUserId());
        List<TeacherCourseStatPO> courseStats = listCourseStats(teacher.getUserId(), null, range);
        List<TeacherRevenueStatPO> revenueStats = listRevenueStats(teacher.getUserId(), null, range);
        List<TeacherStudentProgressStatPO> progressStats = listProgressStats(teacher.getUserId(), null);

        TeacherDashboardOverviewVO overview = new TeacherDashboardOverviewVO();
        overview.setStartDate(range.startDate());
        overview.setEndDate(range.endDate());
        overview.setCourseCount(courses.size());
        overview.setActiveStudentCount(countDistinctStudents(progressStats));
        overview.setAverageCompletionRate(averageCompletionRate(courseStats));
        overview.setPaidOrderCount(sumPaidOrderCount(revenueStats));
        overview.setIncomeAmount(sumIncomeAmount(revenueStats));
        overview.setRefundAmount(sumRefundAmount(revenueStats));
        overview.setPendingQuestionCount(0);
        overview.setPendingReviewCourseCount(countPendingReviewCourses(courses));
        overview.setHomeworkCount(0);
        return overview;
    }

    /**
     * 查询单门课程的访问、学习和收入聚合数据。
     * @param courseId 课程 ID
     * @param query 日期范围查询条件；未传时默认最近7天
     * @return 课程分析数据
     */
    @Override
    public TeacherCourseAnalysisVO getCourseAnalysis(Long courseId, TeacherDashboardQuery query) {
        LoginUser teacher = requireTeacher();
        DateRange range = resolveDateRange(query);
        CoursePO course = requireOwnedCourse(courseId, teacher.getUserId());
        List<TeacherCourseStatPO> courseStats = listCourseStats(teacher.getUserId(), course.getId(), range);
        List<TeacherRevenueStatPO> revenueStats = listRevenueStats(teacher.getUserId(), course.getId(), range);

        TeacherCourseAnalysisVO analysis = new TeacherCourseAnalysisVO();
        analysis.setStartDate(range.startDate());
        analysis.setEndDate(range.endDate());
        analysis.setCourseId(course.getId());
        analysis.setCourseName(course.getTitle());
        analysis.setViewCount(sumViewCount(courseStats));
        analysis.setLearnCount(sumLearnCount(courseStats));
        analysis.setCompletionRate(averageCompletionRate(courseStats));
        analysis.setPaidOrderCount(sumPaidOrderCount(revenueStats));
        analysis.setIncomeAmount(sumIncomeAmount(revenueStats));
        analysis.setRefundAmount(sumRefundAmount(revenueStats));
        analysis.setAverageLearningMinutes(0);
        analysis.setInteractionCount(0);
        analysis.setNote(TeachingConstants.STAT_NOTE);
        return analysis;
    }

    /**
     * 分页查询教师名下课程的学员进度。
     * @param courseId 课程 ID
     * @param query 分页和风险筛选条件
     * @return 学员进度分页结果
     */
    @Override
    public PageResult<TeacherStudentProgressVO> listCourseStudents(Long courseId, TeacherStudentProgressQuery query) {
        LoginUser teacher = requireTeacher();
        CoursePO course = requireOwnedCourse(courseId, teacher.getUserId());
        TeacherStudentProgressQuery safeQuery = query == null ? new TeacherStudentProgressQuery() : query;

        Page<TeacherStudentProgressStatPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        var wrapper = Wrappers.<TeacherStudentProgressStatPO>lambdaQuery()
                .eq(TeacherStudentProgressStatPO::getTeacherId, teacher.getUserId())
                .eq(TeacherStudentProgressStatPO::getCourseId, course.getId());
        if (safeQuery.getRiskLevel() != null) {
            wrapper.eq(TeacherStudentProgressStatPO::getRiskLevel, safeQuery.getRiskLevel());
        }
        wrapper.orderByAsc(TeacherStudentProgressStatPO::getRiskLevel)
                .orderByDesc(TeacherStudentProgressStatPO::getLastLearnTime)
                .orderByDesc(TeacherStudentProgressStatPO::getId);

        Page<TeacherStudentProgressStatPO> result = teacherStudentProgressStatMapper.selectPage(page, wrapper);
        Map<Long, SysUserPO> userMap = mapUsers(result.getRecords());
        return PageResult.of(result, record -> toStudentProgressVO(record, userMap));
    }

    private LoginUser requireTeacher() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(TeachingConstants.ROLE_TEACHER::equalsIgnoreCase)) {
            return loginUser;
        }
        throw new BizException(TeachingErrorCode.TEACHING_FORBIDDEN);
    }

    private CoursePO requireOwnedCourse(Long courseId, Long teacherId) {
        CoursePO course = courseId == null ? null : courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(TeachingErrorCode.TEACHING_COURSE_NOT_FOUND);
        }
        if (!Objects.equals(course.getTeacherId(), teacherId)) {
            throw new BizException(TeachingErrorCode.TEACHING_COURSE_FORBIDDEN);
        }
        return course;
    }

    private DateRange resolveDateRange(TeacherDashboardQuery query) {
        TeacherDashboardQuery safeQuery = query == null ? new TeacherDashboardQuery() : query;
        LocalDate endDate = safeQuery.getEndDate() == null ? LocalDate.now() : safeQuery.getEndDate();
        LocalDate startDate = safeQuery.getStartDate() == null
                ? endDate.minusDays(TeachingConstants.DEFAULT_STAT_DAYS - 1L)
                : safeQuery.getStartDate();
        if (startDate.isAfter(endDate)) {
            throw new BizException(TeachingErrorCode.TEACHING_DATE_RANGE_INVALID);
        }
        return new DateRange(startDate, endDate);
    }

    private List<CoursePO> listTeacherCourses(Long teacherId) {
        List<CoursePO> courses = courseMapper.selectList(Wrappers.<CoursePO>lambdaQuery()
                .eq(CoursePO::getTeacherId, teacherId));
        return courses == null ? Collections.emptyList() : courses;
    }

    private List<TeacherCourseStatPO> listCourseStats(Long teacherId, Long courseId, DateRange range) {
        var wrapper = Wrappers.<TeacherCourseStatPO>lambdaQuery()
                .eq(TeacherCourseStatPO::getTeacherId, teacherId)
                .ge(TeacherCourseStatPO::getStatDate, range.startDate())
                .le(TeacherCourseStatPO::getStatDate, range.endDate());
        if (courseId != null) {
            wrapper.eq(TeacherCourseStatPO::getCourseId, courseId);
        }
        return safeList(teacherCourseStatMapper.selectList(wrapper));
    }

    private List<TeacherRevenueStatPO> listRevenueStats(Long teacherId, Long courseId, DateRange range) {
        var wrapper = Wrappers.<TeacherRevenueStatPO>lambdaQuery()
                .eq(TeacherRevenueStatPO::getTeacherId, teacherId)
                .ge(TeacherRevenueStatPO::getStatDate, range.startDate())
                .le(TeacherRevenueStatPO::getStatDate, range.endDate());
        if (courseId != null) {
            wrapper.eq(TeacherRevenueStatPO::getCourseId, courseId);
        }
        return safeList(teacherRevenueStatMapper.selectList(wrapper));
    }

    private List<TeacherStudentProgressStatPO> listProgressStats(Long teacherId, Long courseId) {
        var wrapper = Wrappers.<TeacherStudentProgressStatPO>lambdaQuery()
                .eq(TeacherStudentProgressStatPO::getTeacherId, teacherId);
        if (courseId != null) {
            wrapper.eq(TeacherStudentProgressStatPO::getCourseId, courseId);
        }
        return safeList(teacherStudentProgressStatMapper.selectList(wrapper));
    }

    private Map<Long, SysUserPO> mapUsers(List<TeacherStudentProgressStatPO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = records.stream()
                .map(TeacherStudentProgressStatPO::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysUserPO> users = sysUserMapper.selectBatchIds(userIds);
        return users == null
                ? Collections.emptyMap()
                : users.stream().collect(Collectors.toMap(SysUserPO::getId, Function.identity(), (left, right) -> left));
    }

    private TeacherStudentProgressVO toStudentProgressVO(
            TeacherStudentProgressStatPO progress,
            Map<Long, SysUserPO> userMap) {
        return BeanCopyUtils.copyBean(progress, TeacherStudentProgressVO.class, (source, target) -> {
            SysUserPO user = userMap.get(source.getStudentId());
            target.setStudentName(resolveStudentName(user));
            target.setLatestLearnTime(source.getLastLearnTime());
            target.setRiskLevelDesc(source.getRiskLevel() == null ? null : source.getRiskLevel().getDesc());
            target.setCompletedSectionCount(null);
        });
    }

    private String resolveStudentName(SysUserPO user) {
        if (user == null) {
            return TeachingConstants.UNKNOWN_STUDENT_NAME;
        }
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getUsername() == null ? TeachingConstants.UNKNOWN_STUDENT_NAME : user.getUsername();
    }

    private int countPendingReviewCourses(List<CoursePO> courses) {
        return (int) courses.stream()
                .filter(course -> course.getStatus() == CourseStatus.PENDING)
                .count();
    }

    private int countDistinctStudents(List<TeacherStudentProgressStatPO> progressStats) {
        return (int) progressStats.stream()
                .map(TeacherStudentProgressStatPO::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private int sumViewCount(List<TeacherCourseStatPO> stats) {
        return stats.stream().map(TeacherCourseStatPO::getViewCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

    private int sumLearnCount(List<TeacherCourseStatPO> stats) {
        return stats.stream().map(TeacherCourseStatPO::getLearnCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

    private int sumPaidOrderCount(List<TeacherRevenueStatPO> stats) {
        return stats.stream().map(TeacherRevenueStatPO::getPaidOrderCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

    private BigDecimal sumIncomeAmount(List<TeacherRevenueStatPO> stats) {
        return stats.stream()
                .map(TeacherRevenueStatPO::getIncomeAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumRefundAmount(List<TeacherRevenueStatPO> stats) {
        return stats.stream()
                .map(TeacherRevenueStatPO::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal averageCompletionRate(List<TeacherCourseStatPO> stats) {
        List<BigDecimal> rates = stats.stream()
                .map(TeacherCourseStatPO::getCompletionRate)
                .filter(Objects::nonNull)
                .toList();
        if (rates.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(rates.size()), 2, RoundingMode.HALF_UP);
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
