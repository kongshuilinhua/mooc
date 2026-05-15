package com.elysia.mooc.knowledge.parse;

import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Markdown 文档解析器。 */
@Component
@RequiredArgsConstructor
public class MarkdownDocumentParser implements DocumentParser {

    private final TextDocumentParser textDocumentParser;

    @Override
    public boolean supports(KnowledgeDocumentSourceType sourceType) {
        return sourceType == KnowledgeDocumentSourceType.MARKDOWN;
    }

    @Override
    public String parse(Path path) {
        String text = textDocumentParser.parse(path);
        return text.replaceAll("(?s)```.*?```", " ")
                .replaceAll("`([^`]*)`", "$1")
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("\\[([^]]+)]\\([^)]*\\)", "$1")
                .replaceAll("^\\s*>\\s?", "")
                .replaceAll("[*_~]{1,3}", "")
                .trim();
    }
}
