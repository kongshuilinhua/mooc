package com.elysia.mooc.course.constants;

/** 课程模块常量。 */
public final class CourseConstants {

    /** 教师单次绑定标签上限。 */
    public static final int MAX_TAG_BIND_SIZE = 10;

    /** 教师角色编码。 */
    public static final String ROLE_TEACHER = "TEACHER";

    /** 管理员角色编码。 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 创建课程所需权限编码。 */
    public static final String PERMISSION_COURSE_PUBLISH = "course:publish";

    /** 未知讲师兜底展示名。 */
    public static final String UNKNOWN_TEACHER_NAME = "未知讲师";

    private CourseConstants() {
    }
}
