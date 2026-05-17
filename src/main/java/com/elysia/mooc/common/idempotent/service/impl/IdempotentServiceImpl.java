package com.elysia.mooc.common.idempotent.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.common.idempotent.domain.enums.IdempotentStatus;
import com.elysia.mooc.common.idempotent.domain.po.IdempotentRecordPO;
import com.elysia.mooc.common.idempotent.mapper.IdempotentRecordMapper;
import com.elysia.mooc.common.idempotent.service.IdempotentService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 幂等记录服务实现。 */
@Service
@RequiredArgsConstructor
public class IdempotentServiceImpl implements IdempotentService {

    private static final int MAX_RECORD_KEY_LENGTH = 128;

    private final IdempotentRecordMapper idempotentRecordMapper;

    @Override
    public String buildRecordKey(String bizType, Long userId, String rawKey) {
        String safeUserId = userId == null ? "anonymous" : String.valueOf(userId);
        String composite = bizType + ":" + safeUserId + ":" + rawKey.trim();
        if (composite.length() <= MAX_RECORD_KEY_LENGTH) {
            return composite;
        }
        return bizType + ":" + safeUserId + ":" + sha256(composite);
    }

    @Override
    public IdempotentRecordPO tryCreateProcessing(
            String recordKey,
            String bizType,
            String bizId,
            String requestHash,
            LocalDateTime expireTime) {
        IdempotentRecordPO record = new IdempotentRecordPO();
        record.setIdempotentKey(recordKey);
        record.setBizType(bizType);
        record.setBizId(StringUtils.hasText(bizId) ? bizId : null);
        record.setRequestHash(requestHash);
        record.setStatus(IdempotentStatus.PROCESSING);
        record.setExpireTime(expireTime);
        record.setDeleted(0);
        try {
            idempotentRecordMapper.insert(record);
            record.setNewlyCreated(true);
            return record;
        } catch (DuplicateKeyException ignored) {
            IdempotentRecordPO existed = findByRecordKey(recordKey);
            if (existed != null) {
                existed.setNewlyCreated(false);
            }
            return existed;
        }
    }

    @Override
    public void saveSuccessResponse(Long id, String responseBody) {
        IdempotentRecordPO update = new IdempotentRecordPO();
        update.setId(id);
        update.setStatus(IdempotentStatus.SUCCESS);
        update.setResponseBody(responseBody);
        update.setUpdateTime(LocalDateTime.now());
        idempotentRecordMapper.updateById(update);
    }

    @Override
    public void saveFailure(Long id, String errorMessage) {
        IdempotentRecordPO update = new IdempotentRecordPO();
        update.setId(id);
        update.setStatus(IdempotentStatus.FAILED);
        update.setResponseBody(errorMessage);
        update.setUpdateTime(LocalDateTime.now());
        idempotentRecordMapper.updateById(update);
    }

    private IdempotentRecordPO findByRecordKey(String recordKey) {
        return idempotentRecordMapper.selectOne(Wrappers.<IdempotentRecordPO>lambdaQuery()
                .eq(IdempotentRecordPO::getIdempotentKey, recordKey));
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("幂等键摘要生成失败", ex);
        }
    }
}
