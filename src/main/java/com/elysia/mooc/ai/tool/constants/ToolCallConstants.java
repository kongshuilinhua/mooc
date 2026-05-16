package com.elysia.mooc.ai.tool.constants;

/** Tool Calling 模块常量。 */
public final class ToolCallConstants {

    /** 工具调用日志查看权限。 */
    public static final String PERMISSION_TOOL_LOG_VIEW = "ai:tool-log:view";

    /** 管理员角色编码。 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 教师角色编码。 */
    public static final String ROLE_TEACHER = "TEACHER";

    /** 默认课程搜索数量。 */
    public static final int DEFAULT_COURSE_LIMIT = 5;

    /** 最大工具结果摘要长度。 */
    public static final int MAX_RESULT_SUMMARY_LENGTH = 500;

    private ToolCallConstants() {
    }
}
