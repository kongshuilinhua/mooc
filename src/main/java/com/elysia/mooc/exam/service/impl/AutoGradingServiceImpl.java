package com.elysia.mooc.exam.service.impl;

import com.elysia.mooc.exam.domain.bo.GradingResult;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.elysia.mooc.exam.domain.po.ExamQuestionOptionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.service.AutoGradingService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 自动判分服务实现。 */
@Service
public class AutoGradingServiceImpl implements AutoGradingService {

    /**
     * 按题型对单题答案自动判分。
     *
     * @param question 题目
     * @param options 题目选项
     * @param answer 用户答案
     * @param score 本试卷中该题分值
     * @return 判分结果
     */
    @Override
    public GradingResult grade(
            ExamQuestionPO question,
            List<ExamQuestionOptionPO> options,
            String answer,
            BigDecimal score) {
        if (question == null || question.getQuestionType() == null) {
            return wrong(score, "题目数据不完整，无法判分");
        }
        if (question.getQuestionType() == ExamQuestionType.SHORT) {
            return new GradingResult(null, BigDecimal.ZERO, "简答题需要人工批改", true);
        }

        boolean correct = switch (question.getQuestionType()) {
            case SINGLE -> matchChoiceAnswer(options, question.getAnswerText(), answer, false);
            case MULTI -> matchChoiceAnswer(options, question.getAnswerText(), answer, true);
            case JUDGE -> matchJudgeAnswer(options, question.getAnswerText(), answer);
            case SHORT -> false;
        };
        return correct
                ? new GradingResult(Boolean.TRUE, safeScore(score), "自动判分正确", false)
                : wrong(score, "自动判分错误");
    }

    private GradingResult wrong(BigDecimal score, String comment) {
        return new GradingResult(Boolean.FALSE, BigDecimal.ZERO, comment, false);
    }

    private boolean matchChoiceAnswer(
            List<ExamQuestionOptionPO> options,
            String referenceAnswer,
            String userAnswer,
            boolean multi) {
        Set<String> submitted = normalizeChoiceSet(userAnswer);
        if (submitted.isEmpty()) {
            return false;
        }

        Set<String> expectedByOption = correctOptions(options).stream()
                .map(ExamQuestionOptionPO::getOptionKey)
                .filter(StringUtils::hasText)
                .map(this::normalizeChoiceToken)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!expectedByOption.isEmpty()) {
            return multi ? submitted.equals(expectedByOption) : submitted.size() == 1 && submitted.equals(expectedByOption);
        }

        Set<String> expectedByAnswer = normalizeChoiceSet(referenceAnswer);
        return !expectedByAnswer.isEmpty()
                && (multi ? submitted.equals(expectedByAnswer) : submitted.size() == 1 && submitted.equals(expectedByAnswer));
    }

    private boolean matchJudgeAnswer(List<ExamQuestionOptionPO> options, String referenceAnswer, String userAnswer) {
        String submitted = normalizeJudge(userAnswer);
        if (!StringUtils.hasText(submitted)) {
            return false;
        }
        for (ExamQuestionOptionPO option : correctOptions(options)) {
            if (submitted.equals(normalizeJudge(option.getOptionKey()))
                    || submitted.equals(normalizeJudge(option.getOptionText()))) {
                return true;
            }
        }
        return submitted.equals(normalizeJudge(referenceAnswer));
    }

    private List<ExamQuestionOptionPO> correctOptions(List<ExamQuestionOptionPO> options) {
        if (CollectionUtils.isEmpty(options)) {
            return Collections.emptyList();
        }
        return options.stream()
                .filter(option -> Objects.equals(option.getCorrect(), 1))
                .toList();
    }

    private Set<String> normalizeChoiceSet(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptySet();
        }
        return Arrays.stream(value.replace('，', ',')
                        .replace('、', ',')
                        .replace(';', ',')
                        .replace('；', ',')
                        .replace('|', ',')
                        .split(","))
                .map(this::normalizeChoiceToken)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeChoiceToken(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeJudge(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String text = value.trim().toUpperCase(Locale.ROOT);
        if (Set.of("TRUE", "T", "Y", "YES", "1", "正确", "对", "是").contains(text)) {
            return "TRUE";
        }
        if (Set.of("FALSE", "F", "N", "NO", "0", "错误", "错", "否").contains(text)) {
            return "FALSE";
        }
        return text;
    }

    private BigDecimal safeScore(BigDecimal score) {
        return score == null ? BigDecimal.ZERO : score;
    }
}
