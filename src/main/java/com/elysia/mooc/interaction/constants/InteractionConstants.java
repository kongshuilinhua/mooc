package com.elysia.mooc.interaction.constants;

/** 互动模块常量。 */
public final class InteractionConstants {

    /** 互动创建权限编码。 */
    public static final String PERMISSION_CREATE = "interaction:create";

    /** 互动管理权限编码。 */
    public static final String PERMISSION_MANAGE = "interaction:manage";

    /** 管理员角色编码。 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 问题和评价内容最大长度。 */
    public static final int MAX_CONTENT_LENGTH = 1000;

    /** 举报详情最大长度。 */
    public static final int MAX_REPORT_DETAIL_LENGTH = 1000;

    private InteractionConstants() {
    }
}
