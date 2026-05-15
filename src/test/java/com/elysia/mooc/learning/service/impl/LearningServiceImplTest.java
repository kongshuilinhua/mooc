package com.elysia.mooc.learning.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.event.service.BusinessEventPublisher;
import com.elysia.mooc.learning.constants.LearningConstants;
import com.elysia.mooc.learning.constants.LearningErrorCode;
import com.elysia.mooc.learning.domain.dto.JoinCourseRequest;
import com.elysia.mooc.learning.domain.dto.LearningHeartbeatRequest;
import com.elysia.mooc.learning.domain.enums.LearningFinishedStatus;
import com.elysia.mooc.learning.domain.enums.LearningSource;
import com.elysia.mooc.learning.domain.po.LearningBehaviorLogPO;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.domain.po.LearningRecordPO;
import com.elysia.mooc.learning.domain.vo.LearningRecordVO;
import com.elysia.mooc.learning.mapper.LearningBehaviorLogMapper;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.learning.mapper.LearningRecordMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 学习进度服务核心业务测试。 */
@ExtendWith(MockitoExtension.class)
class LearningServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseSectionMapper courseSectionMapper;

    @Mock
    private LearningCourseMapper learningCourseMapper;

    @Mock
    private LearningRecordMapper learningRecordMapper;

    @Mock
    private LearningBehaviorLogMapper learningBehaviorLogMapper;

    @Mock
    private BusinessEventPublisher businessEventPublisher;

    @InjectMocks
    private LearningServiceImpl learningService;

    @Test
    void joinCourseShouldBeIdempotentWhenAlreadyJoined() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, CourseStatus.PUBLISHED, BigDecimal.ZERO));
        when(learningCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(learningCourse(3L, 3001L));
        JoinCourseRequest request = new JoinCourseRequest();
        request.setCourseId(3001L);

        Boolean result = learningService.joinCourse(request);

        assertThat(result).isTrue();
        verify(learningCourseMapper, never()).insert(any(LearningCoursePO.class));
    }

    @Test
    void joinCourseShouldRejectPaidCourseBeforeOrderModule() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(courseMapper.selectById(3002L)).thenReturn(course(3002L, CourseStatus.PUBLISHED, new BigDecimal("99.00")));
        JoinCourseRequest request = new JoinCourseRequest();
        request.setCourseId(3002L);

        assertThatThrownBy(() -> learningService.joinCourse(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(LearningErrorCode.LEARNING_COURSE_NOT_AVAILABLE.code());
    }

    @Test
    void heartbeatShouldRejectWhenCourseNotJoined() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(learningCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> learningService.heartbeat(heartbeat(3001L, 5001L, 120, 900)))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(LearningErrorCode.LEARNING_COURSE_NOT_JOINED.code());
    }

    @Test
    void heartbeatRequestShouldRejectPositionGreaterThanDuration() {
        LearningHeartbeatRequest request = heartbeat(3001L, 5001L, 901, 900);

        assertThatThrownBy(request::check)
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(LearningErrorCode.LEARNING_PARAM_INVALID.code());
    }

    @Test
    void heartbeatShouldPreventProgressRollbackAndClampLearnedSeconds() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(learningCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(learningCourse(3L, 3001L));
        when(courseSectionMapper.selectById(5001L)).thenReturn(section(5001L, 3001L, 900));
        when(learningRecordMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(record(3L, 3001L, 5001L, 300, 120, 900, LearningFinishedStatus.UNFINISHED));
        when(courseSectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                record(3L, 3001L, 5001L, 300, 120, 900, LearningFinishedStatus.UNFINISHED)));

        LearningRecordVO result = learningService.heartbeat(heartbeat(3001L, 5001L, 200, 900));

        assertThat(result.getLastPlayTime()).isEqualTo(300);
        assertThat(result.getMaxHistoryTime()).isEqualTo(300);
        ArgumentCaptor<LearningRecordPO> recordCaptor = ArgumentCaptor.forClass(LearningRecordPO.class);
        verify(learningRecordMapper).updateById(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getLastPosition()).isEqualTo(300);
        assertThat(recordCaptor.getValue().getLearnedSeconds()).isEqualTo(120);

        ArgumentCaptor<LearningBehaviorLogPO> logCaptor = ArgumentCaptor.forClass(LearningBehaviorLogPO.class);
        verify(learningBehaviorLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getExtra()).contains("\"deltaSeconds\":0");
        verify(businessEventPublisher).publishLearningBehaviorCreated(
                logCaptor.getValue().getId(),
                3L,
                3001L,
                5001L,
                logCaptor.getValue().getEventType(),
                300,
                0);
    }

    @Test
    void heartbeatShouldFinishSectionAndRefreshCourseProgress() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(learningCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(learningCourse(3L, 3001L));
        when(courseSectionMapper.selectById(5001L)).thenReturn(section(5001L, 3001L, 900));
        when(learningRecordMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(record(3L, 3001L, 5001L, 760, 700, 900, LearningFinishedStatus.UNFINISHED));
        when(courseSectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                record(3L, 3001L, 5001L, 820, 760, 900, LearningFinishedStatus.FINISHED),
                record(3L, 3001L, 5002L, 100, 100, 900, LearningFinishedStatus.UNFINISHED)));

        LearningRecordVO result = learningService.heartbeat(heartbeat(3001L, 5001L, 820, 900));

        assertThat(result.getCompleted()).isTrue();
        ArgumentCaptor<LearningRecordPO> recordCaptor = ArgumentCaptor.forClass(LearningRecordPO.class);
        verify(learningRecordMapper).updateById(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getFinished()).isEqualTo(LearningFinishedStatus.FINISHED);
        assertThat(recordCaptor.getValue().getLearnedSeconds()).isEqualTo(700 + LearningConstants.MAX_HEARTBEAT_SECONDS);

        ArgumentCaptor<LearningCoursePO> courseCaptor = ArgumentCaptor.forClass(LearningCoursePO.class);
        verify(learningCourseMapper).updateById(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getProgressPercent()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(courseCaptor.getValue().getLearnedSeconds()).isEqualTo(860);
    }

    private LearningHeartbeatRequest heartbeat(Long courseId, Long sectionId, int position, int duration) {
        LearningHeartbeatRequest request = new LearningHeartbeatRequest();
        request.setCourseId(courseId);
        request.setSectionId(sectionId);
        request.setPosition(position);
        request.setDuration(duration);
        return request;
    }

    private CoursePO course(Long id, CourseStatus status, BigDecimal price) {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setTitle("测试课程");
        course.setStatus(status);
        course.setPrice(price);
        return course;
    }

    private CourseSectionPO section(Long id, Long courseId, Integer duration) {
        CourseSectionPO section = new CourseSectionPO();
        section.setId(id);
        section.setCourseId(courseId);
        section.setTitle("测试小节");
        section.setDurationSeconds(duration);
        section.setStatus(EnableStatus.ENABLED);
        return section;
    }

    private LearningCoursePO learningCourse(Long userId, Long courseId) {
        LearningCoursePO learningCourse = new LearningCoursePO();
        learningCourse.setId(8001L);
        learningCourse.setUserId(userId);
        learningCourse.setCourseId(courseId);
        learningCourse.setSource(LearningSource.FREE);
        learningCourse.setProgressPercent(BigDecimal.ZERO);
        learningCourse.setLearnedSeconds(0);
        learningCourse.setFinished(LearningFinishedStatus.UNFINISHED);
        return learningCourse;
    }

    private LearningRecordPO record(
            Long userId,
            Long courseId,
            Long sectionId,
            Integer lastPosition,
            Integer learnedSeconds,
            Integer durationSeconds,
            LearningFinishedStatus finished) {
        LearningRecordPO record = new LearningRecordPO();
        record.setId(sectionId);
        record.setUserId(userId);
        record.setCourseId(courseId);
        record.setSectionId(sectionId);
        record.setLastPosition(lastPosition);
        record.setLearnedSeconds(learnedSeconds);
        record.setDurationSeconds(durationSeconds);
        record.setFinished(finished);
        return record;
    }
}
