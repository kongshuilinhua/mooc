package com.elysia.mooc.course.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 创建知识点请求参数。 */
@Data
public class CreateConceptRequest implements Checker {

    /** 知识点标题。 */
    @NotBlank(message = "知识点标题不能为空")
    @Size(max = 128, message = "知识点标题不能超过128个字符")
    private String title;

    /** 知识点说明。 */
    @Size(max = 5000, message = "知识点说明不能超过5000个字符")
    private String content;

    /** 关联小节 ID，空值表示课程级知识点。 */
    private Long sectionId;

    /** 视频内开始秒数。 */
    private Integer startSecond;

    /** 视频内结束秒数。 */
    private Integer endSecond;

    /** 排序。 */
    private Integer sort;

    @Override
    public void check() {
        this.title = title == null ? null : title.trim();
        this.content = StringUtils.hasText(content) ? content.trim() : null;
        this.sort = sort == null ? 0 : sort;
        if (sectionId != null && sectionId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "小节ID必须为正数");
        }
        if (startSecond != null && startSecond < 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "知识点开始秒数不能小于0");
        }
        if (endSecond != null && endSecond < 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "知识点结束秒数不能小于0");
        }
        if (startSecond != null && endSecond != null && endSecond < startSecond) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "知识点结束秒数不能小于开始秒数");
        }
        if (sort < 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "知识点排序不能小于0");
        }
    }
}
