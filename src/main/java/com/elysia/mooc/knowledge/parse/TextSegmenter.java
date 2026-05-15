package com.elysia.mooc.knowledge.parse;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 文本切片器，按最大长度和重叠窗口生成 RAG 前置切片。 */
@Component
@RequiredArgsConstructor
public class TextSegmenter {

    private final SegmentConfig segmentConfig;

    /**
     * 将纯文本切成可检索片段。
     *
     * @param text 原始纯文本
     * @return 切片列表
     */
    public List<TextSegment> segment(String text) {
        String cleaned = normalize(text);
        if (!StringUtils.hasText(cleaned)) {
            return List.of();
        }
        int maxLength = segmentConfig.safeMaxLength();
        int overlapLength = segmentConfig.safeOverlapLength();
        List<String> windows = splitByWindow(cleaned, maxLength, overlapLength);
        List<TextSegment> segments = new ArrayList<>(windows.size());
        for (int index = 0; index < windows.size(); index++) {
            String content = windows.get(index);
            segments.add(new TextSegment(
                    index + 1,
                    resolveTitle(content),
                    content,
                    estimateTokenCount(content),
                    metadata(index + 1, maxLength, overlapLength)));
        }
        return segments;
    }

    private List<String> splitByWindow(String text, int maxLength, int overlapLength) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLength, text.length());
            int preferredEnd = preferParagraphBoundary(text, start, end, maxLength);
            String content = text.substring(start, preferredEnd).trim();
            if (StringUtils.hasText(content)) {
                result.add(content);
            }
            if (preferredEnd >= text.length()) {
                break;
            }
            start = Math.max(0, preferredEnd - overlapLength);
            if (start >= preferredEnd) {
                start = preferredEnd;
            }
        }
        return result;
    }

    private int preferParagraphBoundary(String text, int start, int end, int maxLength) {
        if (end >= text.length()) {
            return end;
        }
        // 切片优先在段落或句子边界截断，降低 overlap 把句子硬切断的概率。
        int minEnd = start + Math.max(1, maxLength / 2);
        int paragraph = text.lastIndexOf("\n\n", end);
        if (paragraph >= minEnd) {
            return paragraph;
        }
        int line = text.lastIndexOf('\n', end);
        if (line >= minEnd) {
            return line;
        }
        int sentence = Math.max(text.lastIndexOf('。', end), text.lastIndexOf('.', end));
        if (sentence >= minEnd) {
            return sentence + 1;
        }
        return end;
    }

    private String normalize(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[\\t\\x0B\\f]+", " ")
                .replaceAll(" {2,}", " ")
                .replaceAll("(?m)^\\s+$", "")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String resolveTitle(String content) {
        String firstLine = content.lines()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("文档片段");
        String normalized = firstLine.replaceFirst("^#{1,6}\\s*", "").trim();
        if (!StringUtils.hasText(normalized)) {
            return "文档片段";
        }
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }

    private int estimateTokenCount(String content) {
        if (!StringUtils.hasText(content)) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(content.trim().length() / 2.0));
    }

    private String metadata(int segmentIndex, int maxLength, int overlapLength) {
        return "{\"segmentIndex\":" + segmentIndex
                + ",\"maxLength\":" + maxLength
                + ",\"overlapLength\":" + overlapLength + "}";
    }
}
