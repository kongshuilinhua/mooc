package com.elysia.mooc.ai.chat.constants;

/** AI 普通聊天模块常量。 */
public final class AiChatConstants {

    /** 普通聊天权限点。 */
    public static final String PERMISSION_AI_CHAT = "ai:chat";

    /** 管理员角色编码。 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 最近上下文窗口大小，避免长会话无限拼接拖慢模型调用。 */
    public static final int DEFAULT_CONTEXT_MESSAGE_LIMIT = 10;

    /** 默认会话标题最大长度。 */
    public static final int TITLE_MAX_LENGTH = 28;

    private AiChatConstants() {
    }
}
