package com.elysia.mooc.auth.controller;

import com.elysia.mooc.auth.service.AuthService;
import com.elysia.mooc.auth.domain.vo.CurrentUserVO;
import com.elysia.mooc.common.api.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口控制器，提供当前用户信息查询。
 */
@Tag(name = "用户接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /**
     * 查询当前登录用户。
     *
     * @return 当前用户信息
     */
    @Operation(summary = "当前用户")
    @GetMapping("/me")
    public ApiResult<CurrentUserVO> currentUser() {
        return ApiResult.ok(authService.currentUser());
    }
}
