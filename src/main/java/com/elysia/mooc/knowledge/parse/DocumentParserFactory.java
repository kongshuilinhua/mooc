package com.elysia.mooc.knowledge.parse;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 文档解析器工厂。 */
@Component
@RequiredArgsConstructor
public class DocumentParserFactory {

    private final List<DocumentParser> parsers;

    /**
     * 根据文档来源类型选择解析器。
     *
     * @param sourceType 文档来源类型
     * @return 匹配的解析器
     */
    public DocumentParser getParser(KnowledgeDocumentSourceType sourceType) {
        return parsers.stream()
                .filter(parser -> parser.supports(sourceType))
                .findFirst()
                .orElseThrow(() -> new BizException(
                        KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID,
                        "暂不支持该文档类型解析：" + (sourceType == null ? "未知类型" : sourceType.getValue())));
    }
}
