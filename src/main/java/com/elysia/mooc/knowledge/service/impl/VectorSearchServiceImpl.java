package com.elysia.mooc.knowledge.service.impl;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.constants.KnowledgeConstants;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.dto.VectorSearchRequest;
import com.elysia.mooc.knowledge.domain.vo.VectorSearchResponseVO;
import com.elysia.mooc.knowledge.domain.vo.VectorSearchSourceVO;
import com.elysia.mooc.knowledge.embedding.EmbeddingClient;
import com.elysia.mooc.knowledge.embedding.EmbeddingProperties;
import com.elysia.mooc.knowledge.embedding.EmbeddingRequest;
import com.elysia.mooc.knowledge.embedding.EmbeddingResult;
import com.elysia.mooc.knowledge.qdrant.QdrantClient;
import com.elysia.mooc.knowledge.qdrant.QdrantPointPayload;
import com.elysia.mooc.knowledge.qdrant.QdrantProperties;
import com.elysia.mooc.knowledge.qdrant.VectorSearchResult;
import com.elysia.mooc.knowledge.service.VectorSearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 向量检索服务实现。 */
@Service
@RequiredArgsConstructor
public class VectorSearchServiceImpl implements VectorSearchService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int PREVIEW_LENGTH = 120;

    private final UserContextService userContextService;
    private final EmbeddingProperties embeddingProperties;
    private final QdrantProperties qdrantProperties;
    private final EmbeddingClient embeddingClient;
    private final QdrantClient qdrantClient;

    /**
     * 执行向量检索调试。
     *
     * @param request 检索请求
     * @return 命中来源
     */
    @Override
    public VectorSearchResponseVO search(VectorSearchRequest request) {
        requireManagePermission();
        VectorSearchRequest safeRequest = requireRequest(request);
        EmbeddingResult result = embeddingClient.embed(new EmbeddingRequest(
                safeRequest.getQuery().trim(),
                embeddingProperties.getModel(),
                embeddingProperties.getDimensions()));
        validateVectorDimensions(result);
        List<VectorSearchSourceVO> sources = qdrantClient.search(
                        result.vector(),
                        safeRequest.getKnowledgeBaseId(),
                        safeRequest.getTopK() == null ? DEFAULT_TOP_K : safeRequest.getTopK())
                .stream()
                .map(this::toSourceVO)
                .toList();
        VectorSearchResponseVO vo = new VectorSearchResponseVO();
        vo.setQuery(safeRequest.getQuery().trim());
        vo.setSources(sources);
        return vo;
    }

    private VectorSearchRequest requireRequest(VectorSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuery())) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "检索文本不能为空");
        }
        if (request.getTopK() == null) {
            request.setTopK(DEFAULT_TOP_K);
        }
        return request;
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

    private VectorSearchSourceVO toSourceVO(VectorSearchResult result) {
        QdrantPointPayload payload = result.getPayload();
        VectorSearchSourceVO vo = new VectorSearchSourceVO();
        vo.setVectorId(result.getVectorId());
        vo.setScore(result.getScore());
        vo.setPayload(payload);
        if (payload != null) {
            vo.setKbId(payload.getKbId());
            vo.setDocumentId(payload.getDocumentId());
            vo.setSegmentId(payload.getSegmentId());
            vo.setCourseId(payload.getCourseId());
            vo.setTitle(payload.getTitle());
            vo.setSourceType(payload.getSourceType());
            vo.setContentPreview(preview(payload.getContent()));
        }
        return vo;
    }

    private String preview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String text = content.trim();
        return text.length() <= PREVIEW_LENGTH ? text : text.substring(0, PREVIEW_LENGTH);
    }

    private LoginUser requireManagePermission() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (hasRole(loginUser, KnowledgeConstants.ROLE_ADMIN)
                || hasPermission(loginUser, KnowledgeConstants.PERMISSION_KNOWLEDGE_MANAGE)) {
            return loginUser;
        }
        throw new BizException(KnowledgeErrorCode.KNOWLEDGE_FORBIDDEN);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        List<String> roles = loginUser.getRoles();
        return roles != null && roles.stream().anyMatch(role -> roleCode.equalsIgnoreCase(role));
    }

    private boolean hasPermission(LoginUser loginUser, String permissionCode) {
        List<String> permissions = loginUser.getPermissions();
        return permissions != null && permissions.contains(permissionCode);
    }
}
