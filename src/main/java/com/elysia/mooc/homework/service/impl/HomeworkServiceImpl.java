package com.elysia.mooc.homework.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.domain.po.CourseChapterPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseChapterMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.homework.constants.HomeworkConstants;
import com.elysia.mooc.homework.constants.HomeworkErrorCode;
import com.elysia.mooc.homework.domain.dto.GradeHomeworkRequest;
import com.elysia.mooc.homework.domain.dto.PublishHomeworkRequest;
import com.elysia.mooc.homework.domain.dto.SubmitHomeworkRequest;
import com.elysia.mooc.homework.domain.enums.HomeworkAssignmentStatus;
import com.elysia.mooc.homework.domain.enums.HomeworkGradeStatus;
import com.elysia.mooc.homework.domain.po.HomeworkAssignmentPO;
import com.elysia.mooc.homework.domain.po.HomeworkGradeRecordPO;
import com.elysia.mooc.homework.domain.po.HomeworkSubmissionPO;
import com.elysia.mooc.homework.domain.vo.HomeworkAssignmentVO;
import com.elysia.mooc.homework.domain.vo.HomeworkGradeVO;
import com.elysia.mooc.homework.domain.vo.HomeworkSubmissionVO;
import com.elysia.mooc.homework.mapper.HomeworkAssignmentMapper;
import com.elysia.mooc.homework.mapper.HomeworkGradeRecordMapper;
import com.elysia.mooc.homework.mapper.HomeworkSubmissionMapper;
import com.elysia.mooc.homework.service.HomeworkService;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.message.service.MessageCommandService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/** 作业发布、提交与批改服务实现。 */
@Service
@RequiredArgsConstructor
public class HomeworkServiceImpl implements HomeworkService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final LearningCourseMapper learningCourseMapper;
    private final HomeworkAssignmentMapper assignmentMapper;
    private final HomeworkSubmissionMapper submissionMapper;
    private final HomeworkGradeRecordMapper gradeRecordMapper;
    private final MessageCommandService messageCommandService;
    private final ObjectMapper objectMapper;

    /**
     * 教师发布作业。
     *
     * @param request 发布作业请求
     * @return 作业响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeworkAssignmentVO publishAssignment(PublishHomeworkRequest request) {
        LoginUser loginUser = userContextService.currentLoginUser();
        CoursePO course = requireCourse(request.getCourseId());
        assertTeacherCanMaintain(loginUser, course);
        validateChapter(request.getChapterId(), course.getId());

        HomeworkAssignmentPO assignment = BeanCopyUtils.copyBean(request, HomeworkAssignmentPO.class, (source, target) -> {
            target.setStatus(source.getStatus() == null ? HomeworkAssignmentStatus.PUBLISHED : source.getStatus());
            target.setAllowResubmit(Boolean.TRUE.equals(source.getAllowResubmit()) ? 1 : 0);
            target.setDeleted(0);
        });
        assignmentMapper.insert(assignment);
        return toAssignmentVO(assignment);
    }

    /**
     * 学生提交作业。
     *
     * @param request 提交作业请求
     * @return 提交记录响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeworkSubmissionVO submitHomework(SubmitHomeworkRequest request) {
        LoginUser loginUser = userContextService.currentLoginUser();
        HomeworkAssignmentPO assignment = requireAssignment(request.getAssignmentId());
        validateAssignmentCanSubmit(assignment);
        requireJoinedCourse(loginUser.getUserId(), assignment.getCourseId());
        HomeworkSubmissionPO existed = getSubmission(assignment.getId(), loginUser.getUserId());
        if (existed != null) {
            if (!allowResubmit(assignment)) {
                throw new BizException(HomeworkErrorCode.HOMEWORK_SUBMIT_DUPLICATED);
            }
            return updateSubmission(existed, request);
        }

        HomeworkSubmissionPO submission = new HomeworkSubmissionPO();
        submission.setAssignmentId(assignment.getId());
        submission.setStudentId(loginUser.getUserId());
        submission.setSubmitContent(serializeSubmitContent(request));
        submission.setSubmitTime(LocalDateTime.now());
        submission.setGradeStatus(HomeworkGradeStatus.PENDING);
        submission.setDeleted(0);
        try {
            submissionMapper.insert(submission);
        } catch (DuplicateKeyException ex) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_SUBMIT_DUPLICATED);
        }
        return toSubmissionVO(submission);
    }

    /**
     * 教师批改作业。
     *
     * @param submissionId 提交记录 ID
     * @param request 批改请求
     * @return 批改结果响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeworkGradeVO gradeSubmission(Long submissionId, GradeHomeworkRequest request) {
        if (submissionId == null || submissionId <= 0) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_PARAM_INVALID, "提交记录ID必须为正数");
        }
        LoginUser loginUser = userContextService.currentLoginUser();
        HomeworkSubmissionPO submission = requireSubmission(submissionId);
        HomeworkAssignmentPO assignment = requireAssignment(submission.getAssignmentId());
        CoursePO course = requireCourse(assignment.getCourseId());
        assertTeacherCanMaintain(loginUser, course);

        LocalDateTime now = LocalDateTime.now();
        HomeworkGradeRecordPO gradeRecord = new HomeworkGradeRecordPO();
        gradeRecord.setSubmissionId(submission.getId());
        gradeRecord.setTeacherId(loginUser.getUserId());
        gradeRecord.setScore(request.getScore());
        gradeRecord.setFeedback(request.getFeedback());
        gradeRecord.setGradeTime(now);
        gradeRecord.setDeleted(0);
        gradeRecordMapper.insert(gradeRecord);

        submission.setScore(request.getScore());
        submission.setGradeStatus(HomeworkGradeStatus.GRADED);
        submissionMapper.updateById(submission);

        Long messageId = notifyGradeResult(loginUser.getUserId(), submission.getStudentId(), assignment, submission, request);
        return toGradeVO(gradeRecord, messageId);
    }

    private CoursePO requireCourse(Long courseId) {
        CoursePO course = courseId == null ? null : courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_COURSE_NOT_FOUND);
        }
        return course;
    }

    private HomeworkAssignmentPO requireAssignment(Long assignmentId) {
        HomeworkAssignmentPO assignment = assignmentId == null ? null : assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_ASSIGNMENT_NOT_FOUND);
        }
        return assignment;
    }

    private HomeworkSubmissionPO requireSubmission(Long submissionId) {
        HomeworkSubmissionPO submission = submissionId == null ? null : submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_SUBMISSION_NOT_FOUND);
        }
        return submission;
    }

    private void validateChapter(Long chapterId, Long courseId) {
        if (chapterId == null) {
            return;
        }
        CourseChapterPO chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null || !Objects.equals(chapter.getCourseId(), courseId)) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_CHAPTER_INVALID);
        }
    }

    private void validateAssignmentCanSubmit(HomeworkAssignmentPO assignment) {
        if (assignment.getStatus() != HomeworkAssignmentStatus.PUBLISHED) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_STATUS_INVALID, "作业未发布，不能提交");
        }
        if (assignment.getDeadlineTime() != null && !assignment.getDeadlineTime().isAfter(LocalDateTime.now())) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_DEADLINE_EXPIRED);
        }
    }

    private void requireJoinedCourse(Long userId, Long courseId) {
        Long count = learningCourseMapper.selectCount(Wrappers.<LearningCoursePO>lambdaQuery()
                .eq(LearningCoursePO::getUserId, userId)
                .eq(LearningCoursePO::getCourseId, courseId));
        if (count == null || count <= 0) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_COURSE_NOT_JOINED);
        }
    }

    private HomeworkSubmissionPO getSubmission(Long assignmentId, Long studentId) {
        return submissionMapper.selectOne(Wrappers.<HomeworkSubmissionPO>lambdaQuery()
                .eq(HomeworkSubmissionPO::getAssignmentId, assignmentId)
                .eq(HomeworkSubmissionPO::getStudentId, studentId)
                .last("LIMIT 1"));
    }

    private HomeworkSubmissionVO updateSubmission(HomeworkSubmissionPO submission, SubmitHomeworkRequest request) {
        // 允许重交时沿用唯一索引下的原提交记录，避免为支持重交临时绕过 SQL 设计。
        submission.setSubmitContent(serializeSubmitContent(request));
        submission.setSubmitTime(LocalDateTime.now());
        submission.setScore(null);
        submission.setGradeStatus(HomeworkGradeStatus.PENDING);
        submissionMapper.updateById(submission);
        return toSubmissionVO(submission);
    }

    private void assertTeacherCanMaintain(LoginUser loginUser, CoursePO course) {
        if (hasRole(loginUser, HomeworkConstants.ROLE_ADMIN)) {
            return;
        }
        if (hasRole(loginUser, HomeworkConstants.ROLE_TEACHER)
                && Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            return;
        }
        if (hasPermission(loginUser, HomeworkConstants.PERMISSION_HOMEWORK_MANAGE)
                && Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            return;
        }
        throw new BizException(HomeworkErrorCode.HOMEWORK_FORBIDDEN);
    }

    private Long notifyGradeResult(
            Long teacherId,
            Long studentId,
            HomeworkAssignmentPO assignment,
            HomeworkSubmissionPO submission,
            GradeHomeworkRequest request) {
        String content = "作业《" + assignment.getTitle() + "》已批改，得分：" + request.getScore();
        return messageCommandService.sendToUser(
                teacherId,
                studentId,
                MessageType.COURSE,
                "作业批改完成",
                content,
                "/homework/submissions/" + submission.getId());
    }

    private String serializeSubmitContent(SubmitHomeworkRequest request) {
        if (CollectionUtils.isEmpty(request.getAttachmentUrls())) {
            return request.getSubmitContent();
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("content", request.getSubmitContent());
        payload.put("attachmentUrls", request.getAttachmentUrls());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new BizException(HomeworkErrorCode.HOMEWORK_PARAM_INVALID, "作业提交内容格式不正确");
        }
    }

    private HomeworkAssignmentVO toAssignmentVO(HomeworkAssignmentPO assignment) {
        return BeanCopyUtils.copyBean(assignment, HomeworkAssignmentVO.class, (source, target) -> {
            target.setAllowResubmit(allowResubmit(source));
            target.setPublishTime(source.getCreateTime());
            target.setStatusDesc(source.getStatus() == null ? null : source.getStatus().getDesc());
        });
    }

    private HomeworkSubmissionVO toSubmissionVO(HomeworkSubmissionPO submission) {
        return BeanCopyUtils.copyBean(submission, HomeworkSubmissionVO.class, (source, target) -> {
            target.setSubmissionId(source.getId());
            target.setGradeStatusDesc(source.getGradeStatus() == null ? null : source.getGradeStatus().getDesc());
        });
    }

    private HomeworkGradeVO toGradeVO(HomeworkGradeRecordPO gradeRecord, Long messageId) {
        return BeanCopyUtils.copyBean(gradeRecord, HomeworkGradeVO.class, (source, target) -> {
            target.setGradeStatus(HomeworkGradeStatus.GRADED);
            target.setGradeStatusDesc(HomeworkGradeStatus.GRADED.getDesc());
            target.setMessageId(messageId);
        });
    }

    private boolean allowResubmit(HomeworkAssignmentPO assignment) {
        return Objects.equals(assignment.getAllowResubmit(), 1);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        return loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(roleCode::equalsIgnoreCase);
    }

    private boolean hasPermission(LoginUser loginUser, String permissionCode) {
        return loginUser != null
                && loginUser.getPermissions() != null
                && loginUser.getPermissions().stream().anyMatch(permissionCode::equalsIgnoreCase);
    }
}
