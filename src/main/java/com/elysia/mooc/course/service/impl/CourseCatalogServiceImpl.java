package com.elysia.mooc.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.dto.CreateChapterRequest;
import com.elysia.mooc.course.domain.dto.CreateSectionRequest;
import com.elysia.mooc.course.domain.dto.UpdateChapterRequest;
import com.elysia.mooc.course.domain.dto.UpdateSectionRequest;
import com.elysia.mooc.course.domain.enums.CatalogMutationStatus;
import com.elysia.mooc.course.domain.enums.CatalogNodeType;
import com.elysia.mooc.course.domain.po.CourseChapterPO;
import com.elysia.mooc.course.domain.po.CourseConceptPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.domain.vo.CatalogMutationVO;
import com.elysia.mooc.course.domain.vo.ChapterVO;
import com.elysia.mooc.course.domain.vo.ConceptVO;
import com.elysia.mooc.course.domain.vo.CourseCatalogVO;
import com.elysia.mooc.course.domain.vo.SectionVO;
import com.elysia.mooc.course.mapper.CourseChapterMapper;
import com.elysia.mooc.course.mapper.CourseConceptMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.course.service.CourseCatalogService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/** 课程目录服务实现。 */
@Service
@RequiredArgsConstructor
public class CourseCatalogServiceImpl extends CourseCatalogAccessSupport implements CourseCatalogService {

    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseSectionMapper courseSectionMapper;
    private final CourseConceptMapper courseConceptMapper;

    /**
     * 查询课程目录树。
     *
     * @param courseId 课程 ID
     * @return 课程目录树
     */
    @Override
    public CourseCatalogVO getCourseCatalog(Long courseId) {
        CoursePO course = getCatalogVisibleCourse(courseMapper, courseId);
        List<CourseChapterPO> chapters = listChapters(course.getId());
        List<CourseSectionPO> sections = listSections(course.getId());
        List<CourseConceptPO> concepts = listConcepts(course.getId());

        Map<Long, List<ConceptVO>> sectionConcepts = concepts.stream()
                .filter(concept -> concept.getSectionId() != null)
                .collect(Collectors.groupingBy(
                        CourseConceptPO::getSectionId,
                        LinkedHashMap::new,
                        Collectors.mapping(this::toConceptVO, Collectors.toList())));
        Map<Long, List<SectionVO>> chapterSections = sections.stream()
                .collect(Collectors.groupingBy(
                        CourseSectionPO::getChapterId,
                        LinkedHashMap::new,
                        Collectors.mapping(section -> toSectionVO(section, sectionConcepts), Collectors.toList())));
        List<ConceptVO> courseConcepts = concepts.stream()
                .filter(concept -> concept.getSectionId() == null)
                .map(this::toConceptVO)
                .toList();
        List<ChapterVO> chapterVos = chapters.stream()
                .map(chapter -> toChapterVO(chapter, chapterSections))
                .toList();
        int durationSeconds = sections.stream()
                .map(CourseSectionPO::getDurationSeconds)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        return CourseCatalogVO.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseStatus(course.getStatus())
                .chapterCount(chapters.size())
                .sectionCount(sections.size())
                .durationSeconds(durationSeconds)
                .chapters(chapterVos)
                .concepts(courseConcepts)
                .build();
    }

    /**
     * 创建章节。
     *
     * @param courseId 课程 ID
     * @param request  创建章节请求
     * @return 目录变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogMutationVO createChapter(Long courseId, CreateChapterRequest request) {
        CoursePO course = requireEditableCourse(courseId);
        validateChapterSortAvailable(course.getId(), request.getSort(), null);

        // 新建章节只复制同名业务字段，课程归属和删除默认值显式设置。
        CourseChapterPO chapter = BeanCopyUtils.copyBean(request, CourseChapterPO.class, (source, target) -> {
            target.setCourseId(course.getId());
            target.setDeleted(0);
        });
        courseChapterMapper.insert(chapter);
        touchCourse(course.getId());
        return toMutationVO(chapter.getId(), course.getId(), CatalogNodeType.CHAPTER, CatalogMutationStatus.CREATED,
                chapter.getUpdateTime());
    }

    /**
     * 修改章节。
     *
     * @param chapterId 章节 ID
     * @param request   修改章节请求
     * @return 目录变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogMutationVO updateChapter(Long chapterId, UpdateChapterRequest request) {
        CourseChapterPO chapter = getChapter(chapterId);
        CoursePO course = requireEditableCourse(chapter.getCourseId());
        validateChapterSortAvailable(course.getId(), request.getSort(), chapter.getId());

        BeanCopyUtils.copyProperties(request, chapter);
        courseChapterMapper.updateById(chapter);
        touchCourse(course.getId());
        return toMutationVO(chapter.getId(), course.getId(), CatalogNodeType.CHAPTER, CatalogMutationStatus.UPDATED,
                chapter.getUpdateTime());
    }

    /**
     * 删除章节。
     *
     * @param chapterId 章节 ID
     * @return 目录变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogMutationVO deleteChapter(Long chapterId) {
        CourseChapterPO chapter = getChapter(chapterId);
        CoursePO course = requireEditableCourse(chapter.getCourseId());
        Long sectionCount = courseSectionMapper.selectCount(Wrappers.<CourseSectionPO>lambdaQuery()
                .eq(CourseSectionPO::getChapterId, chapter.getId()));
        if (sectionCount > 0) {
            throw new BizException(CourseErrorCode.CATALOG_CHAPTER_HAS_SECTION);
        }

        courseChapterMapper.deleteById(chapter.getId());
        touchCourse(course.getId());
        return toMutationVO(chapter.getId(), course.getId(), CatalogNodeType.CHAPTER, CatalogMutationStatus.DELETED,
                LocalDateTime.now());
    }

    /**
     * 创建小节。
     *
     * @param chapterId 章节 ID
     * @param request   创建小节请求
     * @return 目录变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogMutationVO createSection(Long chapterId, CreateSectionRequest request) {
        CourseChapterPO chapter = getChapter(chapterId);
        CoursePO course = requireEditableCourse(chapter.getCourseId());
        validateSectionSortAvailable(chapter.getId(), request.getSort(), null);

        // 新建小节默认启用，课程和章节归属由服务端确定。
        CourseSectionPO section = BeanCopyUtils.copyBean(request, CourseSectionPO.class, (source, target) -> {
            target.setCourseId(course.getId());
            target.setChapterId(chapter.getId());
            target.setStatus(EnableStatus.ENABLED);
            target.setDeleted(0);
        });
        courseSectionMapper.insert(section);
        touchCourse(course.getId());
        return toMutationVO(section.getId(), course.getId(), CatalogNodeType.SECTION, CatalogMutationStatus.CREATED,
                section.getUpdateTime());
    }

    /**
     * 修改小节。
     *
     * @param sectionId 小节 ID
     * @param request   修改小节请求
     * @return 目录变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogMutationVO updateSection(Long sectionId, UpdateSectionRequest request) {
        CourseSectionPO section = getSection(sectionId);
        CoursePO course = requireEditableCourse(section.getCourseId());
        validateSectionSortAvailable(section.getChapterId(), request.getSort(), section.getId());

        BeanCopyUtils.copyProperties(request, section);
        courseSectionMapper.updateById(section);
        touchCourse(course.getId());
        return toMutationVO(section.getId(), course.getId(), CatalogNodeType.SECTION, CatalogMutationStatus.UPDATED,
                section.getUpdateTime());
    }

    /**
     * 删除小节。
     *
     * @param sectionId 小节 ID
     * @return 目录变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogMutationVO deleteSection(Long sectionId) {
        CourseSectionPO section = getSection(sectionId);
        CoursePO course = requireEditableCourse(section.getCourseId());
        Long conceptCount = courseConceptMapper.selectCount(Wrappers.<CourseConceptPO>lambdaQuery()
                .eq(CourseConceptPO::getSectionId, section.getId()));
        if (conceptCount > 0) {
            throw new BizException(CourseErrorCode.CATALOG_SECTION_HAS_CONCEPT);
        }

        courseSectionMapper.deleteById(section.getId());
        touchCourse(course.getId());
        return toMutationVO(section.getId(), course.getId(), CatalogNodeType.SECTION, CatalogMutationStatus.DELETED,
                LocalDateTime.now());
    }

    private CoursePO requireEditableCourse(Long courseId) {
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(CourseErrorCode.CATALOG_COURSE_NOT_FOUND);
        }
        requireCatalogMaintainer(course);
        return course;
    }

    private CourseChapterPO getChapter(Long chapterId) {
        if (chapterId == null || chapterId <= 0) {
            throw new BizException(CourseErrorCode.CATALOG_CHAPTER_NOT_FOUND);
        }
        CourseChapterPO chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BizException(CourseErrorCode.CATALOG_CHAPTER_NOT_FOUND);
        }
        return chapter;
    }

    private CourseSectionPO getSection(Long sectionId) {
        if (sectionId == null || sectionId <= 0) {
            throw new BizException(CourseErrorCode.CATALOG_SECTION_NOT_FOUND);
        }
        CourseSectionPO section = courseSectionMapper.selectById(sectionId);
        if (section == null) {
            throw new BizException(CourseErrorCode.CATALOG_SECTION_NOT_FOUND);
        }
        return section;
    }

    private void validateChapterSortAvailable(Long courseId, Integer sort, Long excludeChapterId) {
        LambdaQueryWrapper<CourseChapterPO> wrapper = Wrappers.<CourseChapterPO>lambdaQuery()
                .eq(CourseChapterPO::getCourseId, courseId)
                .eq(CourseChapterPO::getSort, sort);
        if (excludeChapterId != null) {
            wrapper.ne(CourseChapterPO::getId, excludeChapterId);
        }
        if (courseChapterMapper.selectCount(wrapper) > 0) {
            throw new BizException(CourseErrorCode.CATALOG_SORT_DUPLICATED);
        }
    }

    private void validateSectionSortAvailable(Long chapterId, Integer sort, Long excludeSectionId) {
        LambdaQueryWrapper<CourseSectionPO> wrapper = Wrappers.<CourseSectionPO>lambdaQuery()
                .eq(CourseSectionPO::getChapterId, chapterId)
                .eq(CourseSectionPO::getSort, sort);
        if (excludeSectionId != null) {
            wrapper.ne(CourseSectionPO::getId, excludeSectionId);
        }
        if (courseSectionMapper.selectCount(wrapper) > 0) {
            throw new BizException(CourseErrorCode.CATALOG_SORT_DUPLICATED);
        }
    }

    private List<CourseChapterPO> listChapters(Long courseId) {
        return courseChapterMapper.selectList(Wrappers.<CourseChapterPO>lambdaQuery()
                .eq(CourseChapterPO::getCourseId, courseId)
                .orderByAsc(CourseChapterPO::getSort)
                .orderByAsc(CourseChapterPO::getId));
    }

    private List<CourseSectionPO> listSections(Long courseId) {
        return courseSectionMapper.selectList(Wrappers.<CourseSectionPO>lambdaQuery()
                .eq(CourseSectionPO::getCourseId, courseId)
                .orderByAsc(CourseSectionPO::getChapterId)
                .orderByAsc(CourseSectionPO::getSort)
                .orderByAsc(CourseSectionPO::getId));
    }

    private List<CourseConceptPO> listConcepts(Long courseId) {
        return courseConceptMapper.selectList(Wrappers.<CourseConceptPO>lambdaQuery()
                .eq(CourseConceptPO::getCourseId, courseId)
                .orderByAsc(CourseConceptPO::getSectionId)
                .orderByAsc(CourseConceptPO::getSort)
                .orderByAsc(CourseConceptPO::getStartSecond)
                .orderByAsc(CourseConceptPO::getId));
    }

    private ChapterVO toChapterVO(CourseChapterPO chapter, Map<Long, List<SectionVO>> chapterSections) {
        return BeanCopyUtils.copyBean(chapter, ChapterVO.class, (source, target) ->
                target.setSections(chapterSections.getOrDefault(source.getId(), Collections.emptyList())));
    }

    private SectionVO toSectionVO(CourseSectionPO section, Map<Long, List<ConceptVO>> sectionConcepts) {
        return BeanCopyUtils.copyBean(section, SectionVO.class, (source, target) ->
                target.setConcepts(sectionConcepts.getOrDefault(source.getId(), Collections.emptyList())));
    }

    private ConceptVO toConceptVO(CourseConceptPO concept) {
        return BeanCopyUtils.copyBean(concept, ConceptVO.class);
    }

    private CatalogMutationVO toMutationVO(
            Long id,
            Long courseId,
            CatalogNodeType type,
            CatalogMutationStatus status,
            LocalDateTime updateTime) {
        return CatalogMutationVO.builder()
                .id(id)
                .courseId(courseId)
                .type(type)
                .status(status)
                .updateTime(updateTime == null ? LocalDateTime.now() : updateTime)
                .build();
    }

    private void touchCourse(Long courseId) {
        CoursePO update = new CoursePO();
        update.setId(courseId);
        courseMapper.updateById(update);
    }
}
