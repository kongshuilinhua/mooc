package com.elysia.mooc.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 视频转码状态。 */
@Getter
@RequiredArgsConstructor
public enum MediaTranscodeStatus implements BaseEnum<String> {

    /** 待转码。 */
    PENDING("PENDING", "待转码"),

    /** 转码中。 */
    PROCESSING("PROCESSING", "转码中"),

    /** 转码成功。 */
    SUCCESS("SUCCESS", "转码成功"),

    /** 转码失败。 */
    FAILED("FAILED", "转码失败");

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
    public static MediaTranscodeStatus of(Object value) {
        return BaseEnum.parse(MediaTranscodeStatus.class, value);
    }
}
