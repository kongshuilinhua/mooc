package com.elysia.mooc.ops.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.ops.constants.OpsConfigConstants;
import com.elysia.mooc.ops.constants.OpsConfigErrorCode;
import com.elysia.mooc.ops.domain.dto.CreateExportJobRequest;
import com.elysia.mooc.ops.domain.dto.CreateReviewTaskRequest;
import com.elysia.mooc.ops.domain.dto.UpdateConfigItemRequest;
import com.elysia.mooc.ops.domain.enums.OpsConfigStatus;
import com.elysia.mooc.ops.domain.enums.OpsConfigValueType;
import com.elysia.mooc.ops.domain.enums.OpsExportFormat;
import com.elysia.mooc.ops.domain.enums.OpsExportJobStatus;
import com.elysia.mooc.ops.domain.enums.OpsReviewStatus;
import com.elysia.mooc.ops.domain.po.OpsConfigItemPO;
import com.elysia.mooc.ops.domain.po.OpsExportJobPO;
import com.elysia.mooc.ops.domain.po.OpsReviewTaskPO;
import com.elysia.mooc.ops.domain.vo.ConfigItemResultVO;
import com.elysia.mooc.ops.domain.vo.ExportTaskResultVO;
import com.elysia.mooc.ops.domain.vo.ReviewTaskResultVO;
import com.elysia.mooc.ops.mapper.OpsConfigItemMapper;
import com.elysia.mooc.ops.mapper.OpsExportJobMapper;
import com.elysia.mooc.ops.mapper.OpsReviewTaskMapper;
import com.elysia.mooc.ops.service.OpsConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 运营配置、审核任务和导出任务服务实现。 */
@Service
@RequiredArgsConstructor
public class OpsConfigServiceImpl implements OpsConfigService {

    private final OpsReviewTaskMapper reviewTaskMapper;
    private final OpsExportJobMapper exportJobMapper;
    private final OpsConfigItemMapper configItemMapper;
    private final UserContextService userContextService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewTaskResultVO createReviewTask(CreateReviewTaskRequest request) {
        request.check();
        Long currentUserId = userContextService.currentUserId();
        Long submitUserId = request.getSubmitUserId() == null ? currentUserId : request.getSubmitUserId();
        ensureNoPendingReview(request.getBizType(), request.getBizId());

        OpsReviewTaskPO entity = new OpsReviewTaskPO();
        entity.setBizType(request.getBizType());
        entity.setBizId(request.getBizId());
        entity.setSubmitUserId(submitUserId);
        entity.setReviewStatus(OpsReviewStatus.PENDING);
        entity.setReviewReason(buildReviewReason(request));
        entity.setDeleted(0);
        reviewTaskMapper.insert(entity);

        ReviewTaskResultVO result = new ReviewTaskResultVO();
        result.setReviewId(entity.getId());
        result.setTargetType(entity.getBizType());
        result.setTargetId(String.valueOf(entity.getBizId()));
        result.setStatus(entity.getReviewStatus());
        result.setReason(request.getReason());
        result.setPriority(request.getPriority());
        result.setCreatedAt(entity.getCreateTime() == null ? LocalDateTime.now() : entity.getCreateTime());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExportTaskResultVO createExportJob(CreateExportJobRequest request) {
        request.check();
        Long currentUserId = userContextService.currentUserId();
        OpsExportJobPO entity = new OpsExportJobPO();
        entity.setJobType(request.getJobType());
        entity.setRequestUserId(currentUserId);
        entity.setRequestParams(toRequestParams(request));
        entity.setJobStatus(OpsExportJobStatus.PENDING);
        entity.setDeleted(0);
        exportJobMapper.insert(entity);

        ExportTaskResultVO result = new ExportTaskResultVO();
        result.setExportId(entity.getId());
        result.setExportType(entity.getJobType());
        result.setStatus(entity.getJobStatus());
        result.setFileName(buildExportFileName(request));
        result.setFileUrl(entity.getFileUrl());
        result.setCreatedAt(entity.getCreateTime() == null ? LocalDateTime.now() : entity.getCreateTime());
        result.setFinishedAt(entity.getFinishTime());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigItemResultVO updateConfigItem(String configKey, UpdateConfigItemRequest request) {
        request.check();
        String normalizedKey = normalizeConfigKey(configKey);
        OpsConfigItemPO existing = configItemMapper.selectOne(Wrappers.<OpsConfigItemPO>lambdaQuery()
                .eq(OpsConfigItemPO::getConfigKey, normalizedKey)
                .eq(OpsConfigItemPO::getDeleted, 0)
                .last("LIMIT 1"));

        OpsConfigValueType valueType = resolveValueType(request, existing);
        validateConfigValue(request.getConfigValue(), valueType);

        if (existing == null) {
            existing = new OpsConfigItemPO();
            existing.setConfigKey(normalizedKey);
            existing.setDeleted(0);
        }
        existing.setConfigGroup(resolveConfigGroup(request, existing, normalizedKey));
        existing.setConfigValue(request.getConfigValue());
        existing.setValueType(valueType);
        existing.setStatus(request.getStatus() == null
                ? (existing.getStatus() == null ? OpsConfigStatus.ENABLED : existing.getStatus())
                : request.getStatus());

        if (existing.getId() == null) {
            configItemMapper.insert(existing);
        } else {
            configItemMapper.updateById(existing);
        }

        ConfigItemResultVO result = new ConfigItemResultVO();
        result.setConfigKey(existing.getConfigKey());
        result.setConfigGroup(existing.getConfigGroup());
        result.setValue(existing.getConfigValue());
        result.setValueType(existing.getValueType());
        result.setStatus(existing.getStatus());
        result.setRemark(request.getRemark());
        result.setUpdatedAt(existing.getUpdateTime() == null ? LocalDateTime.now() : existing.getUpdateTime());
        return result;
    }

    private void ensureNoPendingReview(String bizType, Long bizId) {
        Long count = reviewTaskMapper.selectCount(Wrappers.<OpsReviewTaskPO>lambdaQuery()
                .eq(OpsReviewTaskPO::getBizType, bizType)
                .eq(OpsReviewTaskPO::getBizId, bizId)
                .eq(OpsReviewTaskPO::getReviewStatus, OpsReviewStatus.PENDING)
                .eq(OpsReviewTaskPO::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BizException(OpsConfigErrorCode.OPS_REVIEW_PENDING_DUPLICATED);
        }
    }

    private String buildReviewReason(CreateReviewTaskRequest request) {
        String reason = StringUtils.hasText(request.getReviewReason()) ? request.getReviewReason().trim() : "后台创建审核任务";
        return "优先级：" + request.getPriority().getValue() + "；说明：" + reason;
    }

    private String toRequestParams(CreateExportJobRequest request) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("exportType", request.getJobType().getValue());
        params.put("bizDate", request.getBizDate());
        params.put("format", request.getFormat().getValue());
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "导出参数序列化失败");
        }
    }

    private String buildExportFileName(CreateExportJobRequest request) {
        String extension = request.getFormat() == OpsExportFormat.CSV
                ? OpsConfigConstants.EXPORT_FILE_EXTENSION_CSV
                : OpsConfigConstants.EXPORT_FILE_EXTENSION_XLSX;
        String dateText = request.getBizDate() == null ? "all" : request.getBizDate().toString();
        return request.getJobType().getValue().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + dateText + "." + extension;
    }

    private String normalizeConfigKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            throw new BizException(OpsConfigErrorCode.OPS_CONFIG_KEY_INVALID);
        }
        String normalizedKey = configKey.trim();
        if (normalizedKey.length() > OpsConfigConstants.CONFIG_KEY_MAX_LENGTH
                || !normalizedKey.matches("[A-Za-z0-9._:-]+")) {
            throw new BizException(OpsConfigErrorCode.OPS_CONFIG_KEY_INVALID);
        }
        return normalizedKey;
    }

    private String resolveConfigGroup(UpdateConfigItemRequest request, OpsConfigItemPO existing, String configKey) {
        if (StringUtils.hasText(request.getConfigGroup())) {
            return request.getConfigGroup();
        }
        if (existing.getId() != null && StringUtils.hasText(existing.getConfigGroup())) {
            return existing.getConfigGroup();
        }
        int dotIndex = configKey.indexOf('.');
        if (dotIndex > 0) {
            return configKey.substring(0, dotIndex).trim().toUpperCase(Locale.ROOT);
        }
        return OpsConfigConstants.DEFAULT_CONFIG_GROUP;
    }

    private OpsConfigValueType resolveValueType(UpdateConfigItemRequest request, OpsConfigItemPO existing) {
        if (request.getValueType() != null) {
            return request.getValueType();
        }
        if (existing != null && existing.getValueType() != null) {
            return existing.getValueType();
        }
        return inferValueType(request.getConfigValue());
    }

    private OpsConfigValueType inferValueType(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return OpsConfigValueType.BOOLEAN;
        }
        try {
            new BigDecimal(value);
            return OpsConfigValueType.NUMBER;
        } catch (NumberFormatException ignored) {
            // 不是数字时继续按 JSON 或字符串判断。
        }
        if (value != null && (value.trim().startsWith("{") || value.trim().startsWith("["))) {
            return OpsConfigValueType.JSON;
        }
        return OpsConfigValueType.STRING;
    }

    private void validateConfigValue(String value, OpsConfigValueType valueType) {
        if (valueType == OpsConfigValueType.NUMBER) {
            try {
                new BigDecimal(value);
            } catch (NumberFormatException ex) {
                throw new BizException(OpsConfigErrorCode.OPS_CONFIG_VALUE_INVALID, "数字类型配置必须填写合法数字");
            }
            return;
        }
        if (valueType == OpsConfigValueType.BOOLEAN
                && !"true".equalsIgnoreCase(value)
                && !"false".equalsIgnoreCase(value)) {
            throw new BizException(OpsConfigErrorCode.OPS_CONFIG_VALUE_INVALID, "布尔类型配置只能填写 true 或 false");
        }
        if (valueType == OpsConfigValueType.JSON) {
            try {
                objectMapper.readTree(value);
            } catch (JsonProcessingException ex) {
                throw new BizException(OpsConfigErrorCode.OPS_CONFIG_VALUE_INVALID, "JSON 类型配置必须填写合法 JSON");
            }
        }
    }
}
