package com.elysia.mooc.course.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 课程标签查询参数。 */
@Data
public class CourseTagQuery {

    /** 标签名称搜索关键词。 */
    @Size(max = 50, message = "标签关键词不能超过50个字符")
    private String keyword;
}
