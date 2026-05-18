package com.elysia.mooc.studyarchive.domain.dto;

import com.elysia.mooc.studyarchive.domain.enums.LearningNoteStatus;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteType;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/** 保存学习笔记请求。 */
@Data
public class CreateLearningNoteRequest {

    /** 课程 ID，必须是学生已加入的课程。 */
    @NotNull(message = "课程ID不能为空")
    @Positive(message = "课程ID必须为正数")
    private Long courseId;

    /** 小节 ID，兼容前端旧字段 videoId。 */
    @Positive(message = "小节ID必须为正数")
    @JsonAlias("videoId")
    private Long sectionId;

    /** 笔记正文，当前 SQL 使用 TEXT 字段存储。 */
    @NotBlank(message = "笔记内容不能为空")
    @Size(max = 5000, message = "笔记内容不能超过5000个字符")
    private String content;

    /** 兼容前端旧字段，若传入则合并保存到 content 文本中。 */
    @Size(max = 100, message = "笔记标题不能超过100个字符")
    private String title;

    /** 兼容前端旧字段，当前不扩表，保存为正文附加信息。 */
    @Size(max = 20, message = "笔记标签不能超过20个")
    private List<@Size(max = 30, message = "单个标签不能超过30个字符") String> tags;

    /** 笔记类型，不传默认 TEXT。 */
    private LearningNoteType noteType;

    /** 笔记状态，不传默认 NORMAL。 */
    private LearningNoteStatus status;
}
