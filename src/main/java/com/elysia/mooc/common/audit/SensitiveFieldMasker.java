package com.elysia.mooc.common.audit;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 敏感字段脱敏工具。
 * 审计和幂等快照都可能保存文本，因此统一在落库前过滤密码、Token 和密钥类字段。
 */
@Component
public class SensitiveFieldMasker {

    private static final String MASK = "******";
    private static final int MAX_TEXT_LENGTH = 10_000;
    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"?(password|passwd|pwd|token|accessToken|refreshToken|authorization|secret|apiKey|api_key)\"?\\s*[:=]\\s*\")([^\"]*)(\")");
    private static final Pattern SIMPLE_FIELD_PATTERN = Pattern.compile(
            "(?i)((password|passwd|pwd|token|accessToken|refreshToken|authorization|secret|apiKey|api_key)\\s*[:=]\\s*)([^,}\\s]+)");
    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._\\-]+");

    /**
     * 对任意文本做敏感字段掩码处理。
     *
     * @param text 原始文本
     * @return 已脱敏文本
     */
    public String mask(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String masked = JSON_FIELD_PATTERN.matcher(text).replaceAll("$1" + MASK + "$4");
        masked = SIMPLE_FIELD_PATTERN.matcher(masked).replaceAll("$1" + MASK);
        masked = BEARER_PATTERN.matcher(masked).replaceAll("$1" + MASK);
        if (masked.length() > MAX_TEXT_LENGTH) {
            return masked.substring(0, MAX_TEXT_LENGTH) + "...";
        }
        return masked;
    }
}
