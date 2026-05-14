package com.elysia.mooc.interaction.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.interaction.domain.dto.AcceptAnswerRequest;
import com.elysia.mooc.interaction.domain.dto.CreateAnswerRequest;
import com.elysia.mooc.interaction.domain.dto.CreateQuestionRequest;
import com.elysia.mooc.interaction.domain.dto.CreateRatingRequest;
import com.elysia.mooc.interaction.domain.dto.HandleReportRequest;
import com.elysia.mooc.interaction.domain.dto.LikeRequest;
import com.elysia.mooc.interaction.domain.dto.QuestionQuery;
import com.elysia.mooc.interaction.domain.dto.ReportQuery;
import com.elysia.mooc.interaction.domain.dto.ReportRequest;
import com.elysia.mooc.interaction.domain.vo.InteractionCreateResultVO;
import com.elysia.mooc.interaction.domain.vo.LikeResultVO;
import com.elysia.mooc.interaction.domain.vo.QuestionItemVO;
import com.elysia.mooc.interaction.domain.vo.RatingResultVO;
import com.elysia.mooc.interaction.domain.vo.ReportItemVO;
import com.elysia.mooc.interaction.domain.vo.ReportResultVO;

/** 课程互动服务。 */
public interface InteractionService {

    /**
     * 分页查询课程问题。
     *
     * @param courseId 课程 ID
     * @param query    查询参数
     * @return 问题分页结果
     */
    PageResult<QuestionItemVO> listQuestions(Long courseId, QuestionQuery query);

    /**
     * 创建课程问题。
     *
     * @param courseId 课程 ID
     * @param request  创建问题请求
     * @return 创建结果
     */
    InteractionCreateResultVO createQuestion(Long courseId, CreateQuestionRequest request);

    /**
     * 创建问题回答。
     *
     * @param questionId 问题 ID
     * @param request    创建回答请求
     * @return 创建结果
     */
    InteractionCreateResultVO createAnswer(Long questionId, CreateAnswerRequest request);

    /**
     * 采纳回答。
     *
     * @param answerId 回答 ID
     * @param request  采纳请求
     * @return 是否处理成功
     */
    Boolean acceptAnswer(Long answerId, AcceptAnswerRequest request);

    /**
     * 创建或更新课程评价。
     *
     * @param courseId 课程 ID
     * @param request  评分请求
     * @return 评价结果
     */
    RatingResultVO rateCourse(Long courseId, CreateRatingRequest request);

    /**
     * 收藏课程。
     *
     * @param courseId 课程 ID
     * @return 是否处理成功
     */
    Boolean favoriteCourse(Long courseId);

    /**
     * 取消收藏课程。
     *
     * @param courseId 课程 ID
     * @return 是否处理成功
     */
    Boolean unfavoriteCourse(Long courseId);

    /**
     * 点赞互动目标。
     *
     * @param request 点赞请求
     * @return 点赞结果
     */
    LikeResultVO like(LikeRequest request);

    /**
     * 创建举报。
     *
     * @param request 举报请求
     * @return 举报结果
     */
    ReportResultVO report(ReportRequest request);

    /**
     * 管理端分页查询举报。
     *
     * @param query 查询参数
     * @return 举报分页
     */
    PageResult<ReportItemVO> listReports(ReportQuery query);

    /**
     * 管理端处理举报。
     *
     * @param reportId 举报 ID
     * @param request  处理请求
     * @return 是否处理成功
     */
    Boolean handleReport(Long reportId, HandleReportRequest request);
}
