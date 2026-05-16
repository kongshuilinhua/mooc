package com.elysia.mooc.ai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.admin.constants.AiAdminErrorCode;
import com.elysia.mooc.ai.admin.domain.dto.AiModelConfigQuery;
import com.elysia.mooc.ai.admin.domain.dto.UpdateModelConfigRequest;
import com.elysia.mooc.ai.admin.domain.po.AiModelConfigPO;
import com.elysia.mooc.ai.admin.domain.vo.AiModelConfigVO;
import com.elysia.mooc.ai.admin.domain.vo.AiUsageVO;
import com.elysia.mooc.ai.admin.domain.vo.DocumentStatusCountVO;
import com.elysia.mooc.ai.admin.domain.vo.DocumentStatusOverviewVO;
import com.elysia.mooc.ai.admin.mapper.AiModelConfigMapper;
import com.elysia.mooc.ai.admin.service.AiAdminService;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import com.elysia.mooc.ai.tool.domain.po.AiToolCallLogPO;
import com.elysia.mooc.ai.tool.mapper.AiToolCallLogMapper;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import com.elysia.mooc.knowledge.mapper.KnowledgeDocumentMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** AI 管理后台服务实现。 */
@Service
@RequiredArgsConstructor
public class AiAdminServiceImpl implements AiAdminService {

    private static final Pattern API_KEY_REF_PATTERN = Pattern.compile("[A-Z][A-Z0-9_]{2,127}");

    private static final Map<String, SFunction<AiModelConfigPO, ?>> MODEL_CONFIG_SORT_FIELDS = Map.of(
            "id", AiModelConfigPO::getId,
            "createTime", AiModelConfigPO::getCreateTime,
            "updateTime", AiModelConfigPO::getUpdateTime,
            "scene", AiModelConfigPO::getScene);

    private final AiModelConfigMapper aiModelConfigMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final AiMessageMapper aiMessageMapper;
    private final AiToolCallLogMapper aiToolCallLogMapper;

    /**
     * 分页查询模型配置。
     *
     * @param query 查询条件
     * @return 模型配置分页
     */
    @Override
    public PageResult<AiModelConfigVO> listModelConfigs(AiModelConfigQuery query) {
        AiModelConfigQuery safeQuery = query == null ? new AiModelConfigQuery() : query;
        LambdaQueryWrapper<AiModelConfigPO> wrapper = Wrappers.<AiModelConfigPO>lambdaQuery();
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(nested -> nested.like(AiModelConfigPO::getProvider, keyword)
                    .or()
                    .like(AiModelConfigPO::getModelName, keyword)
                    .or()
                    .like(AiModelConfigPO::getApiKeyRef, keyword)
                    .or()
                    .like(AiModelConfigPO::getScene, keyword));
        }
        if (safeQuery.getScene() != null) {
            wrapper.eq(AiModelConfigPO::getScene, safeQuery.getScene());
        }
        if (safeQuery.resolvedEnabled() != null) {
            wrapper.eq(AiModelConfigPO::getEnabled, safeQuery.resolvedEnabled());
        }
        applyModelConfigOrder(wrapper, safeQuery);
        Page<AiModelConfigPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return PageResult.of(aiModelConfigMapper.selectPage(page, wrapper), this::toModelConfigVO);
    }

    /**
     * 修改模型配置。
     *
     * @param id 模型配置 ID
     * @param request 修改请求
     * @return 修改后的模型配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiModelConfigVO updateModelConfig(Long id, UpdateModelConfigRequest request) {
        if (id == null || id <= 0) {
            throw new BizException(AiAdminErrorCode.AI_MODEL_CONFIG_NOT_FOUND);
        }
        if (request == null) {
            throw new BizException(AiAdminErrorCode.AI_ADMIN_PARAM_INVALID, "模型配置请求不能为空");
        }
        AiModelConfigPO po = aiModelConfigMapper.selectById(id);
        if (po == null) {
            throw new BizException(AiAdminErrorCode.AI_MODEL_CONFIG_NOT_FOUND);
        }

        // 只允许改运营可控字段，scene 由种子和业务场景决定，避免误改后聊天、RAG、Embedding 读取错配置。
        applyEditableFields(po, request);
        aiModelConfigMapper.updateById(po);
        return toModelConfigVO(aiModelConfigMapper.selectById(id));
    }

    /**
     * 查询知识库文档处理状态统计。
     *
     * @return 文档处理状态统计
     */
    @Override
    public DocumentStatusOverviewVO getDocumentStatusOverview() {
        Map<KnowledgeProcessStatus, Long> parseCounts = countDocumentsByStatus("parse_status");
        Map<KnowledgeProcessStatus, Long> embeddingCounts = countDocumentsByStatus("embedding_status");

        DocumentStatusOverviewVO vo = new DocumentStatusOverviewVO();
        vo.setTotalDocuments(countDocuments(null, null));
        vo.setParsePending(parseCounts.getOrDefault(KnowledgeProcessStatus.PENDING, 0L));
        vo.setParseProcessing(parseCounts.getOrDefault(KnowledgeProcessStatus.PROCESSING, 0L));
        vo.setParseSuccess(parseCounts.getOrDefault(KnowledgeProcessStatus.SUCCESS, 0L));
        vo.setParseFailed(parseCounts.getOrDefault(KnowledgeProcessStatus.FAILED, 0L));
        vo.setEmbeddingPending(embeddingCounts.getOrDefault(KnowledgeProcessStatus.PENDING, 0L));
        vo.setEmbeddingProcessing(embeddingCounts.getOrDefault(KnowledgeProcessStatus.PROCESSING, 0L));
        vo.setEmbeddingSuccess(embeddingCounts.getOrDefault(KnowledgeProcessStatus.SUCCESS, 0L));
        vo.setEmbeddingFailed(embeddingCounts.getOrDefault(KnowledgeProcessStatus.FAILED, 0L));
        vo.setTotalSegments(sumDocumentSegments());
        vo.setParseStatusCounts(toStatusCounts(parseCounts));
        vo.setEmbeddingStatusCounts(toStatusCounts(embeddingCounts));
        return vo;
    }

    /**
     * 查询 AI 调用统计。
     *
     * @return AI 调用统计
     */
    @Override
    public AiUsageVO getUsage() {
        AiUsageVO vo = new AiUsageVO();
        vo.setMessageCount(countMessages(null, null));
        vo.setUserMessageCount(countMessages(AiMessageRole.USER, null));
        vo.setAssistantMessageCount(countMessages(AiMessageRole.ASSISTANT, null));
        vo.setSuccessMessageCount(countMessages(null, AiMessageStatus.SUCCESS));
        vo.setFailedMessageCount(countMessages(null, AiMessageStatus.FAILED));
        vo.setPromptTokens(sumMessageField("prompt_tokens"));
        vo.setCompletionTokens(sumMessageField("completion_tokens"));
        vo.setTotalTokens(sumMessageField("total_tokens"));
        vo.setToolCallCount(countToolCalls(null));
        vo.setToolSuccessCount(countToolCalls(ToolCallStatus.SUCCESS));
        vo.setToolFailedCount(countToolCalls(ToolCallStatus.FAILED));
        vo.setAverageToolCostMs(avgToolCostMs());
        vo.setDocumentCount(countDocuments(null, null));
        vo.setParsedDocumentCount(countDocuments(KnowledgeProcessStatus.SUCCESS, null));
        vo.setEmbeddedDocumentCount(countDocuments(null, KnowledgeProcessStatus.SUCCESS));
        return vo;
    }

    private void applyEditableFields(AiModelConfigPO po, UpdateModelConfigRequest request) {
        if (StringUtils.hasText(request.getProvider())) {
            po.setProvider(request.getProvider().trim().toUpperCase());
        }
        if (StringUtils.hasText(request.getModelName())) {
            po.setModelName(request.getModelName().trim());
        }
        if (StringUtils.hasText(request.getBaseUrl())) {
            po.setBaseUrl(request.getBaseUrl().trim());
        }
        if (request.getApiKeyRef() != null) {
            po.setApiKeyRef(resolveApiKeyRef(request.getApiKeyRef()));
        }
        if (request.getTemperature() != null) {
            po.setTemperature(request.getTemperature());
        }
        if (request.getTopK() != null) {
            po.setTopK(request.getTopK());
        }
        EnableStatus enabled = resolveEnabled(request);
        if (enabled != null) {
            po.setEnabled(enabled);
        }
    }

    private String resolveApiKeyRef(String apiKeyRef) {
        String text = apiKeyRef.trim();
        if (text.isEmpty()) {
            return null;
        }
        if (!API_KEY_REF_PATTERN.matcher(text).matches()) {
            throw new BizException(AiAdminErrorCode.AI_ADMIN_PARAM_INVALID, "API Key 引用只能填写环境变量名，不能填写真实密钥");
        }
        return text;
    }

    private EnableStatus resolveEnabled(UpdateModelConfigRequest request) {
        if (request.getEnabled() != null) {
            return request.getEnabled();
        }
        if (request.getEnable() != null) {
            return Boolean.TRUE.equals(request.getEnable()) ? EnableStatus.ENABLED : EnableStatus.DISABLED;
        }
        return null;
    }

    private AiModelConfigVO toModelConfigVO(AiModelConfigPO po) {
        return BeanCopyUtils.copyBean(po, AiModelConfigVO.class, (source, target) -> {
            target.setStatus(source.getEnabled());
            target.setApiKeyConfigured(StringUtils.hasText(source.getApiKeyRef()));
        });
    }

    private void applyModelConfigOrder(
            LambdaQueryWrapper<AiModelConfigPO> wrapper,
            AiModelConfigQuery query) {
        String sortBy = StringUtils.hasText(query.getSortBy()) ? query.getSortBy() : "createTime";
        SFunction<AiModelConfigPO, ?> sortField = MODEL_CONFIG_SORT_FIELDS.getOrDefault(
                sortBy, AiModelConfigPO::getCreateTime);
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        wrapper.orderBy(true, asc, sortField);
        if (!"id".equals(sortBy)) {
            wrapper.orderByDesc(AiModelConfigPO::getId);
        }
    }

    private Map<KnowledgeProcessStatus, Long> countDocumentsByStatus(String column) {
        Map<KnowledgeProcessStatus, Long> result = new LinkedHashMap<>();
        knowledgeDocumentMapper.selectMaps(Wrappers.<KnowledgeDocumentPO>query()
                        .select(column + " AS status", "COUNT(*) AS total")
                        .groupBy(column))
                .forEach(row -> result.put(toKnowledgeProcessStatus(row.get("status")), toLong(row.get("total"))));
        return result;
    }

    private List<DocumentStatusCountVO> toStatusCounts(Map<KnowledgeProcessStatus, Long> counts) {
        return Arrays.stream(KnowledgeProcessStatus.values())
                .map(status -> new DocumentStatusCountVO(
                        status,
                        status.getDesc(),
                        counts.getOrDefault(status, 0L)))
                .toList();
    }

    private Long sumDocumentSegments() {
        Object value = knowledgeDocumentMapper.selectObjs(Wrappers.<KnowledgeDocumentPO>query()
                        .select("COALESCE(SUM(segment_count), 0)"))
                .stream()
                .findFirst()
                .orElse(0L);
        return toLong(value);
    }

    private Long countDocuments(KnowledgeProcessStatus parseStatus, KnowledgeProcessStatus embeddingStatus) {
        LambdaQueryWrapper<KnowledgeDocumentPO> wrapper = Wrappers.<KnowledgeDocumentPO>lambdaQuery();
        if (parseStatus != null) {
            wrapper.eq(KnowledgeDocumentPO::getParseStatus, parseStatus);
        }
        if (embeddingStatus != null) {
            wrapper.eq(KnowledgeDocumentPO::getEmbeddingStatus, embeddingStatus);
        }
        return knowledgeDocumentMapper.selectCount(wrapper);
    }

    private Long countMessages(AiMessageRole role, AiMessageStatus status) {
        LambdaQueryWrapper<AiMessagePO> wrapper = Wrappers.<AiMessagePO>lambdaQuery();
        if (role != null) {
            wrapper.eq(AiMessagePO::getRole, role);
        }
        if (status != null) {
            wrapper.eq(AiMessagePO::getStatus, status);
        }
        return aiMessageMapper.selectCount(wrapper);
    }

    private Long sumMessageField(String column) {
        QueryWrapper<AiMessagePO> wrapper = Wrappers.<AiMessagePO>query()
                .select("COALESCE(SUM(" + column + "), 0)");
        Object value = aiMessageMapper.selectObjs(wrapper).stream().findFirst().orElse(0L);
        return toLong(value);
    }

    private Long countToolCalls(ToolCallStatus status) {
        LambdaQueryWrapper<AiToolCallLogPO> wrapper = Wrappers.<AiToolCallLogPO>lambdaQuery();
        if (status != null) {
            wrapper.eq(AiToolCallLogPO::getStatus, status);
        }
        return aiToolCallLogMapper.selectCount(wrapper);
    }

    private Double avgToolCostMs() {
        Object value = aiToolCallLogMapper.selectObjs(Wrappers.<AiToolCallLogPO>query()
                        .select("COALESCE(AVG(cost_ms), 0)"))
                .stream()
                .findFirst()
                .orElse(BigDecimal.ZERO);
        if (value instanceof BigDecimal decimal) {
            return decimal.doubleValue();
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private KnowledgeProcessStatus toKnowledgeProcessStatus(Object value) {
        if (value instanceof KnowledgeProcessStatus status) {
            return status;
        }
        return KnowledgeProcessStatus.of(value);
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? 0L : Long.parseLong(String.valueOf(value));
    }
}
