package com.elysia.mooc.media.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 媒资本地存储配置。 */
@Data
@ConfigurationProperties(prefix = "mooc.storage")
public class MediaStorageProperties {

    /** 本地存储根路径。 */
    private String rootPath = "D:/mooc-storage";

    /** 公开访问前缀。 */
    private String publicPrefix = "/files";

    /** 单文件最大字节数。 */
    private long maxFileSize = 536870912L;

    /** 允许上传的 MIME 类型。 */
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "video/mp4",
            "application/pdf",
            "text/plain",
            "text/markdown",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
}
