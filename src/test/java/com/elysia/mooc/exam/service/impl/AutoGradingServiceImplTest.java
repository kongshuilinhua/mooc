package com.elysia.mooc.exam.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.elysia.mooc.exam.domain.bo.GradingResult;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.elysia.mooc.exam.domain.po.ExamQuestionOptionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 自动判分服务测试。 */
class AutoGradingServiceImplTest {

    private final AutoGradingServiceImpl autoGradingService = new AutoGradingServiceImpl();

    @Test
    void shouldGradeSingleChoiceByCorrectOption() {
        ExamQuestionPO question = question(ExamQuestionType.SINGLE, "A");

        GradingResult result = autoGradingService.grade(
                question,
                List.of(option("A", true), option("B", false)),
                "A",
                new BigDecimal("5.00"));

        assertThat(result.correct()).isTrue();
        assertThat(result.score()).isEqualByComparingTo("5.00");
        assertThat(result.manualReviewRequired()).isFalse();
    }

    @Test
    void shouldGradeMultiChoiceBySetEquality() {
        ExamQuestionPO question = question(ExamQuestionType.MULTI, "A,C");

        GradingResult result = autoGradingService.grade(
                question,
                List.of(option("A", true), option("B", false), option("C", true)),
                "C,A",
                new BigDecimal("8.00"));

        assertThat(result.correct()).isTrue();
        assertThat(result.score()).isEqualByComparingTo("8.00");
    }

    @Test
    void shouldGradeJudgeAnswerByChineseText() {
        ExamQuestionPO question = question(ExamQuestionType.JUDGE, "正确");

        GradingResult result = autoGradingService.grade(
                question,
                List.of(option("正确", true), option("错误", false)),
                "对",
                new BigDecimal("3.00"));

        assertThat(result.correct()).isTrue();
        assertThat(result.score()).isEqualByComparingTo("3.00");
    }

    @Test
    void shouldLeaveShortAnswerForManualReview() {
        ExamQuestionPO question = question(ExamQuestionType.SHORT, "参考答案");

        GradingResult result = autoGradingService.grade(question, List.of(), "学生答案", new BigDecimal("10.00"));

        assertThat(result.correct()).isNull();
        assertThat(result.score()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.manualReviewRequired()).isTrue();
    }

    private ExamQuestionPO question(ExamQuestionType type, String answer) {
        ExamQuestionPO question = new ExamQuestionPO();
        question.setQuestionType(type);
        question.setAnswerText(answer);
        return question;
    }

    private ExamQuestionOptionPO option(String key, boolean correct) {
        ExamQuestionOptionPO option = new ExamQuestionOptionPO();
        option.setOptionKey(key);
        option.setOptionText(key);
        option.setCorrect(correct ? 1 : 0);
        return option;
    }
}
