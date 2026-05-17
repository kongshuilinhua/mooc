package com.elysia.mooc.homework.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseChapterMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.homework.constants.HomeworkErrorCode;
import com.elysia.mooc.homework.domain.dto.GradeHomeworkRequest;
import com.elysia.mooc.homework.domain.dto.PublishHomeworkRequest;
import com.elysia.mooc.homework.domain.dto.SubmitHomeworkRequest;
import com.elysia.mooc.homework.domain.enums.HomeworkAssignmentStatus;
import com.elysia.mooc.homework.domain.enums.HomeworkGradeStatus;
import com.elysia.mooc.homework.domain.po.HomeworkAssignmentPO;
import com.elysia.mooc.homework.domain.po.HomeworkGradeRecordPO;
import com.elysia.mooc.homework.domain.po.HomeworkSubmissionPO;
import com.elysia.mooc.homework.domain.vo.HomeworkGradeVO;
import com.elysia.mooc.homework.domain.vo.HomeworkSubmissionVO;
import com.elysia.mooc.homework.mapper.HomeworkAssignmentMapper;
import com.elysia.mooc.homework.mapper.HomeworkGradeRecordMapper;
import com.elysia.mooc.homework.mapper.HomeworkSubmissionMapper;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.message.service.MessageCommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 作业服务单元测试。 */
@ExtendWith(MockitoExtension.class)
class HomeworkServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseChapterMapper courseChapterMapper;

    @Mock
    private LearningCourseMapper learningCourseMapper;

    @Mock
    private HomeworkAssignmentMapper assignmentMapper;

    @Mock
    private HomeworkSubmissionMapper submissionMapper;

    @Mock
    private HomeworkGradeRecordMapper gradeRecordMapper;

    @Mock
    private MessageCommandService messageCommandService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private HomeworkServiceImpl homeworkService;

    @Test
    void publishAssignmentShouldCreatePublishedHomeworkForCourseOwner() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L));
        doAnswer(invocation -> {
            HomeworkAssignmentPO assignment = invocation.getArgument(0);
            assignment.setId(25001L);
            assignment.setCreateTime(LocalDateTime.now());
            return 1;
        }).when(assignmentMapper).insert(any(HomeworkAssignmentPO.class));

        PublishHomeworkRequest request = new PublishHomeworkRequest();
        request.setCourseId(3001L);
        request.setTitle("JWT 作业");
        request.setDeadlineTime(LocalDateTime.now().plusDays(3));
        request.setAllowResubmit(true);

        var result = homeworkService.publishAssignment(request);

        ArgumentCaptor<HomeworkAssignmentPO> captor = ArgumentCaptor.forClass(HomeworkAssignmentPO.class);
        verify(assignmentMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(HomeworkAssignmentStatus.PUBLISHED);
        assertThat(captor.getValue().getAllowResubmit()).isEqualTo(1);
        assertThat(result.getId()).isEqualTo(25001L);
        assertThat(result.getStatus()).isEqualTo(HomeworkAssignmentStatus.PUBLISHED);
    }

    @Test
    void submitHomeworkShouldInsertPendingSubmissionWhenStudentJoinedCourse() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(assignmentMapper.selectById(25001L)).thenReturn(assignment(25001L, 3001L, HomeworkAssignmentStatus.PUBLISHED, 0));
        when(learningCourseMapper.selectCount(any())).thenReturn(1L);
        when(submissionMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            HomeworkSubmissionPO submission = invocation.getArgument(0);
            submission.setId(25101L);
            return 1;
        }).when(submissionMapper).insert(any(HomeworkSubmissionPO.class));

        SubmitHomeworkRequest request = new SubmitHomeworkRequest();
        request.setAssignmentId(25001L);
        request.setSubmitContent("已完成作业");

        HomeworkSubmissionVO result = homeworkService.submitHomework(request);

        ArgumentCaptor<HomeworkSubmissionPO> captor = ArgumentCaptor.forClass(HomeworkSubmissionPO.class);
        verify(submissionMapper).insert(captor.capture());
        assertThat(captor.getValue().getStudentId()).isEqualTo(3L);
        assertThat(captor.getValue().getGradeStatus()).isEqualTo(HomeworkGradeStatus.PENDING);
        assertThat(result.getSubmissionId()).isEqualTo(25101L);
    }

    @Test
    void submitHomeworkShouldRejectDuplicateWhenResubmitDisabled() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(assignmentMapper.selectById(25001L)).thenReturn(assignment(25001L, 3001L, HomeworkAssignmentStatus.PUBLISHED, 0));
        when(learningCourseMapper.selectCount(any())).thenReturn(1L);
        when(submissionMapper.selectOne(any())).thenReturn(submission(25101L, 25001L, 3L));

        SubmitHomeworkRequest request = new SubmitHomeworkRequest();
        request.setAssignmentId(25001L);
        request.setSubmitContent("重复提交");

        assertThatThrownBy(() -> homeworkService.submitHomework(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(HomeworkErrorCode.HOMEWORK_SUBMIT_DUPLICATED.code());
    }

    @Test
    void submitHomeworkShouldUpdateExistingSubmissionWhenResubmitEnabled() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(assignmentMapper.selectById(25001L)).thenReturn(assignment(25001L, 3001L, HomeworkAssignmentStatus.PUBLISHED, 1));
        when(learningCourseMapper.selectCount(any())).thenReturn(1L);
        when(submissionMapper.selectOne(any())).thenReturn(submission(25101L, 25001L, 3L));

        SubmitHomeworkRequest request = new SubmitHomeworkRequest();
        request.setAssignmentId(25001L);
        request.setSubmitContent("重交内容");

        HomeworkSubmissionVO result = homeworkService.submitHomework(request);

        verify(submissionMapper).updateById(any(HomeworkSubmissionPO.class));
        assertThat(result.getSubmissionId()).isEqualTo(25101L);
        assertThat(result.getGradeStatus()).isEqualTo(HomeworkGradeStatus.PENDING);
    }

    @Test
    void submitHomeworkShouldRejectExpiredAssignment() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        HomeworkAssignmentPO assignment = assignment(25001L, 3001L, HomeworkAssignmentStatus.PUBLISHED, 1);
        assignment.setDeadlineTime(LocalDateTime.now().minusMinutes(1));
        when(assignmentMapper.selectById(25001L)).thenReturn(assignment);

        SubmitHomeworkRequest request = new SubmitHomeworkRequest();
        request.setAssignmentId(25001L);
        request.setSubmitContent("逾期提交");

        assertThatThrownBy(() -> homeworkService.submitHomework(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(HomeworkErrorCode.HOMEWORK_DEADLINE_EXPIRED.code());
    }

    @Test
    void submitHomeworkShouldRejectUnpublishedAssignment() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(assignmentMapper.selectById(25001L)).thenReturn(assignment(25001L, 3001L, HomeworkAssignmentStatus.DRAFT, 1));

        SubmitHomeworkRequest request = new SubmitHomeworkRequest();
        request.setAssignmentId(25001L);
        request.setSubmitContent("草稿作业提交");

        assertThatThrownBy(() -> homeworkService.submitHomework(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(HomeworkErrorCode.HOMEWORK_STATUS_INVALID.code());
    }

    @Test
    void submitHomeworkShouldRejectStudentWithoutLearningCourse() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(assignmentMapper.selectById(25001L)).thenReturn(assignment(25001L, 3001L, HomeworkAssignmentStatus.PUBLISHED, 1));
        when(learningCourseMapper.selectCount(any())).thenReturn(0L);

        SubmitHomeworkRequest request = new SubmitHomeworkRequest();
        request.setAssignmentId(25001L);
        request.setSubmitContent("未选课提交");

        assertThatThrownBy(() -> homeworkService.submitHomework(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(HomeworkErrorCode.HOMEWORK_COURSE_NOT_JOINED.code());
    }

    @Test
    void gradeSubmissionShouldWriteGradeAndNotifyStudent() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(submissionMapper.selectById(25101L)).thenReturn(submission(25101L, 25001L, 3L));
        when(assignmentMapper.selectById(25001L)).thenReturn(assignment(25001L, 3001L, HomeworkAssignmentStatus.PUBLISHED, 0));
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L));
        doAnswer(invocation -> {
            HomeworkGradeRecordPO gradeRecord = invocation.getArgument(0);
            gradeRecord.setId(25201L);
            return 1;
        }).when(gradeRecordMapper).insert(any(HomeworkGradeRecordPO.class));
        when(messageCommandService.sendToUser(
                eq(2L), eq(3L), eq(MessageType.COURSE), eq("作业批改完成"), any(), eq("/homework/submissions/25101")))
                .thenReturn(101L);

        GradeHomeworkRequest request = new GradeHomeworkRequest();
        request.setScore(new BigDecimal("95.00"));
        request.setFeedback("完成不错");

        HomeworkGradeVO result = homeworkService.gradeSubmission(25101L, request);

        verify(submissionMapper).updateById(any(HomeworkSubmissionPO.class));
        verify(messageCommandService).sendToUser(
                eq(2L), eq(3L), eq(MessageType.COURSE), eq("作业批改完成"), any(), eq("/homework/submissions/25101"));
        assertThat(result.getId()).isEqualTo(25201L);
        assertThat(result.getGradeStatus()).isEqualTo(HomeworkGradeStatus.GRADED);
        assertThat(result.getMessageId()).isEqualTo(101L);
    }

    @Test
    void gradeSubmissionShouldRejectOtherTeacherCourse() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(submissionMapper.selectById(25101L)).thenReturn(submission(25101L, 25001L, 3L));
        when(assignmentMapper.selectById(25001L)).thenReturn(assignment(25001L, 3001L, HomeworkAssignmentStatus.PUBLISHED, 0));
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 9L));

        GradeHomeworkRequest request = new GradeHomeworkRequest();
        request.setScore(new BigDecimal("80.00"));

        assertThatThrownBy(() -> homeworkService.gradeSubmission(25101L, request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(HomeworkErrorCode.HOMEWORK_FORBIDDEN.code());
    }

    private LoginUser teacher() {
        return new LoginUser(2L, "teacher", List.of("TEACHER"), List.of());
    }

    private LoginUser student() {
        return new LoginUser(3L, "student", List.of("STUDENT"), List.of());
    }

    private CoursePO course(Long id, Long teacherId) {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setTeacherId(teacherId);
        return course;
    }

    private HomeworkAssignmentPO assignment(Long id, Long courseId, HomeworkAssignmentStatus status, Integer allowResubmit) {
        HomeworkAssignmentPO assignment = new HomeworkAssignmentPO();
        assignment.setId(id);
        assignment.setCourseId(courseId);
        assignment.setTitle("JWT 作业");
        assignment.setStatus(status);
        assignment.setAllowResubmit(allowResubmit);
        assignment.setDeadlineTime(LocalDateTime.now().plusDays(1));
        return assignment;
    }

    private HomeworkSubmissionPO submission(Long id, Long assignmentId, Long studentId) {
        HomeworkSubmissionPO submission = new HomeworkSubmissionPO();
        submission.setId(id);
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setGradeStatus(HomeworkGradeStatus.PENDING);
        submission.setSubmitContent("提交内容");
        return submission;
    }
}
