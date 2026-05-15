package com.elysia.mooc.knowledge.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.knowledge.domain.dto.CreateKnowledgeBaseRequest;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeBaseQuery;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeDocumentQuery;
import com.elysia.mooc.knowledge.domain.dto.UpdateKnowledgeBaseRequest;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeBaseVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import org.springframework.web.multipart.MultipartFile;

/** 知识库基础与文档管理服务。 */
public interface KnowledgeBaseService {

    /**
     * 分页查询知识库。
     *
     * @param query 查询条件
     * @return 知识库分页结果
     */
    PageResult<KnowledgeBaseVO> listKnowledgeBases(KnowledgeBaseQuery query);

    /**
     * 创建知识库。
     *
     * @param request 创建请求
     * @return 新建知识库
     */
    KnowledgeBaseVO createKnowledgeBase(CreateKnowledgeBaseRequest request);

    /**
     * 修改知识库。
     *
     * @param id 知识库 ID
     * @param request 修改请求
     * @return 修改后的知识库
     */
    KnowledgeBaseVO updateKnowledgeBase(Long id, UpdateKnowledgeBaseRequest request);

    /**
     * 上传知识库文档并登记元数据。
     *
     * @param kbId 知识库 ID
     * @param file 文档文件
     * @param title 文档标题
     * @param sourceType 来源类型
     * @return 新建文档
     */
    KnowledgeDocumentVO uploadDocument(Long kbId, MultipartFile file, String title, String sourceType);

    /**
     * 分页查询知识库文档。
     *
     * @param query 查询条件
     * @return 文档分页结果
     */
    PageResult<KnowledgeDocumentVO> listDocuments(KnowledgeDocumentQuery query);

    /**
     * 重建文档索引，仅重置状态并投递事件。
     *
     * @param documentId 文档 ID
     * @return 是否提交成功
     */
    Boolean rebuildDocument(Long documentId);
}
