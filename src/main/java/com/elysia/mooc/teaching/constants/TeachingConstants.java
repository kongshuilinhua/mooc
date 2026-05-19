package com.elysia.mooc.teaching.constants;

/** 教师看板模块常量。 */
public final class TeachingConstants {

    /** 教师角色编码。 */
    public static final String ROLE_TEACHER = "TEACHER";

    /** 默认统计天数，包含当天。 */
    public static final int DEFAULT_STAT_DAYS = 7;

    /** 未能查询到学生姓名时的兜底展示。 */
    public static final String UNKNOWN_STUDENT_NAME = "未知学员";

    /** 教师看板统计说明。 */
    public static final String STAT_NOTE = "统计来源：teacher_course_stat 与 teacher_revenue_stat，默认日期范围为最近7天";

    private TeachingConstants() {
    }
}
