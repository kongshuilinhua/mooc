package com.elysia.mooc.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.enums.ReadStatus;
import com.elysia.mooc.message.domain.po.MessagePO;
import com.elysia.mooc.message.domain.vo.MessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 站内消息数据库访问接口。 */
@Mapper
public interface MessageMapper extends BaseMapper<MessagePO> {

    /**
     * 分页查询当前用户可见消息。
     *
     * @param page 分页参数
     * @param receiverId 当前登录用户 ID
     * @param type 消息类型筛选，允许为空
     * @param readStatus 已读状态筛选，允许为空
     * @return 当前用户消息分页
     */
    @Select("""
            <script>
            SELECT
                m.id AS id,
                m.message_type AS type,
                m.content AS content,
                mr.read_status AS isRead,
                COALESCE(mr.create_time, m.create_time) AS createTime
            FROM message_receiver mr
            INNER JOIN message m ON m.id = mr.message_id
            WHERE mr.receiver_id = #{receiverId}
              AND mr.deleted = 0
              AND m.deleted = 0
            <if test="type != null">
              AND m.message_type = #{type}
            </if>
            <if test="readStatus != null">
              AND mr.read_status = #{readStatus}
            </if>
            ORDER BY COALESCE(mr.create_time, m.create_time) DESC, mr.id DESC
            </script>
            """)
    Page<MessageVO> selectUserMessagePage(
            Page<MessageVO> page,
            @Param("receiverId") Long receiverId,
            @Param("type") MessageType type,
            @Param("readStatus") ReadStatus readStatus);
}
