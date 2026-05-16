package com.elysia.mooc.exam.service;

import com.elysia.mooc.exam.domain.bo.GradingResult;
import com.elysia.mooc.exam.domain.po.ExamQuestionOptionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import java.math.BigDecimal;
import java.util.List;

/** 自动判分服务。 */
public interface AutoGradingService {

    /**
     * 按题型对单题答案自动判分。
     *
     * @param question 题目
     * @param options 题目选项
     * @param answer 用户答案
     * @param score 本试卷中该题分值
     * @return 判分结果
     */
    GradingResult grade(ExamQuestionPO question, List<ExamQuestionOptionPO> options, String answer, BigDecimal score);
}
