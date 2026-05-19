package com.elysia.mooc.ops.service;

import com.elysia.mooc.ops.domain.dto.CreateExportJobRequest;
import com.elysia.mooc.ops.domain.dto.CreateReviewTaskRequest;
import com.elysia.mooc.ops.domain.dto.UpdateConfigItemRequest;
import com.elysia.mooc.ops.domain.vo.ConfigItemResultVO;
import com.elysia.mooc.ops.domain.vo.ExportTaskResultVO;
import com.elysia.mooc.ops.domain.vo.ReviewTaskResultVO;

/** 运营配置、审核任务和导出任务服务。 */
public interface OpsConfigService {

    /**
     * 创建审核任务。
     *
     * @param request 审核任务请求
     * @return 创建结果
     */
    ReviewTaskResultVO createReviewTask(CreateReviewTaskRequest request);

    /**
     * 创建异步导出任务。
     *
     * @param request 导出任务请求
     * @return 创建结果
     */
    ExportTaskResultVO createExportJob(CreateExportJobRequest request);

    /**
     * 更新或创建运营配置项。
     *
     * @param configKey 配置键
     * @param request 配置更新请求
     * @return 最新配置
     */
    ConfigItemResultVO updateConfigItem(String configKey, UpdateConfigItemRequest request);
}
