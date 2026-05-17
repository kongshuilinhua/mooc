package com.elysia.mooc.common.idempotent.service;

import com.elysia.mooc.common.idempotent.domain.po.IdempotentRecordPO;
import java.time.LocalDateTime;

/** 幂等记录服务。 */
public interface IdempotentService {

    /**
     * 构建落库幂等键。
     *
     * @param bizType 业务类型
     * @param userId 当前用户 ID
     * @param rawKey 请求头原始 key
     * @return 组合后的幂等键
     */
    String buildRecordKey(String bizType, Long userId, String rawKey);

    /**
     * 尝试创建处理中记录。
     *
     * @param recordKey 幂等键
     * @param bizType 业务类型
     * @param bizId 业务 ID
     * @param requestHash 请求摘要
     * @param expireTime 过期时间
     * @return 新建或已存在的记录
     */
    IdempotentRecordPO tryCreateProcessing(
            String recordKey,
            String bizType,
            String bizId,
            String requestHash,
            LocalDateTime expireTime);

    /**
     * 保存成功响应快照。
     *
     * @param id 记录 ID
     * @param responseBody 响应快照
     */
    void saveSuccessResponse(Long id, String responseBody);

    /**
     * 标记请求处理失败。
     *
     * @param id 记录 ID
     * @param errorMessage 错误摘要
     */
    void saveFailure(Long id, String errorMessage);
}
