package com.elysia.mooc.interaction.domain.vo;

import com.elysia.mooc.interaction.domain.enums.QuestionStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 问题列表展示项。 */
@Data
@Builder
public class QuestionItemVO {

    /** 问题 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 提问人 ID。 */
    private Long userId;

    /** 问题标题。 */
    private String title;

    /** 问题内容。 */
    private String content;

    /** 回答数。 */
    private Integer answerCount;

    /** 点赞数。 */
    private Integer likeCount;

    /** 问题状态。 */
    private QuestionStatus status;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 回答列表，列表接口返回当前问题下的正常回答。 */
    private List<AnswerItemVO> answers;
}
