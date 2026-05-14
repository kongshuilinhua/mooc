package com.elysia.mooc.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.constants.CourseConstants;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.dto.AuditCourseRequest;
import com.elysia.mooc.course.domain.dto.CourseAuditLogQuery;
import com.elysia.mooc.course.domain.dto.OfflineCourseRequest;
import com.elysia.mooc.course.domain.dto.RejectCourseRequest;
import com.elysia.mooc.course.domain.dto.SubmitCourseAuditRequest;
import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CourseAuditLogPO;
import com.elysia.mooc.course.domain.po.CourseChapterPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.domain.vo.CourseAuditLogVO;
import com.elysia.mooc.course.mapper.CourseAuditLogMapper;
import com.elysia.mooc.course.mapper.CourseChapterMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.course.service.CourseAuditService;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 课程审核发布服务实现。 */
@Service
@RequiredArgsConstructor
public class CourseAuditServiceImpl implements CourseAuditService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseSectionMapper courseSectionMapper;
    private final CourseAuditLogMapper courseAuditLogMapper;

    /**
     * 提交课程审核。
     *
     * @param courseId 课程 ID
     * @param request 提交审核请求，可为空
     * @return 操作成功返回 true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitAudit(Long courseId, SubmitCourseAuditRequest request) {
        LoginUser loginUser = userContextService.currentLoginUser();
        CoursePO course = getCourse(courseId);
        requireTeacherOwnerOrAdmin(loginUser, course);
        requireStatus(course, CourseStatus.DRAFT, CourseStatus.REJECTED, CourseStatus.OFFLINE);
        validatePublishReady(course);
        changeStatus(course, CourseStatus.PENDING, loginUser.getUserId(),
                CourseAuditAction.SUBMIT, request == null ? null : request.getRemark());
        return true;
    }

    /**
     * 审核通过课程。
     *
     * @param courseId 课程 ID
     * @param request 审核通过请求，可为空
     * @return 操作成功返回 true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveCourse(Long courseId, AuditCourseRequest request) {
        LoginUser loginUser = requireAdmin();
        CoursePO course = getCourse(courseId);
        requireStatus(course, CourseStatus.PENDING);
        validatePublishReady(course);
        course.setPublishTime(LocalDateTime.now());
        changeStatus(course, CourseStatus.PUBLISHED, loginUser.getUserId(),
                CourseAuditAction.APPROVE, request == null ? null : request.getRemark());
        return true;
    }

    /**
     * 驳回课程审核。
     *
     * @param courseId 课程 ID
     * @param request 驳回请求
     * @return 操作成功返回 true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectCourse(Long courseId, RejectCourseRequest request) {
        LoginUser loginUser = requireAdmin();
        CoursePO course = getCourse(courseId);
        requireStatus(course, CourseStatus.PENDING);
        changeStatus(course, CourseStatus.REJECTED, loginUser.getUserId(),
                CourseAuditAction.REJECT, request.getReason());
        return true;
    }

    /**
     * 下架已发布课程。
     *
     * @param courseId 课程 ID
     * @param request 下架请求
     * @return 操作成功返回 true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean offlineCourse(Long courseId, OfflineCourseRequest request) {
        LoginUser loginUser = requireAdmin();
        CoursePO course = getCourse(courseId);
        requireStatus(course, CourseStatus.PUBLISHED);
        changeStatus(course, CourseStatus.OFFLINE, loginUser.getUserId(),
                CourseAuditAction.OFFLINE, request.getReason());
        return true;
    }

    /**
     * 分页查询课程审核日志。
     *
     * @param courseId 课程 ID
     * @param query 查询参数
     * @return 审核日志分页
     */
    @Override
    public PageResult<CourseAuditLogVO> listAuditLogs(Long courseId, CourseAuditLogQuery query) {
        LoginUser loginUser = userContextService.currentLoginUser();
        CoursePO course = getCourse(courseId);
        requireTeacherOwnerOrAdmin(loginUser, course);
        CourseAuditLogQuery safeQuery = query == null ? new CourseAuditLogQuery() : query;

        // 审核日志属于课程状态轨迹，教师只能看自己课程，管理员可看全部；查询条件只在日志表内收敛。
        LambdaQueryWrapper<CourseAuditLogPO> wrapper = Wrappers.<CourseAuditLogPO>lambdaQuery()
                .eq(CourseAuditLogPO::getCourseId, courseId);
        if (safeQuery.getStatus() != null) {
            wrapper.eq(CourseAuditLogPO::getAfterStatus, safeQuery.getStatus());
        }
        if (safeQuery.getAuditAction() != null) {
            wrapper.eq(CourseAuditLogPO::getAuditAction, safeQuery.getAuditAction());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(CourseAuditLogPO::getAuditComment, safeQuery.getKeyword().trim());
        }
        wrapper.orderByDesc(CourseAuditLogPO::getCreateTime).orderByDesc(CourseAuditLogPO::getId);
        Page<CourseAuditLogPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return PageResult.of(courseAuditLogMapper.selectPage(page, wrapper), this::toLogVO);
    }

    /**
     * 读取课程并统一处理非法 ID 和不存在场景。
     *
     * @param courseId 课程 ID
     * @return 课程实体
     */
    private CoursePO getCourse(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BizException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }

    /**
     * 校验课程当前状态是否在允许集合中。
     *
     * @param course 课程实体
     * @param allowedStatuses 允许状态
     */
    private void requireStatus(CoursePO course, CourseStatus... allowedStatuses) {
        for (CourseStatus allowedStatus : allowedStatuses) {
            if (course.getStatus() == allowedStatus) {
                return;
            }
        }
        throw new BizException(CourseErrorCode.COURSE_STATUS_INVALID, "当前课程状态不允许执行该审核操作");
    }

    /**
     * 校验课程是否具备提交审核或发布条件。
     *
     * @param course 课程实体
     */
    private void validatePublishReady(CoursePO course) {
        // 1. 基础资料必须完整，否则即使审核通过，学生端课程详情也无法正常展示。
        if (!StringUtils.hasText(course.getTitle())
                || !StringUtils.hasText(course.getSummary())
                || course.getCategoryId() == null
                || course.getDifficulty() == null) {
            throw new BizException(CourseErrorCode.COURSE_CONTENT_INCOMPLETE);
        }

        // 2. 发布前至少要有章节和小节，避免出现空课程被管理员误发布。
        Long chapterCount = courseChapterMapper.selectCount(Wrappers.<CourseChapterPO>lambdaQuery()
                .eq(CourseChapterPO::getCourseId, course.getId()));
        if (chapterCount == null || chapterCount <= 0) {
            throw new BizException(CourseErrorCode.COURSE_CONTENT_INCOMPLETE, "课程至少需要一个章节才能提交审核");
        }
        Long sectionCount = courseSectionMapper.selectCount(Wrappers.<CourseSectionPO>lambdaQuery()
                .eq(CourseSectionPO::getCourseId, course.getId()));
        if (sectionCount == null || sectionCount <= 0) {
            throw new BizException(CourseErrorCode.COURSE_CONTENT_INCOMPLETE, "课程至少需要一个小节才能提交审核");
        }
    }

    /**
     * 修改课程状态并写入审核日志。
     *
     * @param course 课程实体
     * @param targetStatus 目标状态
     * @param operatorId 操作人 ID
     * @param action 审核动作
     * @param comment 审核意见
     */
    private void changeStatus(
            CoursePO course,
            CourseStatus targetStatus,
            Long operatorId,
            CourseAuditAction action,
            String comment) {
        // 状态变更和日志必须在同一事务内完成，避免课程状态变化后缺少审计轨迹。
        CourseStatus beforeStatus = course.getStatus();
        course.setStatus(targetStatus);
        courseMapper.updateById(course);
        CourseAuditLogPO log = new CourseAuditLogPO();
        log.setCourseId(course.getId());
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(targetStatus);
        log.setAuditorId(operatorId);
        log.setAuditComment(normalizeComment(comment));
        log.setAuditAction(action);
        log.setDeleted(0);
        courseAuditLogMapper.insert(log);
    }

    /**
     * 校验当前用户是否为课程讲师本人或管理员。
     *
     * @param loginUser 当前登录用户
     * @param course 课程实体
     */
    private void requireTeacherOwnerOrAdmin(LoginUser loginUser, CoursePO course) {
        if (isAdmin(loginUser)) {
            return;
        }
        if (hasRole(loginUser, CourseConstants.ROLE_TEACHER)
                && Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            return;
        }
        throw new BizException(CourseErrorCode.COURSE_FORBIDDEN);
    }

    /**
     * 获取并校验管理员身份。
     *
     * @return 当前管理员
     */
    private LoginUser requireAdmin() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (!isAdmin(loginUser)) {
            throw new BizException(CourseErrorCode.COURSE_FORBIDDEN);
        }
        return loginUser;
    }

    private CourseAuditLogVO toLogVO(CourseAuditLogPO log) {
        return BeanCopyUtils.copyBean(log, CourseAuditLogVO.class);
    }

    private String normalizeComment(String comment) {
        return StringUtils.hasText(comment) ? comment.trim() : null;
    }

    private boolean isAdmin(LoginUser loginUser) {
        return hasRole(loginUser, CourseConstants.ROLE_ADMIN);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        return loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(role -> roleCode.equalsIgnoreCase(role));
    }
}
