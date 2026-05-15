package com.elysia.mooc.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.service.EventPublisher;
import com.elysia.mooc.knowledge.constants.KnowledgeConstants;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.dto.CreateKnowledgeBaseRequest;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeBaseQuery;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeDocumentQuery;
import com.elysia.mooc.knowledge.domain.dto.UpdateKnowledgeBaseRequest;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import com.elysia.mooc.knowledge.domain.payload.KnowledgeEmbeddingRequestedPayload;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeSegmentPO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeBaseVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeDocumentMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeSegmentMapper;
import com.elysia.mooc.knowledge.service.KnowledgeBaseService;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.enums.MediaUploadStatus;
import com.elysia.mooc.media.domain.po.MediaFilePO;
import com.elysia.mooc.media.mapper.MediaFileMapper;
import com.elysia.mooc.media.service.MediaStorageService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 知识库基础与文档管理服务实现。 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private static final Pattern CODE_UNSAFE_PATTERN = Pattern.compile("[^A-Z0-9_]");
    private static final int CODE_HASH_LENGTH = 12;

    private final UserContextService userContextService;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeSegmentMapper knowledgeSegmentMapper;
    private final MediaFileMapper mediaFileMapper;
    private final MediaStorageService mediaStorageService;
    private final EventPublisher eventPublisher;

    /**
     * 分页查询知识库。
     *
     * @param query 查询条件
     * @return 知识库分页结果
     */
    @Override
    public PageResult<KnowledgeBaseVO> listKnowledgeBases(KnowledgeBaseQuery query) {
        requireManagePermission();
        KnowledgeBaseQuery safeQuery = query == null ? new KnowledgeBaseQuery() : query;
        LambdaQueryWrapper<KnowledgeBasePO> wrapper = Wrappers.<KnowledgeBasePO>lambdaQuery();
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(nested -> nested.like(KnowledgeBasePO::getName, keyword)
                    .or()
                    .like(KnowledgeBasePO::getCode, keyword));
        }
        if (safeQuery.getStatus() != null) {
            wrapper.eq(KnowledgeBasePO::getStatus, safeQuery.getStatus());
        }
        applyKnowledgeBaseSort(wrapper, safeQuery);

        Page<KnowledgeBasePO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<KnowledgeBasePO> result = knowledgeBaseMapper.selectPage(page, wrapper);
        return PageResult.of(result, toKnowledgeBaseVOList(result.getRecords()));
    }

    /**
     * 创建知识库。
     *
     * @param request 创建请求
     * @return 新建知识库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseVO createKnowledgeBase(CreateKnowledgeBaseRequest request) {
        requireManagePermission();
        CreateKnowledgeBaseRequest safeRequest = requireCreateRequest(request);
        String name = safeRequest.getName().trim();
        String code = resolveCreateCode(safeRequest, name);
        assertKnowledgeBaseUnique(null, name, code);

        // 新建默认值和状态字段显式赋值，避免 Bean 拷贝把审计字段或逻辑删除语义带入新增对象。
        KnowledgeBasePO po = new KnowledgeBasePO();
        po.setName(name);
        po.setCode(code);
        po.setDescription(cleanText(safeRequest.getDescription()));
        po.setScopeType(safeRequest.getCourseId() == null ? KnowledgeScopeType.GLOBAL : KnowledgeScopeType.COURSE);
        po.setCourseId(safeRequest.getCourseId());
        po.setStatus(EnableStatus.ENABLED);
        po.setDeleted(0);
        try {
            knowledgeBaseMapper.insert(po);
        } catch (DuplicateKeyException ex) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_BASE_DUPLICATED, "知识库编码已存在");
        }
        return toKnowledgeBaseVO(po);
    }

    /**
     * 修改知识库。
     *
     * @param id 知识库 ID
     * @param request 修改请求
     * @return 修改后的知识库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseVO updateKnowledgeBase(Long id, UpdateKnowledgeBaseRequest request) {
        requireManagePermission();
        UpdateKnowledgeBaseRequest safeRequest = requireUpdateRequest(request);
        KnowledgeBasePO po = getKnowledgeBase(id);
        String name = safeRequest.getName().trim();
        assertKnowledgeBaseUnique(po.getId(), name, po.getCode());

        po.setName(name);
        po.setDescription(cleanText(safeRequest.getDescription()));
        po.setStatus(resolveUpdateStatus(safeRequest));
        knowledgeBaseMapper.updateById(po);
        return toKnowledgeBaseVO(getKnowledgeBase(id));
    }

    /**
     * 上传知识库文档并登记元数据。
     *
     * @param kbId 知识库 ID
     * @param file 文档文件
     * @param title 文档标题
     * @param sourceType 来源类型
     * @return 新建文档
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDocumentVO uploadDocument(Long kbId, MultipartFile file, String title, String sourceType) {
        LoginUser loginUser = requireManagePermission();
        KnowledgeBasePO knowledgeBase = getEnabledKnowledgeBase(kbId);
        String safeTitle = cleanRequiredText(title, "文档标题不能为空");
        MediaStorageService.StoredFile storedFile = mediaStorageService.storeFile(file, MediaBizType.KNOWLEDGE_DOC);
        assertDocumentUnique(knowledgeBase.getId(), storedFile.fileHash());

        // 文档上传在 day12 只落元数据，解析、切片和向量化状态必须保持待处理，不能提前标记成功。
        MediaFilePO mediaFile = createMediaFile(loginUser.getUserId(), storedFile);
        KnowledgeDocumentPO document = new KnowledgeDocumentPO();
        document.setKbId(knowledgeBase.getId());
        document.setMediaFileId(mediaFile.getId());
        document.setTitle(safeTitle);
        document.setSourceType(KnowledgeDocumentSourceType.fromInputAndFileName(sourceType, storedFile.originalName()));
        document.setSourceUrl(storedFile.url());
        document.setContentHash(storedFile.fileHash());
        document.setParseStatus(KnowledgeProcessStatus.PENDING);
        document.setSegmentCount(0);
        document.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        document.setDeleted(0);
        try {
            knowledgeDocumentMapper.insert(document);
        } catch (DuplicateKeyException ex) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_DOCUMENT_DUPLICATED, "同一知识库内文档已存在");
        }
        return toKnowledgeDocumentVO(document, knowledgeBase);
    }

    /**
     * 分页查询知识库文档。
     *
     * @param query 查询条件
     * @return 文档分页结果
     */
    @Override
    public PageResult<KnowledgeDocumentVO> listDocuments(KnowledgeDocumentQuery query) {
        requireManagePermission();
        KnowledgeDocumentQuery safeQuery = query == null ? new KnowledgeDocumentQuery() : query;
        LambdaQueryWrapper<KnowledgeDocumentPO> wrapper = Wrappers.<KnowledgeDocumentPO>lambdaQuery();
        if (safeQuery.resolvedKbId() != null) {
            wrapper.eq(KnowledgeDocumentPO::getKbId, safeQuery.resolvedKbId());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(KnowledgeDocumentPO::getTitle, safeQuery.getKeyword().trim());
        }
        if (safeQuery.getParseStatus() != null) {
            wrapper.eq(KnowledgeDocumentPO::getParseStatus, safeQuery.getParseStatus());
        }
        if (safeQuery.getEmbeddingStatus() != null) {
            wrapper.eq(KnowledgeDocumentPO::getEmbeddingStatus, safeQuery.getEmbeddingStatus());
        }
        applyDocumentSort(wrapper, safeQuery);

        Page<KnowledgeDocumentPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<KnowledgeDocumentPO> result = knowledgeDocumentMapper.selectPage(page, wrapper);
        Map<Long, KnowledgeBasePO> baseMap = queryBaseMap(result.getRecords());
        List<KnowledgeDocumentVO> records = result.getRecords().stream()
                .map(document -> toKnowledgeDocumentVO(document, baseMap.get(document.getKbId())))
                .toList();
        return PageResult.of(result, records);
    }

    /**
     * 重建文档索引，仅重置状态并投递事件。
     *
     * @param documentId 文档 ID
     * @return 是否提交成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rebuildDocument(Long documentId) {
        LoginUser loginUser = requireManagePermission();
        KnowledgeDocumentPO document = getKnowledgeDocument(documentId);
        KnowledgeBasePO knowledgeBase = getEnabledKnowledgeBase(document.getKbId());

        // 重建入口只恢复待处理状态并发布事件；真实解析、切片、向量化从 day13/day14 接管。
        document.setParseStatus(KnowledgeProcessStatus.PENDING);
        document.setParseError(null);
        document.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        document.setEmbeddingError(null);
        document.setSegmentCount(0);
        knowledgeDocumentMapper.updateById(document);
        knowledgeSegmentMapper.update(null, Wrappers.<KnowledgeSegmentPO>update()
                .eq("document_id", document.getId())
                .set("embedding_status", KnowledgeProcessStatus.PENDING)
                .set("embedding_error", null)
                .set("vector_id", null));

        KnowledgeEmbeddingRequestedPayload payload = new KnowledgeEmbeddingRequestedPayload(
                knowledgeBase.getId(),
                document.getId(),
                loginUser.getUserId(),
                document.getTitle(),
                document.getSourceUrl(),
                document.getContentHash(),
                LocalDateTime.now());
        eventPublisher.publish(DomainEvent.of(
                EventTopicConstants.KNOWLEDGE_EMBEDDING_REQUESTED,
                EventTopicConstants.KNOWLEDGE_EMBEDDING_REQUESTED,
                KnowledgeConstants.BIZ_KEY_DOCUMENT_PREFIX + document.getId(),
                payload));
        return true;
    }

    private LoginUser requireManagePermission() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (hasRole(loginUser, KnowledgeConstants.ROLE_ADMIN)
                || hasPermission(loginUser, KnowledgeConstants.PERMISSION_KNOWLEDGE_MANAGE)) {
            return loginUser;
        }
        throw new BizException(KnowledgeErrorCode.KNOWLEDGE_FORBIDDEN);
    }

    private CreateKnowledgeBaseRequest requireCreateRequest(CreateKnowledgeBaseRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "知识库名称不能为空");
        }
        if (request.getCourseId() != null && request.getCourseId() <= 0) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "课程ID必须为正数");
        }
        return request;
    }

    private UpdateKnowledgeBaseRequest requireUpdateRequest(UpdateKnowledgeBaseRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "知识库名称不能为空");
        }
        return request;
    }

    private KnowledgeBasePO getKnowledgeBase(Long id) {
        if (id == null || id <= 0) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        KnowledgeBasePO po = knowledgeBaseMapper.selectById(id);
        if (po == null) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        return po;
    }

    private KnowledgeBasePO getEnabledKnowledgeBase(Long id) {
        KnowledgeBasePO po = getKnowledgeBase(id);
        if (po.getStatus() == EnableStatus.DISABLED) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID, "知识库已停用，不能处理文档");
        }
        return po;
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

    private MediaFilePO createMediaFile(Long ownerId, MediaStorageService.StoredFile storedFile) {
        MediaFilePO mediaFile = new MediaFilePO();
        mediaFile.setOwnerId(ownerId);
        mediaFile.setBizType(MediaBizType.KNOWLEDGE_DOC);
        mediaFile.setOriginalName(storedFile.originalName());
        mediaFile.setStoragePath(storedFile.storagePath());
        mediaFile.setUrl(storedFile.url());
        mediaFile.setContentType(storedFile.contentType());
        mediaFile.setFileSize(storedFile.fileSize());
        mediaFile.setFileHash(storedFile.fileHash());
        mediaFile.setUploadStatus(MediaUploadStatus.SUCCESS);
        mediaFile.setDeleted(0);
        mediaFileMapper.insert(mediaFile);
        return mediaFile;
    }

    private void assertKnowledgeBaseUnique(Long excludeId, String name, String code) {
        Long count = knowledgeBaseMapper.selectCount(Wrappers.<KnowledgeBasePO>lambdaQuery()
                .and(wrapper -> wrapper.eq(KnowledgeBasePO::getName, name)
                        .or()
                        .eq(KnowledgeBasePO::getCode, code))
                .ne(excludeId != null, KnowledgeBasePO::getId, excludeId));
        if (count > 0) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_BASE_DUPLICATED, "知识库名称或编码已存在");
        }
    }

    private void assertDocumentUnique(Long kbId, String contentHash) {
        Long count = knowledgeDocumentMapper.selectCount(Wrappers.<KnowledgeDocumentPO>lambdaQuery()
                .eq(KnowledgeDocumentPO::getKbId, kbId)
                .eq(KnowledgeDocumentPO::getContentHash, contentHash));
        if (count > 0) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_DOCUMENT_DUPLICATED, "同一知识库内文档已存在");
        }
    }

    private String resolveCreateCode(CreateKnowledgeBaseRequest request, String name) {
        if (StringUtils.hasText(request.getCode())) {
            return normalizeCode(request.getCode());
        }
        String prefix = request.getCourseId() == null ? "KB_GLOBAL_" : "KB_COURSE_" + request.getCourseId() + "_";
        return prefix + sha256(name + ":" + (request.getCourseId() == null ? "GLOBAL" : request.getCourseId()))
                .substring(0, CODE_HASH_LENGTH)
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        String normalized = CODE_UNSAFE_PATTERN.matcher(code.trim().toUpperCase(Locale.ROOT)).replaceAll("_");
        if (!StringUtils.hasText(normalized)) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "知识库编码不能为空");
        }
        return normalized.length() > 64 ? normalized.substring(0, 64) : normalized;
    }

    private EnableStatus resolveUpdateStatus(UpdateKnowledgeBaseRequest request) {
        if (request.getStatus() != null) {
            return request.getStatus();
        }
        if (request.getEnabled() != null) {
            return Boolean.TRUE.equals(request.getEnabled()) ? EnableStatus.ENABLED : EnableStatus.DISABLED;
        }
        return EnableStatus.ENABLED;
    }

    private List<KnowledgeBaseVO> toKnowledgeBaseVOList(List<KnowledgeBasePO> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        List<Long> kbIds = records.stream().map(KnowledgeBasePO::getId).toList();
        Map<Long, Long> documentCountMap = countDocuments(kbIds);
        Map<Long, Long> segmentCountMap = countSegments(kbIds);
        return records.stream()
                .map(record -> toKnowledgeBaseVO(record, documentCountMap, segmentCountMap))
                .toList();
    }

    private KnowledgeBaseVO toKnowledgeBaseVO(KnowledgeBasePO po) {
        if (po.getId() == null) {
            return toKnowledgeBaseVO(po, Map.of(), Map.of());
        }
        return toKnowledgeBaseVO(po, countDocuments(List.of(po.getId())), countSegments(List.of(po.getId())));
    }

    private KnowledgeBaseVO toKnowledgeBaseVO(
            KnowledgeBasePO po,
            Map<Long, Long> documentCountMap,
            Map<Long, Long> segmentCountMap) {
        return BeanCopyUtils.copyBean(po, KnowledgeBaseVO.class, (source, target) -> {
            target.setScope(toLegacyScope(source.getScopeType()));
            Long id = source.getId();
            target.setDocumentCount(id == null ? 0L : documentCountMap.getOrDefault(id, 0L));
            target.setSegmentCount(id == null ? 0L : segmentCountMap.getOrDefault(id, 0L));
        });
    }

    private KnowledgeDocumentVO toKnowledgeDocumentVO(KnowledgeDocumentPO document, KnowledgeBasePO knowledgeBase) {
        return BeanCopyUtils.copyBean(document, KnowledgeDocumentVO.class, (source, target) -> {
            target.setKnowledgeBaseId(source.getKbId());
            target.setKnowledgeBaseName(knowledgeBase == null ? null : knowledgeBase.getName());
            target.setErrorMessage(StringUtils.hasText(source.getParseError())
                    ? source.getParseError()
                    : source.getEmbeddingError());
        });
    }

    private Map<Long, Long> countDocuments(List<Long> kbIds) {
        if (kbIds == null || kbIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> result = new HashMap<>();
        knowledgeDocumentMapper.selectMaps(Wrappers.<KnowledgeDocumentPO>query()
                        .select("kb_id", "COUNT(*) AS total")
                        .in("kb_id", kbIds)
                        .groupBy("kb_id"))
                .forEach(row -> result.put(toLong(row.get("kb_id")), toLong(row.get("total"))));
        return result;
    }

    private Map<Long, Long> countSegments(List<Long> kbIds) {
        if (kbIds == null || kbIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> result = new HashMap<>();
        knowledgeSegmentMapper.selectMaps(Wrappers.<KnowledgeSegmentPO>query()
                        .select("kb_id", "COUNT(*) AS total")
                        .in("kb_id", kbIds)
                        .groupBy("kb_id"))
                .forEach(row -> result.put(toLong(row.get("kb_id")), toLong(row.get("total"))));
        return result;
    }

    private Map<Long, KnowledgeBasePO> queryBaseMap(List<KnowledgeDocumentPO> documents) {
        if (documents == null || documents.isEmpty()) {
            return Map.of();
        }
        List<Long> kbIds = documents.stream().map(KnowledgeDocumentPO::getKbId).distinct().toList();
        Map<Long, KnowledgeBasePO> result = new HashMap<>();
        knowledgeBaseMapper.selectBatchIds(kbIds).forEach(item -> result.put(item.getId(), item));
        return result;
    }

    private void applyKnowledgeBaseSort(LambdaQueryWrapper<KnowledgeBasePO> wrapper, KnowledgeBaseQuery query) {
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        String sortBy = query.getSortBy();
        if ("id".equals(sortBy)) {
            wrapper.orderBy(true, asc, KnowledgeBasePO::getId);
            return;
        }
        if ("updateTime".equals(sortBy)) {
            wrapper.orderBy(true, asc, KnowledgeBasePO::getUpdateTime).orderByDesc(KnowledgeBasePO::getId);
            return;
        }
        wrapper.orderBy(true, asc, KnowledgeBasePO::getCreateTime).orderByDesc(KnowledgeBasePO::getId);
    }

    private void applyDocumentSort(LambdaQueryWrapper<KnowledgeDocumentPO> wrapper, KnowledgeDocumentQuery query) {
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        String sortBy = query.getSortBy();
        if ("id".equals(sortBy)) {
            wrapper.orderBy(true, asc, KnowledgeDocumentPO::getId);
            return;
        }
        if ("updateTime".equals(sortBy)) {
            wrapper.orderBy(true, asc, KnowledgeDocumentPO::getUpdateTime).orderByDesc(KnowledgeDocumentPO::getId);
            return;
        }
        wrapper.orderBy(true, asc, KnowledgeDocumentPO::getCreateTime).orderByDesc(KnowledgeDocumentPO::getId);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        List<String> roles = loginUser.getRoles();
        return roles != null && roles.stream().anyMatch(role -> roleCode.equalsIgnoreCase(role));
    }

    private boolean hasPermission(LoginUser loginUser, String permissionCode) {
        List<String> permissions = loginUser.getPermissions();
        return permissions != null && permissions.contains(permissionCode);
    }

    private String cleanRequiredText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, message);
        }
        return text.trim();
    }

    private String cleanText(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private String toLegacyScope(KnowledgeScopeType scopeType) {
        if (scopeType == KnowledgeScopeType.COURSE) {
            return "course";
        }
        if (scopeType == KnowledgeScopeType.ADMIN) {
            return "private";
        }
        return "platform";
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? 0L : Long.parseLong(String.valueOf(value));
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前运行环境不支持SHA-256算法", ex);
        }
    }
}
