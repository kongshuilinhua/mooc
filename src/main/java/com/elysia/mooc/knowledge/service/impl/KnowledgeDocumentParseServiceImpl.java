package com.elysia.mooc.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.payload.MediaDocumentUploadedPayload;
import com.elysia.mooc.event.service.EventPublisher;
import com.elysia.mooc.knowledge.constants.KnowledgeConstants;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeSegmentQuery;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.payload.KnowledgeEmbeddingRequestedPayload;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeSegmentPO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentParseStatusVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeSegmentVO;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeDocumentMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeSegmentMapper;
import com.elysia.mooc.knowledge.parse.DocumentParser;
import com.elysia.mooc.knowledge.parse.DocumentParserFactory;
import com.elysia.mooc.knowledge.parse.TextSegment;
import com.elysia.mooc.knowledge.parse.TextSegmenter;
import com.elysia.mooc.knowledge.service.KnowledgeDocumentParseService;
import com.elysia.mooc.media.config.MediaStorageProperties;
import com.elysia.mooc.media.domain.po.MediaFilePO;
import com.elysia.mooc.media.mapper.MediaFileMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 知识库文档解析与切片服务实现。 */
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentParseServiceImpl implements KnowledgeDocumentParseService {

    private static final int MAX_PARSE_ERROR_LENGTH = 1000;

    private final UserContextService userContextService;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeSegmentMapper knowledgeSegmentMapper;
    private final MediaFileMapper mediaFileMapper;
    private final MediaStorageProperties mediaStorageProperties;
    private final DocumentParserFactory documentParserFactory;
    private final TextSegmenter textSegmenter;
    private final EventPublisher eventPublisher;

    /**
     * 手动触发文档解析。
     *
     * @param documentId 文档 ID
     * @return 解析后的文档信息
     */
    @Override
    public KnowledgeDocumentVO parseDocument(Long documentId) {
        LoginUser loginUser = requireManagePermission();
        KnowledgeDocumentPO document = prepareProcessing(documentId);
        try {
            return parseAndPersist(document.getId(), loginUser.getUserId());
        } catch (RuntimeException ex) {
            markFailed(document.getId(), ex);
            throw ex;
        }
    }

    /**
     * 查询文档切片列表。
     *
     * @param documentId 文档 ID
     * @param query 查询条件
     * @return 切片分页
     */
    @Override
    public PageResult<KnowledgeSegmentVO> listSegments(Long documentId, KnowledgeSegmentQuery query) {
        requireManagePermission();
        KnowledgeDocumentPO document = getKnowledgeDocument(documentId);
        KnowledgeSegmentQuery safeQuery = query == null ? new KnowledgeSegmentQuery() : query;
        LambdaQueryWrapper<KnowledgeSegmentPO> wrapper = Wrappers.<KnowledgeSegmentPO>lambdaQuery()
                .eq(KnowledgeSegmentPO::getDocumentId, document.getId());
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(KnowledgeSegmentPO::getContent, safeQuery.getKeyword().trim());
        }
        if (safeQuery.getStatus() != null) {
            wrapper.eq(KnowledgeSegmentPO::getEmbeddingStatus, safeQuery.getStatus());
        }
        applySegmentSort(wrapper, safeQuery);
        Page<KnowledgeSegmentPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return PageResult.of(knowledgeSegmentMapper.selectPage(page, wrapper), this::toSegmentVO);
    }

    /**
     * 查询文档解析状态。
     *
     * @param documentId 文档 ID
     * @return 解析状态
     */
    @Override
    public KnowledgeDocumentParseStatusVO getParseStatus(Long documentId) {
        requireManagePermission();
        return toParseStatusVO(getKnowledgeDocument(documentId));
    }

    /**
     * 消费文档上传事件后触发解析。
     *
     * @param payload 文档上传事件载荷
     */
    public void parseUploadedDocument(MediaDocumentUploadedPayload payload) {
        if (payload == null || payload.documentId() == null) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "文档上传事件缺少文档ID");
        }
        KnowledgeDocumentPO document = prepareProcessingWithoutPermission(payload.documentId());
        try {
            Long operatorId = payload.ownerId() == null ? 0L : payload.ownerId();
            parseAndPersist(document.getId(), operatorId);
        } catch (RuntimeException ex) {
            markFailed(document.getId(), ex);
            throw ex;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected KnowledgeDocumentPO prepareProcessing(Long documentId) {
        requireManagePermission();
        return prepareProcessingWithoutPermission(documentId);
    }

    @Transactional(rollbackFor = Exception.class)
    protected KnowledgeDocumentPO prepareProcessingWithoutPermission(Long documentId) {
        KnowledgeDocumentPO document = getKnowledgeDocument(documentId);
        getEnabledKnowledgeBase(document.getKbId());
        document.setParseStatus(KnowledgeProcessStatus.PROCESSING);
        document.setParseError(null);
        document.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        document.setEmbeddingError(null);
        document.setSegmentCount(0);
        knowledgeDocumentMapper.updateById(document);
        return document;
    }

    @Transactional(rollbackFor = Exception.class)
    protected KnowledgeDocumentVO parseAndPersist(Long documentId, Long operatorId) {
        KnowledgeDocumentPO document = getKnowledgeDocument(documentId);
        KnowledgeBasePO knowledgeBase = getEnabledKnowledgeBase(document.getKbId());
        Path path = resolveDocumentPath(document);
        DocumentParser parser = documentParserFactory.getParser(resolveSourceType(document, path));
        String parsedText = parser.parse(path);
        List<TextSegment> segments = textSegmenter.segment(parsedText);
        if (segments.isEmpty()) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "文档未提取到有效内容");
        }

        // 重复解析必须先清理旧切片，否则唯一索引会阻止同一 document_id + segment_index 再次写入。
        knowledgeSegmentMapper.deleteByDocumentIdPhysically(document.getId());
        for (TextSegment segment : segments) {
            KnowledgeSegmentPO po = new KnowledgeSegmentPO();
            po.setKbId(document.getKbId());
            po.setDocumentId(document.getId());
            po.setSegmentIndex(segment.segmentIndex());
            po.setTitle(segment.title());
            po.setContent(segment.content());
            po.setTokenCount(segment.tokenCount());
            po.setMetadata(segment.metadata());
            po.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
            po.setDeleted(0);
            knowledgeSegmentMapper.insert(po);
        }

        document.setParseStatus(KnowledgeProcessStatus.SUCCESS);
        document.setParseError(null);
        document.setSegmentCount(segments.size());
        document.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        document.setEmbeddingError(null);
        knowledgeDocumentMapper.updateById(document);
        publishEmbeddingRequested(knowledgeBase, document, operatorId);
        return toDocumentVO(document, knowledgeBase);
    }

    protected void markFailed(Long documentId, RuntimeException ex) {
        KnowledgeDocumentPO document = knowledgeDocumentMapper.selectById(documentId);
        if (document == null) {
            return;
        }
        // 解析失败要尽量独立写回中文原因，即使业务异常继续抛给前端也保留排查线索。
        knowledgeDocumentMapper.update(null, Wrappers.<KnowledgeDocumentPO>update()
                .eq("id", document.getId())
                .set("parse_status", KnowledgeProcessStatus.FAILED)
                .set("parse_error", toChineseError(ex))
                .set("segment_count", 0)
                .set("embedding_status", KnowledgeProcessStatus.PENDING)
                .set("embedding_error", null));
    }

    private LoginUser requireManagePermission() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (hasRole(loginUser, KnowledgeConstants.ROLE_ADMIN)
                || hasPermission(loginUser, KnowledgeConstants.PERMISSION_KNOWLEDGE_MANAGE)) {
            return loginUser;
        }
        throw new BizException(KnowledgeErrorCode.KNOWLEDGE_FORBIDDEN);
    }

    private KnowledgeDocumentPO getKnowledgeDocument(Long documentId) {
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
        if (knowledgeBase.getStatus() != null
                && knowledgeBase.getStatus().equalsValue(com.elysia.mooc.common.enums.EnableStatus.DISABLED)) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "知识库已停用，不能解析文档");
        }
        return knowledgeBase;
    }

    private Path resolveDocumentPath(KnowledgeDocumentPO document) {
        MediaFilePO mediaFile = document.getMediaFileId() == null ? null : mediaFileMapper.selectById(document.getMediaFileId());
        if (mediaFile != null && StringUtils.hasText(mediaFile.getStoragePath())) {
            return assertReadablePath(Path.of(mediaFile.getStoragePath()));
        }
        if (StringUtils.hasText(document.getSourceUrl())) {
            String sourceUrl = document.getSourceUrl().trim();
            if (sourceUrl.startsWith(mediaStorageProperties.getPublicPrefix() + "/")) {
                String relativePath = sourceUrl.substring(mediaStorageProperties.getPublicPrefix().length() + 1);
                return assertReadablePath(Path.of(mediaStorageProperties.getRootPath()).resolve(relativePath));
            }
            if (!sourceUrl.startsWith("local://")) {
                return assertReadablePath(Path.of(sourceUrl));
            }
        }
        throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "文档源文件不存在，无法解析");
    }

    private Path assertReadablePath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (!Files.exists(normalized) || !Files.isRegularFile(normalized)) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "文档源文件不存在，无法解析");
        }
        return normalized;
    }

    private KnowledgeDocumentSourceType resolveSourceType(KnowledgeDocumentPO document, Path path) {
        KnowledgeDocumentSourceType sourceType = document.getSourceType();
        if (sourceType != KnowledgeDocumentSourceType.UPLOAD) {
            return sourceType;
        }
        return KnowledgeDocumentSourceType.fromInputAndFileName(sourceType, path.getFileName().toString());
    }

    private void publishEmbeddingRequested(KnowledgeBasePO knowledgeBase, KnowledgeDocumentPO document, Long operatorId) {
        KnowledgeEmbeddingRequestedPayload payload = new KnowledgeEmbeddingRequestedPayload(
                knowledgeBase.getId(),
                document.getId(),
                operatorId == null ? 0L : operatorId,
                document.getTitle(),
                document.getSourceUrl(),
                document.getContentHash(),
                LocalDateTime.now());
        eventPublisher.publish(DomainEvent.of(
                EventTopicConstants.KNOWLEDGE_EMBEDDING_REQUESTED,
                EventTopicConstants.KNOWLEDGE_EMBEDDING_REQUESTED,
                KnowledgeConstants.BIZ_KEY_DOCUMENT_PREFIX + document.getId(),
                payload));
    }

    private void applySegmentSort(LambdaQueryWrapper<KnowledgeSegmentPO> wrapper, KnowledgeSegmentQuery query) {
        boolean asc = !Boolean.FALSE.equals(query.getIsAsc());
        String sortBy = query.getSortBy();
        if ("id".equals(sortBy)) {
            wrapper.orderBy(true, asc, KnowledgeSegmentPO::getId);
            return;
        }
        if ("createTime".equals(sortBy)) {
            wrapper.orderBy(true, asc, KnowledgeSegmentPO::getCreateTime).orderByAsc(KnowledgeSegmentPO::getSegmentIndex);
            return;
        }
        wrapper.orderBy(true, asc, KnowledgeSegmentPO::getSegmentIndex).orderByAsc(KnowledgeSegmentPO::getId);
    }

    private KnowledgeDocumentVO toDocumentVO(KnowledgeDocumentPO document, KnowledgeBasePO knowledgeBase) {
        return BeanCopyUtils.copyBean(document, KnowledgeDocumentVO.class, (source, target) -> {
            target.setKnowledgeBaseId(source.getKbId());
            target.setKnowledgeBaseName(knowledgeBase == null ? null : knowledgeBase.getName());
            target.setErrorMessage(StringUtils.hasText(source.getParseError())
                    ? source.getParseError()
                    : source.getEmbeddingError());
        });
    }

    private KnowledgeSegmentVO toSegmentVO(KnowledgeSegmentPO po) {
        return BeanCopyUtils.copyBean(po, KnowledgeSegmentVO.class);
    }

    private KnowledgeDocumentParseStatusVO toParseStatusVO(KnowledgeDocumentPO document) {
        KnowledgeDocumentParseStatusVO vo = new KnowledgeDocumentParseStatusVO();
        vo.setDocumentId(document.getId());
        vo.setKbId(document.getKbId());
        vo.setTitle(document.getTitle());
        vo.setParseStatus(document.getParseStatus());
        vo.setParseError(document.getParseError());
        vo.setSegmentCount(document.getSegmentCount());
        vo.setEmbeddingStatus(document.getEmbeddingStatus());
        vo.setEmbeddingError(document.getEmbeddingError());
        vo.setUpdateTime(document.getUpdateTime());
        return vo;
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
        String result = message.startsWith("文档") ? message : "文档解析失败：" + message;
        return result.length() > MAX_PARSE_ERROR_LENGTH ? result.substring(0, MAX_PARSE_ERROR_LENGTH) : result;
    }
}
