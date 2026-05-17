package com.elysia.mooc.common.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** 敏感字段脱敏测试。 */
class SensitiveFieldMaskerTest {

    private final SensitiveFieldMasker masker = new SensitiveFieldMasker();

    @Test
    void maskShouldHideSensitiveFields() {
        String raw = "{\"password\":\"123456\",\"accessToken\":\"abc\",\"apiKey\":\"secret\"} Authorization=Bearer token";

        String result = masker.mask(raw);

        assertThat(result).doesNotContain("123456", "abc", "secret", "Bearer token");
        assertThat(result).contains("******");
    }
}
