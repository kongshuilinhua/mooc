package com.elysia.mooc.knowledge.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 知识库文档来源类型。 */
@Getter
@RequiredArgsConstructor
public enum KnowledgeDocumentSourceType implements BaseEnum<String> {

    /** 用户上传文件。 */
    UPLOAD("UPLOAD", "上传文件"),

    /** 外部链接。 */
    URL("URL", "外部链接"),

    /** 手工录入。 */
    MANUAL("MANUAL", "手工录入"),

    /** PDF 文档。 */
    PDF("PDF", "PDF 文档"),

    /** Word 文档。 */
    WORD("WORD", "Word 文档"),

    /** Markdown 文档。 */
    MARKDOWN("MARKDOWN", "Markdown 文档"),

    /** 普通文本。 */
    TXT("TXT", "文本");

    /** 落库和接口输出值。 */
    @EnumValue
    private final String value;

    /** 中文说明。 */
    private final String desc;

    @JsonValue
    @Override
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static KnowledgeDocumentSourceType of(Object value) {
        return BaseEnum.parse(KnowledgeDocumentSourceType.class, normalize(value));
    }

    /**
     * 兼容前端 day12 早期小写来源，同时按文件类型收敛到可读的来源编码。
     */
    public static KnowledgeDocumentSourceType fromInputAndFileName(Object value, String fileName) {
        KnowledgeDocumentSourceType explicit = of(value);
        if (explicit == null) {
            explicit = UPLOAD;
        }
        if (explicit != UPLOAD) {
            return explicit;
        }
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return PDF;
        }
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
            return WORD;
        }
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return MARKDOWN;
        }
        if (lower.endsWith(".txt")) {
            return TXT;
        }
        return UPLOAD;
    }

    private static Object normalize(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if ("upload".equalsIgnoreCase(text)) {
            return UPLOAD.value;
        }
        if ("url".equalsIgnoreCase(text)) {
            return URL.value;
        }
        if ("manual".equalsIgnoreCase(text)) {
            return MANUAL.value;
        }
        return value;
    }
}
