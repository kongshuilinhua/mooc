package com.elysia.mooc.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.service.EventPublisher;
import com.elysia.mooc.knowledge.constants.KnowledgeConstants;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.payload.KnowledgeEmbeddingRequestedPayload;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeSegmentPO;
import com.elysia.mooc.knowledge.embedding.EmbeddingClient;
import com.elysia.mooc.knowledge.embedding.EmbeddingProperties;
import com.elysia.mooc.knowledge.embedding.EmbeddingRequest;
import com.elysia.mooc.knowledge.embedding.EmbeddingResult;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeDocumentMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeSegmentMapper;
import com.elysia.mooc.knowledge.qdrant.QdrantClient;
import com.elysia.mooc.knowledge.qdrant.QdrantPointPayload;
import com.elysia.mooc.knowledge.qdrant.QdrantProperties;
import com.elysia.mooc.knowledge.service.EmbeddingService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 知识库向量化服务实现。 */
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final int MAX_ERROR_LENGTH = 1000;

    private final UserContextService userContextService;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeSegmentMapper knowledgeSegmentMapper;
    private final EmbeddingProperties embeddingProperties;
    private final QdrantProperties qdrantProperties;
    private final EmbeddingClient embeddingClient;
    private final QdrantClient qdrantClient;
    private final EventPublisher eventPublisher;

    /**
     * 重建单个切片向量。
     *
     * @param segmentId 切片 ID
     * @return true 表示成功
     */
    @Override
    public Boolean rebuildSegment(Long segmentId) {
        LoginUser loginUser = requireManagePermission();
        KnowledgeSegmentPO segment = getSegment(segmentId);
        embedSegment(segment, true);
        refreshDocumentEmbeddingStatus(segment.getDocumentId());
        publishEmbeddingCompleted(segment.getDocumentId(), loginUser.getUserId());
        return true;
    }

    /**
     * 重建文档下全部切片向量。
     *
     * @param documentId 文档 ID
     * @return true 表示成功
     */
    @Override
    public Boolean rebuildDocument(Long documentId) {
        LoginUser loginUser = requireManagePermission();
        embedDocument(documentId, true, loginUser.getUserId());
        return true;
    }

    /**
     * 消费向量化事件，对待处理或失败切片生成向量。
     *
     * @param payload 向量化请求载荷
     */
    @Override
    public void handleEmbeddingRequested(KnowledgeEmbeddingRequestedPayload payload) {
        if (payload == null || payload.documentId() == null) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "向量化事件缺少文档ID");
        }
        embedDocument(payload.documentId(), false, payload.operatorId());
    }

    private void embedDocument(Long documentId, boolean force, Long operatorId) {
        KnowledgeDocumentPO document = getDocument(documentId);
        getEnabledKnowledgeBase(document.getKbId());
        if (document.getParseStatus() != KnowledgeProcessStatus.SUCCESS) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "文档尚未解析成功，不能生成向量");
        }

        List<KnowledgeSegmentPO> segments = knowledgeSegmentMapper.selectList(Wrappers.<KnowledgeSegmentPO>lambdaQuery()
                .eq(KnowledgeSegmentPO::getDocumentId, document.getId())
                .orderByAsc(KnowledgeSegmentPO::getSegmentIndex));
        if (segments.isEmpty()) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "文档没有可向量化的切片");
        }

        // 事件消费默认只处理待处理和失败切片，手动重建才覆盖已有成功向量，避免重复消费污染状态。
        markDocumentProcessing(document.getId());
        boolean processed = false;
        for (KnowledgeSegmentPO segment : segments) {
            if (!force && segment.getEmbeddingStatus() == KnowledgeProcessStatus.SUCCESS) {
                continue;
            }
            processed = true;
            embedSegment(segment, force);
        }
        if (!processed) {
            refreshDocumentEmbeddingStatus(document.getId());
            return;
        }
        refreshDocumentEmbeddingStatus(document.getId());
        publishEmbeddingCompleted(document.getId(), operatorId);
    }

    private void embedSegment(KnowledgeSegmentPO segment, boolean force) {
        KnowledgeDocumentPO document = getDocument(segment.getDocumentId());
        KnowledgeBasePO knowledgeBase = getEnabledKnowledgeBase(document.getKbId());
        try {
            markSegmentProcessing(segment.getId());
            if (force && StringUtils.hasText(segment.getVectorId())) {
                qdrantClient.deletePoint(segment.getVectorId());
            }
            EmbeddingResult result = embeddingClient.embed(new EmbeddingRequest(
                    segment.getContent(),
                    embeddingProperties.getModel(),
                    embeddingProperties.getDimensions()));
            validateVectorDimensions(result);
            String vectorId = buildVectorId(segment.getId());
            qdrantClient.upsertPoint(vectorId, result.vector(), buildPayload(knowledgeBase, document, segment));
            markSegmentSuccess(segment.getId(), vectorId);
        } catch (RuntimeException ex) {
            markSegmentFailed(segment.getId(), ex);
            refreshDocumentEmbeddingStatus(segment.getDocumentId());
            throw ex;
        }
    }

    private void validateVectorDimensions(EmbeddingResult result) {
        if (result == null || result.vector() == null || result.vector().isEmpty()) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "向量模型返回结果为空");
        }
        if (result.dimensions() != embeddingProperties.getDimensions()
                || result.vector().size() != qdrantProperties.getVectorSize()
                || embeddingProperties.getDimensions() != qdrantProperties.getVectorSize()) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID,
                    "向量维度与 Qdrant Collection 配置不一致，必须重建索引");
        }
    }

    private QdrantPointPayload buildPayload(
            KnowledgeBasePO knowledgeBase,
            KnowledgeDocumentPO document,
            KnowledgeSegmentPO segment) {
        return QdrantPointPayload.builder()
                .kbId(segment.getKbId())
                .documentId(segment.getDocumentId())
                .segmentId(segment.getId())
                .courseId(knowledgeBase.getCourseId())
                .segmentIndex(segment.getSegmentIndex())
                .title(StringUtils.hasText(segment.getTitle()) ? segment.getTitle() : document.getTitle())
                .sourceType(document.getSourceType() == null ? null : document.getSourceType().getValue())
                .content(segment.getContent())
                .metadata(segment.getMetadata())
                .build();
    }

    private KnowledgeSegmentPO getSegment(Long segmentId) {
        if (segmentId == null || segmentId <= 0) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "切片ID必须为正数");
        }
        KnowledgeSegmentPO segment = knowledgeSegmentMapper.selectById(segmentId);
        if (segment == null) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_DOCUMENT_NOT_FOUND, "知识切片不存在");
        }
        return segment;
    }

    private KnowledgeDocumentPO getDocument(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_DOCUMENT_NOT_FOUND);
        }
        KnowledgeDocumentPO document = knowledgeDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_DOCUMENT_NOT_FOUND);
        }
        return document;
    }

    private KnowledgeBasePO getEnabledKnowledgeBase(Long kbId) {
        KnowledgeBasePO knowledgeBase = knowledgeBaseMapper.selectById(kbId);
        if (knowledgeBase == null) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        if (knowledgeBase.getStatus() == EnableStatus.DISABLED) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "知识库已停用，不能生成向量");
        }
        return knowledgeBase;
    }

    private LoginUser requireManagePermission() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (hasRole(loginUser, KnowledgeConstants.ROLE_ADMIN)
                || hasPermission(loginUser, KnowledgeConstants.PERMISSION_KNOWLEDGE_MANAGE)) {
            return loginUser;
        }
        throw new BizException(KnowledgeErrorCode.KNOWLEDGE_FORBIDDEN);
    }

    private void markDocumentProcessing(Long documentId) {
        knowledgeDocumentMapper.update(null, Wrappers.<KnowledgeDocumentPO>update()
                .eq("id", documentId)
                .set("embedding_status", KnowledgeProcessStatus.PROCESSING)
                .set("embedding_error", null));
    }

    private void markSegmentProcessing(Long segmentId) {
        knowledgeSegmentMapper.update(null, Wrappers.<KnowledgeSegmentPO>update()
                .eq("id", segmentId)
                .set("embedding_status", KnowledgeProcessStatus.PROCESSING)
                .set("embedding_error", null));
    }

    private void markSegmentSuccess(Long segmentId, String vectorId) {
        knowledgeSegmentMapper.update(null, Wrappers.<KnowledgeSegmentPO>update()
                .eq("id", segmentId)
                .set("vector_id", vectorId)
                .set("embedding_status", KnowledgeProcessStatus.SUCCESS)
                .set("embedding_error", null));
    }

    private void markSegmentFailed(Long segmentId, RuntimeException ex) {
        knowledgeSegmentMapper.update(null, Wrappers.<KnowledgeSegmentPO>update()
                .eq("id", segmentId)
                .set("embedding_status", KnowledgeProcessStatus.FAILED)
                .set("embedding_error", toChineseError(ex)));
    }

    private void refreshDocumentEmbeddingStatus(Long documentId) {
        KnowledgeDocumentPO document = getDocument(documentId);
        List<KnowledgeSegmentPO> segments = knowledgeSegmentMapper.selectList(Wrappers.<KnowledgeSegmentPO>lambdaQuery()
                .eq(KnowledgeSegmentPO::getDocumentId, documentId));
        KnowledgeProcessStatus status;
        String error = null;
        if (segments.isEmpty()) {
            status = KnowledgeProcessStatus.FAILED;
            error = "文档没有可向量化的切片";
        } else if (segments.stream().anyMatch(item -> item.getEmbeddingStatus() == KnowledgeProcessStatus.FAILED)) {
            status = KnowledgeProcessStatus.FAILED;
            error = segments.stream()
                    .map(KnowledgeSegmentPO::getEmbeddingError)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse("部分切片向量化失败");
        } else if (segments.stream().allMatch(item -> item.getEmbeddingStatus() == KnowledgeProcessStatus.SUCCESS)) {
            status = KnowledgeProcessStatus.SUCCESS;
        } else {
            status = KnowledgeProcessStatus.PROCESSING;
        }
        knowledgeDocumentMapper.update(null, Wrappers.<KnowledgeDocumentPO>update()
                .eq("id", document.getId())
                .set("embedding_status", status)
                .set("embedding_error", error));
    }

    private void publishEmbeddingCompleted(Long documentId, Long operatorId) {
        KnowledgeDocumentPO document = getDocument(documentId);
        if (document.getEmbeddingStatus() != KnowledgeProcessStatus.SUCCESS) {
            return;
        }
        KnowledgeEmbeddingRequestedPayload payload = new KnowledgeEmbeddingRequestedPayload(
                document.getKbId(),
                document.getId(),
                operatorId == null ? 0L : operatorId,
                document.getTitle(),
                document.getSourceUrl(),
                document.getContentHash(),
                LocalDateTime.now());
        eventPublisher.publish(DomainEvent.of(
                EventTopicConstants.KNOWLEDGE_EMBEDDING_COMPLETED,
                EventTopicConstants.KNOWLEDGE_EMBEDDING_COMPLETED,
                KnowledgeConstants.BIZ_KEY_DOCUMENT_PREFIX + document.getId(),
                payload));
    }

    private String buildVectorId(Long segmentId) {
        return String.valueOf(segmentId);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        List<String> roles = loginUser.getRoles();
        return roles != null && roles.stream().anyMatch(role -> roleCode.equalsIgnoreCase(role));
    }

    private boolean hasPermission(LoginUser loginUser, String permissionCode) {
        List<String> permissions = loginUser.getPermissions();
        return permissions != null && permissions.contains(permissionCode);
    }

    private String toChineseError(RuntimeException ex) {
        Throwable root = ex;
        while (root != null && root.getCause() != null) {
            root = root.getCause();
        }
        String message = root != null && StringUtils.hasText(root.getMessage())
                ? root.getMessage()
                : "未知异常";
        String result = message.startsWith("向量") || message.startsWith("Qdrant") || message.startsWith("未配置")
                ? message
                : "向量化失败：" + message;
        return result.length() > MAX_ERROR_LENGTH ? result.substring(0, MAX_ERROR_LENGTH) : result;
    }
}
