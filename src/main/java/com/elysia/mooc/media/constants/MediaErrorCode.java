package com.elysia.mooc.media.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 媒资模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum MediaErrorCode implements ErrorCode {

    /** 上传参数错误。 */
    MEDIA_PARAM_INVALID(6001, "媒资参数不正确"),

    /** 文件为空。 */
    MEDIA_FILE_EMPTY(6002, "上传文件不能为空"),

    /** 文件类型不允许。 */
    MEDIA_TYPE_NOT_ALLOWED(6003, "文件类型不允许"),

    /** 文件大小超限。 */
    MEDIA_SIZE_EXCEEDED(6004, "文件大小超过限制"),

    /** 文件不存在。 */
    MEDIA_FILE_NOT_FOUND(6005, "媒资文件不存在"),

    /** 无权限操作媒资。 */
    MEDIA_FORBIDDEN(6006, "无权限操作该媒资"),

    /** 分片不存在或不完整。 */
    MEDIA_CHUNK_INCOMPLETE(6007, "文件分片不完整"),

    /** 存储失败。 */
    MEDIA_STORAGE_FAILED(6008, "文件存储失败"),

    /** 媒资已被引用。 */
    MEDIA_IN_USE(6009, "媒资已被课程小节引用，不能删除");

    private final int code;
    private final String message;

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return switch (this) {
            case MEDIA_FILE_NOT_FOUND -> 404;
            case MEDIA_FORBIDDEN -> 403;
            case MEDIA_CHUNK_INCOMPLETE, MEDIA_IN_USE -> 409;
            default -> 400;
        };
    }
}
