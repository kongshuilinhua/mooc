package com.elysia.mooc.ai.chat.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 消息处理状态。 */
@Getter
@RequiredArgsConstructor
public enum AiMessageStatus implements BaseEnum<String> {

    /** 已成功生成。 */
    SUCCESS("SUCCESS", "成功"),

    /** 生成失败。 */
    FAILED("FAILED", "失败"),

    /** 流式生成中，day17 后续复用。 */
    STREAMING("STREAMING", "生成中"),

    /** 内容被拦截，day32 后续复用。 */
    BLOCKED("BLOCKED", "已拦截");

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
    public static AiMessageStatus of(Object value) {
        return BaseEnum.parse(AiMessageStatus.class, value);
    }
}
