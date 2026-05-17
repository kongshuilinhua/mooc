package com.elysia.mooc.homework.constants;

/** 作业模块常量。 */
public final class HomeworkConstants {

    /** 模块编码。 */
    public static final String MODULE_CODE = "homework";

    /** 管理员角色编码。 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 教师角色编码。 */
    public static final String ROLE_TEACHER = "TEACHER";

    /** 学生角色编码。 */
    public static final String ROLE_STUDENT = "STUDENT";

    /** 教师作业管理权限编码。 */
    public static final String PERMISSION_HOMEWORK_MANAGE = "homework:manage";

    /** 学生作业提交权限编码。 */
    public static final String PERMISSION_HOMEWORK_SUBMIT = "homework:submit";

    /** 单次提交附件数量上限。 */
    public static final int MAX_ATTACHMENT_SIZE = 10;

    private HomeworkConstants() {
    }
}
