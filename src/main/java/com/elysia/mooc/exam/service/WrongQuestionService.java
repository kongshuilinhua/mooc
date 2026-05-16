package com.elysia.mooc.exam.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.exam.domain.dto.WrongQuestionQuery;
import com.elysia.mooc.exam.domain.vo.WrongQuestionVO;

/** 错题本服务。 */
public interface WrongQuestionService {

    /**
     * 分页查询当前用户错题。
     *
     * @param query 查询条件
     * @return 错题分页
     */
    PageResult<WrongQuestionVO> listWrongQuestions(WrongQuestionQuery query);

    /**
     * 写入或累加错题。
     *
     * @param userId 用户 ID
     * @param questionId 题目 ID
     * @param courseId 课程 ID
     */
    void recordWrongQuestion(Long userId, Long questionId, Long courseId);
}
