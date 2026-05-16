package com.elysia.mooc.exam.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.exam.domain.dto.CreateQuestionRequest;
import com.elysia.mooc.exam.domain.dto.ExamQuestionQuery;
import com.elysia.mooc.exam.domain.vo.QuestionVO;

/** 题目服务。 */
public interface QuestionService {

    /**
     * 分页查询题目。
     *
     * @param query 查询条件
     * @return 题目分页
     */
    PageResult<QuestionVO> listQuestions(ExamQuestionQuery query);

    /**
     * 创建题目。
     *
     * @param request 创建题目请求
     * @return 创建后的题目
     */
    QuestionVO createQuestion(CreateQuestionRequest request);
}
