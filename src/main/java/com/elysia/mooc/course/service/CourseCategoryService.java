package com.elysia.mooc.course.service;

import com.elysia.mooc.course.domain.vo.CourseCategoryVO;
import java.util.List;

/** 课程分类服务。 */
public interface CourseCategoryService {

    /**
     * 查询启用分类树。
     *
     * @return 分类树
     */
    List<CourseCategoryVO> listCategoryTree();
}
