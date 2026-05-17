package com.elysia.mooc.statistics.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 用户搜索日志实体，映射 user_search_log 表。 */
@Data
@TableName("user_search_log")
public class UserSearchLogPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID，匿名搜索为空。 */
    private Long userId;

    /** 搜索关键词。 */
    private String keyword;

    /** 结果数量。 */
    private Integer resultCount;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
