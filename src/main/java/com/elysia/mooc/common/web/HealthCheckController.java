package com.elysia.mooc.common.web;

import com.elysia.mooc.common.api.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统基础接口，用于验证后端服务连通性和统一响应格式。
 */
@RestController
@RequestMapping("/api")
@Tag(name = "系统基础接口", description = "连通性与统一响应验证")
public class HealthCheckController {

    /**
     * 连通性测试接口。
     *
     * @return 服务当前状态和服务器时间
     */
    @GetMapping("/ping")
    @Operation(summary = "连通性测试")
    public ApiResult<Map<String, Object>> ping() {
        return ApiResult.ok(Map.of(
                "status", "正常",
                "time", OffsetDateTime.now().toString()));
    }
}
