package com.elysia.mooc.ai.chat.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** AI 会话分页查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiConversationQuery extends PageQuery {

    /** 搜索关键字，匹配会话标题。 */
    private String keyword;

    /** 排序字段白名单：createTime、updateTime、lastMessageTime、id。 */
    private String sortBy;

    /** 是否升序，默认倒序。 */
    private Boolean isAsc;
}
