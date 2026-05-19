package com.elysia.mooc.task.constants;

/** 学习任务模块常量。 */
public final class StudyTaskConstants {

    private StudyTaskConstants() {
    }

    /** 学生角色编码。 */
    public static final String ROLE_STUDENT = "STUDENT";

    /** 管理员角色编码。 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 调度任务权限编码。 */
    public static final String PERMISSION_TASK_DISPATCH = "study:task:dispatch";

    /** 默认批次大小。 */
    public static final int DEFAULT_BATCH_SIZE = 100;

    /** 最大批次大小。 */
    public static final int MAX_BATCH_SIZE = 500;

    /** 任务标题最大长度。 */
    public static final int TASK_TITLE_MAX_LENGTH = 200;

    /** 完成说明最大长度。 */
    public static final int COMPLETE_NOTE_MAX_LENGTH = 500;

    /** 默认提醒时间。 */
    public static final String DEFAULT_REMINDER_TIME = "09:00:00";
}
