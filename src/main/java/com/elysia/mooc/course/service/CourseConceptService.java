package com.elysia.mooc.course.service;

import com.elysia.mooc.course.domain.dto.CreateConceptRequest;
import com.elysia.mooc.course.domain.vo.CatalogMutationVO;

/** 课程知识点服务。 */
public interface CourseConceptService {

    /**
     * 创建课程知识点。
     *
     * @param courseId 课程 ID
     * @param request  创建知识点请求
     * @return 目录变更结果
     */
    CatalogMutationVO createConcept(Long courseId, CreateConceptRequest request);
}
