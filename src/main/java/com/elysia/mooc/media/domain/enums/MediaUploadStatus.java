package com.elysia.mooc.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 媒资上传状态。 */
@Getter
@RequiredArgsConstructor
public enum MediaUploadStatus implements BaseEnum<String> {

    /** 上传中。 */
    UPLOADING("UPLOADING", "上传中"),

    /** 上传成功。 */
    SUCCESS("SUCCESS", "上传成功"),

    /** 上传失败。 */
    FAILED("FAILED", "上传失败"),

    /** 已删除。 */
    DELETED("DELETED", "已删除");

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
    public static MediaUploadStatus of(Object value) {
        return BaseEnum.parse(MediaUploadStatus.class, value);
    }
}
