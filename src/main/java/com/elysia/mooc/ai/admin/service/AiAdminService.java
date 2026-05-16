package com.elysia.mooc.ai.admin.service;

import com.elysia.mooc.ai.admin.domain.dto.AiModelConfigQuery;
import com.elysia.mooc.ai.admin.domain.dto.UpdateModelConfigRequest;
import com.elysia.mooc.ai.admin.domain.vo.AiModelConfigVO;
import com.elysia.mooc.ai.admin.domain.vo.AiUsageVO;
import com.elysia.mooc.ai.admin.domain.vo.DocumentStatusOverviewVO;
import com.elysia.mooc.common.api.PageResult;

/** AI 管理后台服务。 */
public interface AiAdminService {

    /**
     * 分页查询模型配置。
     *
     * @param query 查询条件
     * @return 模型配置分页
     */
    PageResult<AiModelConfigVO> listModelConfigs(AiModelConfigQuery query);

    /**
     * 修改模型配置。
     *
     * @param id 模型配置 ID
     * @param request 修改请求
     * @return 修改后的模型配置
     */
    AiModelConfigVO updateModelConfig(Long id, UpdateModelConfigRequest request);

    /**
     * 查询知识库文档处理状态统计。
     *
     * @return 文档处理状态统计
     */
    DocumentStatusOverviewVO getDocumentStatusOverview();

    /**
     * 查询 AI 调用统计。
     *
     * @return AI 调用统计
     */
    AiUsageVO getUsage();
}
