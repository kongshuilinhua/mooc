package com.elysia.mooc.knowledge.qdrant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 启动后尝试初始化 Qdrant Collection。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QdrantCollectionInitializer implements ApplicationRunner {

    private final QdrantProperties properties;
    private final QdrantClient qdrantClient;

    /**
     * 启动后检查 Collection。
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isAutoInitialize()) {
            return;
        }
        try {
            qdrantClient.ensureCollection();
        } catch (RuntimeException ex) {
            // Qdrant 不可用不能影响课程等主流程启动；真实向量化接口会返回明确 502。
            log.warn("Qdrant Collection 初始化失败，后续向量化接口会继续校验：{}", ex.getMessage());
        }
    }
}
