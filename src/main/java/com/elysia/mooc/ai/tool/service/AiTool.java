package com.elysia.mooc.ai.tool.service;

import com.elysia.mooc.auth.security.LoginUser;
import java.util.Map;

/** 受控 AI 工具接口，所有工具必须通过 ToolRegistry 调用。 */
public interface AiTool<T> {

    /**
     * 获取工具名称。
     *
     * @return 工具名称，统一使用 Java 类名风格
     */
    String name();

    /**
     * 获取参数类型，用于注册表统一做 JSON 解析和校验。
     *
     * @return 参数类型
     */
    Class<T> argumentType();

    /**
     * 执行只读工具。
     *
     * @param arguments 强类型参数
     * @param loginUser 当前登录用户
     * @return 摘要化结果
     */
    Map<String, Object> execute(T arguments, LoginUser loginUser);

    /**
     * 根据工具结果生成短摘要，给模型和前端展示。
     *
     * @param result 工具结果
     * @return 中文摘要
     */
    String summarize(Map<String, Object> result);
}
