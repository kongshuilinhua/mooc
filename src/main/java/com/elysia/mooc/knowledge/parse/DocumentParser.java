package com.elysia.mooc.knowledge.parse;

import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import java.nio.file.Path;

/** 文档解析器。 */
public interface DocumentParser {

    /**
     * 判断是否支持指定来源类型。
     *
     * @param sourceType 文档来源类型
     * @return true 表示支持
     */
    boolean supports(KnowledgeDocumentSourceType sourceType);

    /**
     * 解析文档为纯文本。
     *
     * @param path 本地文件路径
     * @return 纯文本内容
     */
    String parse(Path path);
}
