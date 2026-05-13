package com.elysia.mooc.course.service;

import com.elysia.mooc.course.domain.dto.CourseTagQuery;
import com.elysia.mooc.course.domain.vo.CourseTagVO;
import java.util.List;

/** 课程标签服务。 */
public interface CourseTagService {

    /**
     * 查询启用标签列表。
     *
     * @param query 查询条件
     * @return 标签列表
     */
    List<CourseTagVO> listTags(CourseTagQuery query);
}
