package com.elysia.mooc.knowledge.parse;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/** TXT 文档解析器。 */
@Component
public class TextDocumentParser implements DocumentParser {

    @Override
    public boolean supports(KnowledgeDocumentSourceType sourceType) {
        return sourceType == KnowledgeDocumentSourceType.TXT
                || sourceType == KnowledgeDocumentSourceType.MANUAL
                || sourceType == KnowledgeDocumentSourceType.UPLOAD;
    }

    @Override
    public String parse(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "文本文件读取失败，请确认文件编码为UTF-8");
        }
    }
}
