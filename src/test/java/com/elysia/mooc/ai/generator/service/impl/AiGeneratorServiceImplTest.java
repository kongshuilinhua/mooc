package com.elysia.mooc.ai.generator.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.elysia.mooc.ai.generator.constants.AiGeneratorConstants;
import com.elysia.mooc.ai.generator.constants.AiGeneratorErrorCode;
import com.elysia.mooc.ai.generator.domain.dto.GenerateLearningPathRequest;
import com.elysia.mooc.ai.generator.domain.dto.GenerateQuestionsRequest;
import com.elysia.mooc.ai.generator.domain.enums.AiQuestionReviewStatus;
import com.elysia.mooc.ai.generator.domain.po.AiGenerationTaskPO;
import com.elysia.mooc.ai.generator.domain.po.AiLearningPathPO;
import com.elysia.mooc.ai.generator.domain.po.AiQuestionDraftPO;
import com.elysia.mooc.ai.generator.domain.vo.ChapterSummaryResultVO;
import com.elysia.mooc.ai.generator.domain.vo.GeneratedQuestionsResultVO;
import com.elysia.mooc.ai.generator.domain.vo.LearningPathResultVO;
import com.elysia.mooc.ai.generator.mapper.AiGenerationTaskMapper;
import com.elysia.mooc.ai.generator.mapper.AiLearningPathMapper;
import com.elysia.mooc.ai.generator.mapper.AiQuestionDraftMapper;
import com.elysia.mooc.ai.model.AiChatClient;
import com.elysia.mooc.ai.model.AiChatProperties;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import com.elysia.mooc.auth.mapper.SysUserMapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.po.CourseChapterPO;
import com.elysia.mooc.course.domain.po.CourseConceptPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseChapterMapper;
import com.elysia.mooc.course.mapper.CourseConceptMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.learning.mapper.LearningRecordMapper;
import com.elysia.mooc.studyarchive.mapper.LearningWrongBookMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** AI 生成服务单元测试。 */
@ExtendWith(MockitoExtension.class)
class AiGeneratorServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseChapterMapper courseChapterMapper;

    @Mock
    private CourseSectionMapper courseSectionMapper;

    @Mock
    private CourseConceptMapper courseConceptMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private LearningCourseMapper learningCourseMapper;

    @Mock
    private LearningRecordMapper learningRecordMapper;

    @Mock
    private LearningWrongBookMapper learningWrongBookMapper;

    @Mock
    private AiGenerationTaskMapper generationTaskMapper;

    @Mock
    private AiQuestionDraftMapper questionDraftMapper;

    @Mock
    private AiLearningPathMapper learningPathMapper;

    @Mock
    private AiChatClient aiChatClient;

    private AiGeneratorServiceImpl aiGeneratorService;

    @BeforeEach
    void setUp() {
        AiChatProperties properties = new AiChatProperties();
        properties.setModel("qwen-plus");
        aiGeneratorService = new AiGeneratorServiceImpl(
                userContextService,
                courseMapper,
                courseChapterMapper,
                courseSectionMapper,
                courseConceptMapper,
                sysUserMapper,
                learningCourseMapper,
                learningRecordMapper,
                learningWrongBookMapper,
                generationTaskMapper,
                questionDraftMapper,
                learningPathMapper,
                aiChatClient,
                properties,
                new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void generateChapterSummaryShouldSaveTaskAndReturnFallbackSummary() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(courseChapterMapper.selectById(4002L)).thenReturn(chapter(4002L, 3001L, "认证与权限"));
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, "Spring Boot 实战"));
        when(courseSectionMapper.selectList(any())).thenReturn(List.of(section(5001L, 4002L, "JWT 登录")));
        when(courseConceptMapper.selectList(any())).thenReturn(List.of(concept(6001L, "JWT"), concept(6002L, "RBAC")));
        assignIdOnTaskInsert(28011L);

        ChapterSummaryResultVO result = aiGeneratorService.generateChapterSummary(4002L, null);

        assertThat(result.getTaskId()).isEqualTo(28011L);
        assertThat(result.getChapterId()).isEqualTo(4002L);
        assertThat(result.getSummary()).contains("认证与权限");
        assertThat(result.getKeyPoints()).contains("JWT", "RBAC");
        assertThat(result.getSources()).isNotEmpty();
        assertThat(result.getGenerationSource()).isEqualTo(AiGeneratorConstants.FALLBACK_SOURCE);
        verify(generationTaskMapper).insert(any(AiGenerationTaskPO.class));
        verify(generationTaskMapper).updateById(any(AiGenerationTaskPO.class));
    }

    @Test
    void generateQuestionDraftsShouldPersistPendingDrafts() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, "Spring Boot 实战"));
        when(courseConceptMapper.selectList(any())).thenReturn(List.of(concept(6001L, "RBAC")));
        assignIdOnTaskInsert(28012L);
        assignIdOnQuestionInsert();
        GenerateQuestionsRequest request = new GenerateQuestionsRequest();
        request.setQuestionCount(2);
        request.setDifficulty(ExamDifficulty.MEDIUM);
        request.setQuestionType(ExamQuestionType.SINGLE);

        GeneratedQuestionsResultVO result = aiGeneratorService.generateQuestionDrafts(3001L, request);

        assertThat(result.getTaskId()).isEqualTo(28012L);
        assertThat(result.getQuestionCount()).isEqualTo(2);
        assertThat(result.getReviewStatus()).isEqualTo(AiQuestionReviewStatus.PENDING);
        assertThat(result.getQuestions()).hasSize(2);
        assertThat(result.getQuestions()).allSatisfy(question -> {
            assertThat(question.getDraftId()).isNotNull();
            assertThat(question.getReviewStatus()).isEqualTo(AiQuestionReviewStatus.PENDING);
        });
        verify(questionDraftMapper, times(2)).insert(any(AiQuestionDraftPO.class));
    }

    @Test
    void teacherShouldNotGenerateQuestionsForOtherTeacherCourse() {
        when(userContextService.currentLoginUser()).thenReturn(teacher());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 9L, "他人课程"));

        assertThatThrownBy(() -> aiGeneratorService.generateQuestionDrafts(3001L, new GenerateQuestionsRequest()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(AiGeneratorErrorCode.AI_GENERATOR_COURSE_FORBIDDEN.code());
    }

    @Test
    void generateLearningPathShouldPersistPathForSelfStudent() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(sysUserMapper.selectById(4L)).thenReturn(user(4L, "student"));
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, 2L, "Spring Boot 实战"));
        when(learningCourseMapper.selectList(any())).thenReturn(List.of(learningCourse(4L, 3001L)));
        when(learningRecordMapper.selectList(any())).thenReturn(List.of());
        when(learningWrongBookMapper.selectList(any())).thenReturn(List.of());
        assignIdOnTaskInsert(28013L);
        assignIdOnPathInsert(28213L);
        GenerateLearningPathRequest request = new GenerateLearningPathRequest();
        request.setTarget("掌握 Spring Boot 核心能力");
        request.setTargetCourseId(3001L);
        request.setHorizonDays(30);

        LearningPathResultVO result = aiGeneratorService.generateLearningPath(4L, request);

        assertThat(result.getPathId()).isEqualTo(28213L);
        assertThat(result.getStudentId()).isEqualTo(4L);
        assertThat(result.getTheme()).contains("Spring Boot 实战");
        assertThat(result.getStages()).hasSize(3);
        assertThat(result.getExpireTime()).isNotNull();
        verify(learningPathMapper).insert(any(AiLearningPathPO.class));
    }

    @Test
    void studentShouldNotGenerateOtherStudentLearningPath() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(sysUserMapper.selectById(5L)).thenReturn(user(5L, "other"));
        GenerateLearningPathRequest request = new GenerateLearningPathRequest();
        request.setTarget("补齐基础");

        assertThatThrownBy(() -> aiGeneratorService.generateLearningPath(5L, request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(AiGeneratorErrorCode.AI_GENERATOR_FORBIDDEN.code());
    }

    @Test
    void learningPathShouldRejectEmptyContextWithoutTargetCourse() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(sysUserMapper.selectById(4L)).thenReturn(user(4L, "student"));
        when(learningCourseMapper.selectList(any())).thenReturn(List.of());
        when(learningRecordMapper.selectList(any())).thenReturn(List.of());
        when(learningWrongBookMapper.selectList(any())).thenReturn(List.of());
        assignIdOnTaskInsert(28014L);
        GenerateLearningPathRequest request = new GenerateLearningPathRequest();
        request.setTarget("补齐基础");

        assertThatThrownBy(() -> aiGeneratorService.generateLearningPath(4L, request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(AiGeneratorErrorCode.AI_GENERATOR_LEARNING_DATA_NOT_ENOUGH.code());
        verify(generationTaskMapper).updateById(any(AiGenerationTaskPO.class));
    }

    private void assignIdOnTaskInsert(Long id) {
        org.mockito.Mockito.doAnswer(invocation -> {
            AiGenerationTaskPO task = invocation.getArgument(0);
            task.setId(id);
            return 1;
        }).when(generationTaskMapper).insert(any(AiGenerationTaskPO.class));
    }

    private void assignIdOnQuestionInsert() {
        AtomicLong idGenerator = new AtomicLong(28100L);
        org.mockito.Mockito.doAnswer(invocation -> {
            AiQuestionDraftPO draft = invocation.getArgument(0);
            draft.setId(idGenerator.incrementAndGet());
            return 1;
        }).when(questionDraftMapper).insert(any(AiQuestionDraftPO.class));
    }

    private void assignIdOnPathInsert(Long id) {
        org.mockito.Mockito.doAnswer(invocation -> {
            AiLearningPathPO path = invocation.getArgument(0);
            path.setId(id);
            return 1;
        }).when(learningPathMapper).insert(any(AiLearningPathPO.class));
    }

    private LoginUser teacher() {
        return new LoginUser(2L, "teacher", List.of("TEACHER"), List.of());
    }

    private LoginUser student() {
        return new LoginUser(4L, "student", List.of("STUDENT"), List.of());
    }

    private CoursePO course(Long id, Long teacherId, String title) {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setTeacherId(teacherId);
        course.setTitle(title);
        return course;
    }

    private CourseChapterPO chapter(Long id, Long courseId, String title) {
        CourseChapterPO chapter = new CourseChapterPO();
        chapter.setId(id);
        chapter.setCourseId(courseId);
        chapter.setTitle(title);
        chapter.setSummary("章节简介");
        return chapter;
    }

    private CourseSectionPO section(Long id, Long chapterId, String title) {
        CourseSectionPO section = new CourseSectionPO();
        section.setId(id);
        section.setChapterId(chapterId);
        section.setTitle(title);
        section.setDurationSeconds(300);
        return section;
    }

    private CourseConceptPO concept(Long id, String title) {
        CourseConceptPO concept = new CourseConceptPO();
        concept.setId(id);
        concept.setTitle(title);
        concept.setContent(title + " 知识点说明");
        return concept;
    }

    private SysUserPO user(Long id, String username) {
        SysUserPO user = new SysUserPO();
        user.setId(id);
        user.setUsername(username);
        user.setDeleted(0);
        return user;
    }

    private LearningCoursePO learningCourse(Long userId, Long courseId) {
        LearningCoursePO course = new LearningCoursePO();
        course.setUserId(userId);
        course.setCourseId(courseId);
        course.setProgressPercent(new BigDecimal("35.00"));
        return course;
    }
}
