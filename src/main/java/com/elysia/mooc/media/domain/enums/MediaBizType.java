package com.elysia.mooc.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 媒资业务类型。 */
@Getter
@RequiredArgsConstructor
public enum MediaBizType implements BaseEnum<String> {

    /** 课程视频。 */
    COURSE_VIDEO("COURSE_VIDEO", "课程视频"),

    /** 课程封面。 */
    COURSE_COVER("COURSE_COVER", "课程封面"),

    /** 知识库文档。 */
    KNOWLEDGE_DOC("KNOWLEDGE_DOC", "知识库文档");

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
    public static MediaBizType of(Object value) {
        if (value instanceof String text) {
            return BaseEnum.parse(MediaBizType.class, text.replace('-', '_').toUpperCase());
        }
        return BaseEnum.parse(MediaBizType.class, value);
    }
}
