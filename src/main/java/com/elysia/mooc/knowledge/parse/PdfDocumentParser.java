package com.elysia.mooc.knowledge.parse;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/** PDF 文档解析器。 */
@Component
public class PdfDocumentParser implements DocumentParser {

    @Override
    public boolean supports(KnowledgeDocumentSourceType sourceType) {
        return sourceType == KnowledgeDocumentSourceType.PDF;
    }

    @Override
    public String parse(Path path) {
        try (var document = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(document);
        } catch (Exception ex) {
            throw new BizException(KnowledgeErrorCode.KNOWLEDGE_PARAM_INVALID, "PDF文档解析失败，请确认文件未损坏");
        }
    }
}
