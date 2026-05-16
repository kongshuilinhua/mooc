package com.elysia.mooc.exam.constants;

/** 考试模块常量。 */
public final class ExamConstants {

    /** 管理员角色编码。 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 教师角色编码。 */
    public static final String ROLE_TEACHER = "TEACHER";

    /** 单次创建试卷最多绑定题目数量。 */
    public static final int MAX_PAPER_QUESTION_SIZE = 100;

    /** 单次提交最多答案数量。 */
    public static final int MAX_SUBMIT_ANSWER_SIZE = 100;

    private ExamConstants() {
    }
}
