package com.elysia.mooc.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 文档解析状态。 */
@Getter
@RequiredArgsConstructor
public enum MediaParseStatus implements BaseEnum<String> {

    /** 待解析。 */
    PENDING("PENDING", "待解析"),

    /** 安全扫描中。 */
    SCANNING("SCANNING", "安全扫描中"),

    /** 解析中。 */
    PROCESSING("PROCESSING", "解析中"),

    /** 解析成功。 */
    SUCCESS("SUCCESS", "解析成功"),

    /** 解析失败。 */
    FAILED("FAILED", "解析失败"),

    /** 安全扫描拒绝。 */
    REJECTED("REJECTED", "安全扫描拒绝");

    /** 落库和接口输出值。 */
    @EnumValue
    private final String value;

    /** 中文说明。 */
    private final String desc;

    @JsonValue
    @Override
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MediaParseStatus of(Object value) {
        return BaseEnum.parse(MediaParseStatus.class, value);
    }
}
