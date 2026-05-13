package com.elysia.mooc.media.constants;

/** 媒资模块常量。 */
public final class MediaConstants {

    /** 文件 hash 算法。 */
    public static final String HASH_ALGORITHM = "SHA-256";

    /** 分片临时目录名。 */
    public static final String CHUNK_DIR = "_chunks";

    /** 教师角色编码。 */
    public static final String ROLE_TEACHER = "TEACHER";

    /** 管理员角色编码。 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 教师媒资上传权限编码。 */
    public static final String PERMISSION_COURSE_PUBLISH = "course:publish";

    private MediaConstants() {
    }
}
