package com.elysia.mooc.course.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.constants.CourseConstants;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.dto.CreateChapterRequest;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CourseChapterPO;
import com.elysia.mooc.course.domain.po.CourseConceptPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.domain.vo.CourseCatalogVO;
import com.elysia.mooc.course.mapper.CourseChapterMapper;
import com.elysia.mooc.course.mapper.CourseConceptMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/** 课程目录服务单元测试。 */
@ExtendWith(MockitoExtension.class)
class CourseCatalogServiceImplTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseChapterMapper courseChapterMapper;

    @Mock
    private CourseSectionMapper courseSectionMapper;

    @Mock
    private CourseConceptMapper courseConceptMapper;

    @InjectMocks
    private CourseCatalogServiceImpl courseCatalogService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCourseCatalogShouldReturnTree() {
        CoursePO course = course(3001L, 2L, CourseStatus.PUBLISHED);
        when(courseMapper.selectById(3001L)).thenReturn(course);
        when(courseChapterMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(chapter(4001L, 3001L), chapter(4002L, 3001L)));
        when(courseSectionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(section(5001L, 3001L, 4001L, 900), section(5002L, 3001L, 4002L, 1200)));
        when(courseConceptMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(
                        concept(6001L, 3001L, 5001L, 1),
                        concept(6002L, 3001L, null, 2)));

        CourseCatalogVO catalog = courseCatalogService.getCourseCatalog(3001L);

        assertThat(catalog.getCourseId()).isEqualTo(3001L);
        assertThat(catalog.getChapterCount()).isEqualTo(2);
        assertThat(catalog.getSectionCount()).isEqualTo(2);
        assertThat(catalog.getDurationSeconds()).isEqualTo(2100);
        assertThat(catalog.getChapters()).hasSize(2);
        assertThat(catalog.getChapters().get(0).getSections()).hasSize(1);
        assertThat(catalog.getChapters().get(0).getSections().get(0).getConcepts()).hasSize(1);
        assertThat(catalog.getConcepts()).hasSize(1);
    }

    @Test
    void deleteChapterShouldRejectWhenSectionExists() {
        setTeacherLogin(2L);
        when(courseChapterMapper.selectById(4001L)).thenReturn(chapter(4001L, 3001L));
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, CourseStatus.DRAFT));
        when(courseSectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> courseCatalogService.deleteChapter(4001L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(CourseErrorCode.CATALOG_CHAPTER_HAS_SECTION.code());
    }

    @Test
    void createChapterShouldRejectOtherTeacherCourse() {
        setTeacherLogin(2L);
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 9L, CourseStatus.DRAFT));

        CreateChapterRequest request = new CreateChapterRequest();
        request.setTitle("新章节");
        request.setSort(1);

        assertThatThrownBy(() -> courseCatalogService.createChapter(3001L, request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(CourseErrorCode.CATALOG_FORBIDDEN.code());
    }

    @Test
    void createChapterShouldRejectStudentRole() {
        setStudentLogin(3L);
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 3L, CourseStatus.DRAFT));

        CreateChapterRequest request = new CreateChapterRequest();
        request.setTitle("学生章节");
        request.setSort(1);

        assertThatThrownBy(() -> courseCatalogService.createChapter(3001L, request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(CourseErrorCode.CATALOG_FORBIDDEN.code());
    }

    @Test
    void createChapterShouldInsertWhenTeacherOwnsDraftCourse() {
        setTeacherLogin(2L);
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, CourseStatus.DRAFT));
        when(courseChapterMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        CreateChapterRequest request = new CreateChapterRequest();
        request.setTitle("新章节");
        request.setSort(1);

        courseCatalogService.createChapter(3001L, request);

        verify(courseChapterMapper).insert(any(CourseChapterPO.class));
    }

    private void setTeacherLogin(Long userId) {
        LoginUser loginUser = new LoginUser(
                userId,
                "teacher",
                List.of(CourseConstants.ROLE_TEACHER),
                List.of(CourseConstants.PERMISSION_COURSE_PUBLISH));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
    }

    private void setStudentLogin(Long userId) {
        LoginUser loginUser = new LoginUser(userId, "student", List.of("STUDENT"), List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
    }

    private CoursePO course(Long id, Long teacherId, CourseStatus status) {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setTitle("测试课程");
        course.setTeacherId(teacherId);
        course.setStatus(status);
        return course;
    }

    private CourseChapterPO chapter(Long id, Long courseId) {
        CourseChapterPO chapter = new CourseChapterPO();
        chapter.setId(id);
        chapter.setCourseId(courseId);
        chapter.setTitle("章节" + id);
        chapter.setSort(1);
        return chapter;
    }

    private CourseSectionPO section(Long id, Long courseId, Long chapterId, Integer durationSeconds) {
        CourseSectionPO section = new CourseSectionPO();
        section.setId(id);
        section.setCourseId(courseId);
        section.setChapterId(chapterId);
        section.setTitle("小节" + id);
        section.setDurationSeconds(durationSeconds);
        section.setFreePreview(Boolean.TRUE);
        section.setSort(1);
        section.setStatus(EnableStatus.ENABLED);
        return section;
    }

    private CourseConceptPO concept(Long id, Long courseId, Long sectionId, Integer sort) {
        CourseConceptPO concept = new CourseConceptPO();
        concept.setId(id);
        concept.setCourseId(courseId);
        concept.setSectionId(sectionId);
        concept.setTitle("知识点" + id);
        concept.setSort(sort);
        return concept;
    }
}
