package com.elysia.mooc.interaction.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.interaction.constants.InteractionErrorCode;
import com.elysia.mooc.interaction.domain.dto.AcceptAnswerRequest;
import com.elysia.mooc.interaction.domain.dto.CreateRatingRequest;
import com.elysia.mooc.interaction.domain.dto.LikeRequest;
import com.elysia.mooc.interaction.domain.enums.AnswerAcceptedStatus;
import com.elysia.mooc.interaction.domain.enums.AnswerStatus;
import com.elysia.mooc.interaction.domain.enums.InteractionDeletedStatus;
import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import com.elysia.mooc.interaction.domain.enums.QuestionStatus;
import com.elysia.mooc.interaction.domain.enums.RatingStatus;
import com.elysia.mooc.interaction.domain.po.CourseFavoritePO;
import com.elysia.mooc.interaction.domain.po.CourseRatingPO;
import com.elysia.mooc.interaction.domain.po.InteractionAnswerPO;
import com.elysia.mooc.interaction.domain.po.InteractionLikePO;
import com.elysia.mooc.interaction.domain.po.InteractionQuestionPO;
import com.elysia.mooc.interaction.domain.vo.LikeResultVO;
import com.elysia.mooc.interaction.domain.vo.RatingResultVO;
import com.elysia.mooc.interaction.mapper.CourseFavoriteMapper;
import com.elysia.mooc.interaction.mapper.CourseRatingMapper;
import com.elysia.mooc.interaction.mapper.InteractionAnswerMapper;
import com.elysia.mooc.interaction.mapper.InteractionLikeMapper;
import com.elysia.mooc.interaction.mapper.InteractionQuestionMapper;
import com.elysia.mooc.interaction.mapper.InteractionReportMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 互动服务核心业务测试。 */
@ExtendWith(MockitoExtension.class)
class InteractionServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private InteractionQuestionMapper questionMapper;

    @Mock
    private InteractionAnswerMapper answerMapper;

    @Mock
    private CourseRatingMapper ratingMapper;

    @Mock
    private CourseFavoriteMapper favoriteMapper;

    @Mock
    private InteractionLikeMapper likeMapper;

    @Mock
    private InteractionReportMapper reportMapper;

    @InjectMocks
    private InteractionServiceImpl interactionService;

    @Test
    void acceptAnswerShouldRejectWhenCurrentUserIsNotQuestionOwner() {
        when(userContextService.currentUserId()).thenReturn(4L);
        when(answerMapper.selectById(9101L)).thenReturn(answer(9101L, 9001L, 2L, AnswerAcceptedStatus.NOT_ACCEPTED));
        when(questionMapper.selectById(9001L)).thenReturn(question(9001L, 3001L, 3L, QuestionStatus.OPEN));
        AcceptAnswerRequest request = new AcceptAnswerRequest();
        request.setQuestionId(9001L);

        assertThatThrownBy(() -> interactionService.acceptAnswer(9101L, request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(InteractionErrorCode.INTERACTION_FORBIDDEN.code());
    }

    @Test
    void acceptAnswerShouldMarkAnswerAndResolveQuestion() {
        when(userContextService.currentUserId()).thenReturn(3L);
        InteractionAnswerPO answer = answer(9101L, 9001L, 2L, AnswerAcceptedStatus.NOT_ACCEPTED);
        InteractionQuestionPO question = question(9001L, 3001L, 3L, QuestionStatus.OPEN);
        when(answerMapper.selectById(9101L)).thenReturn(answer);
        when(questionMapper.selectById(9001L)).thenReturn(question);
        when(answerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Boolean result = interactionService.acceptAnswer(9101L, new AcceptAnswerRequest());

        assertThat(result).isTrue();
        assertThat(answer.getAccepted()).isEqualTo(AnswerAcceptedStatus.ACCEPTED);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.RESOLVED);
        verify(answerMapper).updateById(answer);
        verify(questionMapper).updateById(question);
    }

    @Test
    void rateCourseShouldUpdateExistingRatingAndRefreshAverageScore() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L));
        CourseRatingPO existed = rating(9201L, 3001L, 3L, 3);
        when(ratingMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existed);
        when(ratingMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                rating(9201L, 3001L, 3L, 5),
                rating(9202L, 3001L, 4L, 3)));
        CreateRatingRequest request = new CreateRatingRequest();
        request.setScore(5);
        request.setContent("课程不错");

        RatingResultVO result = interactionService.rateCourse(3001L, request);

        assertThat(result.getId()).isEqualTo(9201L);
        assertThat(result.getScore()).isEqualTo(5);
        assertThat(result.getRatingScore()).isEqualByComparingTo(new BigDecimal("4.00"));
        verify(ratingMapper).updateById(existed);
        verify(courseMapper).update(any(), any());
    }

    @Test
    void favoriteCourseShouldBeIdempotentWhenAlreadyFavorited() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L));
        when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(favorite(3001L, 3L));

        Boolean result = interactionService.favoriteCourse(3001L);

        assertThat(result).isTrue();
        verify(favoriteMapper, never()).insert(any(CourseFavoritePO.class));
        verify(courseMapper, never()).update(any(), any());
    }

    @Test
    void likeQuestionShouldInsertOnceAndReturnCurrentCount() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(questionMapper.selectById(9001L)).thenReturn(question(9001L, 3001L, 3L, QuestionStatus.OPEN));
        when(likeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        LikeRequest request = new LikeRequest();
        request.setTargetType(InteractionTargetType.QUESTION);
        request.setTargetId(9001L);

        LikeResultVO result = interactionService.like(request);

        assertThat(result.getLiked()).isTrue();
        assertThat(result.getCurrentLikeCount()).isEqualTo(2);
        ArgumentCaptor<InteractionLikePO> captor = ArgumentCaptor.forClass(InteractionLikePO.class);
        verify(likeMapper).insert(captor.capture());
        assertThat(captor.getValue().getDeleted()).isEqualTo(InteractionDeletedStatus.NORMAL.getValue());
    }

    @Test
    void handleReportShouldRejectNonAdminUser() {
        when(userContextService.currentLoginUser()).thenReturn(new LoginUser(3L, "student", List.of("STUDENT"), List.of()));

        assertThatThrownBy(() -> interactionService.listReports(null))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(InteractionErrorCode.INTERACTION_FORBIDDEN.code());
    }

    private CoursePO course(Long id) {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setTitle("测试课程");
        course.setStatus(CourseStatus.PUBLISHED);
        return course;
    }

    private InteractionQuestionPO question(Long id, Long courseId, Long userId, QuestionStatus status) {
        InteractionQuestionPO question = new InteractionQuestionPO();
        question.setId(id);
        question.setCourseId(courseId);
        question.setUserId(userId);
        question.setTitle("测试问题");
        question.setContent("测试内容");
        question.setStatus(status);
        question.setAnswerCount(1);
        return question;
    }

    private InteractionAnswerPO answer(Long id, Long questionId, Long userId, AnswerAcceptedStatus accepted) {
        InteractionAnswerPO answer = new InteractionAnswerPO();
        answer.setId(id);
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer.setContent("测试回答");
        answer.setAccepted(accepted);
        answer.setStatus(AnswerStatus.NORMAL);
        answer.setLikeCount(0);
        return answer;
    }

    private CourseRatingPO rating(Long id, Long courseId, Long userId, Integer score) {
        CourseRatingPO rating = new CourseRatingPO();
        rating.setId(id);
        rating.setCourseId(courseId);
        rating.setUserId(userId);
        rating.setScore(score);
        rating.setStatus(RatingStatus.NORMAL);
        rating.setComment("评价");
        return rating;
    }

    private CourseFavoritePO favorite(Long courseId, Long userId) {
        CourseFavoritePO favorite = new CourseFavoritePO();
        favorite.setId(1L);
        favorite.setCourseId(courseId);
        favorite.setUserId(userId);
        favorite.setDeleted(InteractionDeletedStatus.NORMAL.getValue());
        return favorite;
    }
}
