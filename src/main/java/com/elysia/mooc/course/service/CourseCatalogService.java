package com.elysia.mooc.course.service;

import com.elysia.mooc.course.domain.dto.CreateChapterRequest;
import com.elysia.mooc.course.domain.dto.CreateConceptRequest;
import com.elysia.mooc.course.domain.dto.CreateSectionRequest;
import com.elysia.mooc.course.domain.dto.UpdateChapterRequest;
import com.elysia.mooc.course.domain.dto.UpdateSectionRequest;
import com.elysia.mooc.course.domain.vo.CatalogMutationVO;
import com.elysia.mooc.course.domain.vo.CourseCatalogVO;

/** 课程目录服务。 */
public interface CourseCatalogService {

    /**
     * 查询课程目录树。
     *
     * @param courseId 课程 ID
     * @return 课程目录树
     */
    CourseCatalogVO getCourseCatalog(Long courseId);

    /**
     * 创建章节。
     *
     * @param courseId 课程 ID
     * @param request  创建章节请求
     * @return 目录变更结果
     */
    CatalogMutationVO createChapter(Long courseId, CreateChapterRequest request);

    /**
     * 修改章节。
     *
     * @param chapterId 章节 ID
     * @param request   修改章节请求
     * @return 目录变更结果
     */
    CatalogMutationVO updateChapter(Long chapterId, UpdateChapterRequest request);

    /**
     * 删除章节。
     *
     * @param chapterId 章节 ID
     * @return 目录变更结果
     */
    CatalogMutationVO deleteChapter(Long chapterId);

    /**
     * 创建小节。
     *
     * @param chapterId 章节 ID
     * @param request   创建小节请求
     * @return 目录变更结果
     */
    CatalogMutationVO createSection(Long chapterId, CreateSectionRequest request);

    /**
     * 修改小节。
     *
     * @param sectionId 小节 ID
     * @param request   修改小节请求
     * @return 目录变更结果
     */
    CatalogMutationVO updateSection(Long sectionId, UpdateSectionRequest request);

    /**
     * 删除小节。
     *
     * @param sectionId 小节 ID
     * @return 目录变更结果
     */
    CatalogMutationVO deleteSection(Long sectionId);
}
