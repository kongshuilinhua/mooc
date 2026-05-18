package com.elysia.mooc.studyarchive.domain.vo;

import com.elysia.mooc.studyarchive.domain.enums.WrongBookMasteryLevel;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookSourceType;
import java.time.LocalDateTime;
import lombok.Data;

/** 错题本条目响应。 */
@Data
public class WrongBookItemVO {

    /** 错题本记录 ID。 */
    private Long id;

    /** 题目 ID。 */
    private Long questionId;

    /** 来源类型。 */
    private WrongBookSourceType sourceType;

    /** 来源类型说明。 */
    private String sourceTypeDesc;

    /** 做错次数。 */
    private Integer wrongCount;

    /** 掌握程度。 */
    private WrongBookMasteryLevel masteryLevel;

    /** 掌握程度说明。 */
    private String masteryLevelDesc;

    /** 建议复习等级。 */
    private String reviewLevel;

    /** 最近做错时间。 */
    private LocalDateTime lastWrongTime;
}
