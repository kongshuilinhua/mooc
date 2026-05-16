package com.elysia.mooc.exam.domain.bo;

import java.math.BigDecimal;

/** 单题判分结果。 */
public record GradingResult(Boolean correct, BigDecimal score, String comment, boolean manualReviewRequired) {
}
