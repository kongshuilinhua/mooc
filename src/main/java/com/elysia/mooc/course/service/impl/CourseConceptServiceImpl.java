package com.elysia.mooc.course.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.dto.CreateConceptRequest;
import com.elysia.mooc.course.domain.enums.CatalogMutationStatus;
import com.elysia.mooc.course.domain.enums.CatalogNodeType;
import com.elysia.mooc.course.domain.po.CourseConceptPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.domain.vo.CatalogMutationVO;
import com.elysia.mooc.course.mapper.CourseConceptMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.course.service.CourseConceptService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 课程知识点服务实现。 */
@Service
@RequiredArgsConstructor
public class CourseConceptServiceImpl extends CourseCatalogAccessSupport implements CourseConceptService {

    private final CourseMapper courseMapper;
    private final CourseSectionMapper courseSectionMapper;
    private final CourseConceptMapper courseConceptMapper;

    /**
     * 创建课程知识点。
     *
     * @param courseId 课程 ID
     * @param request  创建知识点请求
     * @return 目录变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogMutationVO createConcept(Long courseId, CreateConceptRequest request) {
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(CourseErrorCode.CATALOG_COURSE_NOT_FOUND);
        }
        requireCatalogMaintainer(course);
        if (request.getSectionId() != null) {
            validateSectionBelongsToCourse(course.getId(), request.getSectionId());
        }

        // 知识点课程归属由路径决定，避免前端伪造 courseId。
        CourseConceptPO concept = BeanCopyUtils.copyBean(request, CourseConceptPO.class, (source, target) -> {
            target.setCourseId(course.getId());
            target.setDeleted(0);
        });
        courseConceptMapper.insert(concept);
        touchCourse(course.getId());
        return CatalogMutationVO.builder()
                .id(concept.getId())
                .courseId(course.getId())
                .type(CatalogNodeType.CONCEPT)
                .status(CatalogMutationStatus.CREATED)
                .updateTime(concept.getUpdateTime() == null ? LocalDateTime.now() : concept.getUpdateTime())
                .build();
    }

    private void validateSectionBelongsToCourse(Long courseId, Long sectionId) {
        CourseSectionPO section = courseSectionMapper.selectOne(Wrappers.<CourseSectionPO>lambdaQuery()
                .eq(CourseSectionPO::getId, sectionId)
                .eq(CourseSectionPO::getCourseId, courseId));
        if (section == null) {
            throw new BizException(CourseErrorCode.CATALOG_SECTION_NOT_FOUND);
        }
    }

    private void touchCourse(Long courseId) {
        CoursePO update = new CoursePO();
        update.setId(courseId);
        courseMapper.updateById(update);
    }
}
