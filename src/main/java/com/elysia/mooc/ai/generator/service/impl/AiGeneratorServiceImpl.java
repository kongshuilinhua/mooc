package com.elysia.mooc.ai.generator.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.ai.generator.constants.AiGeneratorConstants;
import com.elysia.mooc.ai.generator.constants.AiGeneratorErrorCode;
import com.elysia.mooc.ai.generator.domain.dto.GenerateChapterSummaryRequest;
import com.elysia.mooc.ai.generator.domain.dto.GenerateLearningPathRequest;
import com.elysia.mooc.ai.generator.domain.dto.GenerateQuestionsRequest;
import com.elysia.mooc.ai.generator.domain.enums.AiGenerationBizType;
import com.elysia.mooc.ai.generator.domain.enums.AiGenerationStatus;
import com.elysia.mooc.ai.generator.domain.enums.AiLearningPathStatus;
import com.elysia.mooc.ai.generator.domain.enums.AiQuestionReviewStatus;
import com.elysia.mooc.ai.generator.domain.po.AiGenerationTaskPO;
import com.elysia.mooc.ai.generator.domain.po.AiLearningPathPO;
import com.elysia.mooc.ai.generator.domain.po.AiQuestionDraftPO;
import com.elysia.mooc.ai.generator.domain.vo.ChapterSummaryResultVO;
import com.elysia.mooc.ai.generator.domain.vo.GeneratedQuestionDraftVO;
import com.elysia.mooc.ai.generator.domain.vo.GeneratedQuestionsResultVO;
import com.elysia.mooc.ai.generator.domain.vo.GenerationSourceVO;
import com.elysia.mooc.ai.generator.domain.vo.LearningPathResultVO;
import com.elysia.mooc.ai.generator.domain.vo.LearningPathStageVO;
import com.elysia.mooc.ai.generator.mapper.AiGenerationTaskMapper;
import com.elysia.mooc.ai.generator.mapper.AiLearningPathMapper;
import com.elysia.mooc.ai.generator.mapper.AiQuestionDraftMapper;
import com.elysia.mooc.ai.generator.service.AiGeneratorService;
import com.elysia.mooc.ai.model.AiChatClient;
import com.elysia.mooc.ai.model.AiChatProperties;
import com.elysia.mooc.ai.model.ChatCompletionMessage;
import com.elysia.mooc.ai.model.ChatCompletionRequest;
import com.elysia.mooc.ai.model.ChatCompletionResult;
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
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.domain.po.LearningRecordPO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.learning.mapper.LearningRecordMapper;
import com.elysia.mooc.studyarchive.domain.po.LearningWrongBookPO;
import com.elysia.mooc.studyarchive.mapper.LearningWrongBookMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** AI 出题、章节总结和学习路径服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGeneratorServiceImpl implements AiGeneratorService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseSectionMapper courseSectionMapper;
    private final CourseConceptMapper courseConceptMapper;
    private final SysUserMapper sysUserMapper;
    private final LearningCourseMapper learningCourseMapper;
    private final LearningRecordMapper learningRecordMapper;
    private final LearningWrongBookMapper learningWrongBookMapper;
    private final AiGenerationTaskMapper generationTaskMapper;
    private final AiQuestionDraftMapper questionDraftMapper;
    private final AiLearningPathMapper learningPathMapper;
    private final AiChatClient aiChatClient;
    private final AiChatProperties aiChatProperties;
    private final ObjectMapper objectMapper;

    /**
     * 生成章节总结。
     * @param chapterId 章节 ID
     * @param request 总结生成参数
     * @return 章节总结结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BizException.class)
    public ChapterSummaryResultVO generateChapterSummary(Long chapterId, GenerateChapterSummaryRequest request) {
        LoginUser loginUser = userContextService.currentLoginUser();
        GenerateChapterSummaryRequest safeRequest = request == null ? new GenerateChapterSummaryRequest() : request;
        CourseChapterPO chapter = requireChapter(chapterId);
        CoursePO course = requireCourse(chapter.getCourseId());
        requireTeacherOwnsCourseOrAdmin(loginUser, course);

        List<CourseSectionPO> sections = listSections(chapter.getId());
        List<CourseConceptPO> concepts = listConcepts(course.getId(), sections);
        List<GenerationSourceVO> sources = buildSummarySources(chapter, sections, concepts);
        String prompt = buildSummaryPrompt(course, chapter, sections, concepts, safeRequest);
        AiGenerationTaskPO task = saveGenerationTask(
                AiGenerationBizType.CHAPTER_SUMMARY,
                chapter.getId(),
                loginUser.getUserId(),
                prompt);

        ModelText modelText = generateWithModel(prompt);
        ChapterSummaryResultVO result = buildSummaryResult(chapter, sources, concepts, modelText);
        result.setTaskId(task.getId());
        task.setStatus(AiGenerationStatus.SUCCESS);
        task.setResultSnapshot(toJson(result));
        generationTaskMapper.updateById(task);
        return result;
    }

    /**
     * 生成课程练习题草稿。
     * @param courseId 课程 ID
     * @param request 出题参数
     * @return 题目草稿生成结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BizException.class)
    public GeneratedQuestionsResultVO generateQuestionDrafts(Long courseId, GenerateQuestionsRequest request) {
        LoginUser teacher = requireTeacher();
        GenerateQuestionsRequest safeRequest = request == null ? new GenerateQuestionsRequest() : request;
        safeRequest.check();
        CoursePO course = requireCourse(courseId);
        requireTeacherOwnsCourse(teacher, course);
        CourseChapterPO chapter = null;
        if (safeRequest.getChapterId() != null) {
            chapter = requireChapter(safeRequest.getChapterId());
            if (!Objects.equals(chapter.getCourseId(), course.getId())) {
                throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_PARAM_INVALID, "章节不属于当前课程");
            }
        }

        List<CourseConceptPO> concepts = listConcepts(course.getId(), chapter == null ? Collections.emptyList() : listSections(chapter.getId()));
        String prompt = buildQuestionPrompt(course, chapter, concepts, safeRequest);
        AiGenerationTaskPO task = saveGenerationTask(
                AiGenerationBizType.QUESTION_DRAFT,
                course.getId(),
                teacher.getUserId(),
                prompt);

        ModelText modelText = generateWithModel(prompt);
        List<GeneratedQuestionDraftVO> questions = buildQuestionDrafts(course, chapter, concepts, safeRequest, modelText);
        for (GeneratedQuestionDraftVO question : questions) {
            AiQuestionDraftPO draft = saveQuestionDraft(task.getId(), course.getId(), chapter, question);
            question.setDraftId(draft.getId());
        }

        GeneratedQuestionsResultVO result = new GeneratedQuestionsResultVO();
        result.setTaskId(task.getId());
        result.setCourseId(course.getId());
        result.setQuestionCount(questions.size());
        result.setReviewStatus(AiQuestionReviewStatus.PENDING);
        result.setQuestions(questions);
        result.setStatus(AiGenerationStatus.SUCCESS);
        result.setGenerationSource(modelText.source());

        task.setStatus(AiGenerationStatus.SUCCESS);
        task.setResultSnapshot(toJson(result));
        generationTaskMapper.updateById(task);
        return result;
    }

    /**
     * 生成学生学习路径。
     * @param studentId 学生 ID
     * @param request 学习路径参数
     * @return 学习路径生成结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BizException.class)
    public LearningPathResultVO generateLearningPath(Long studentId, GenerateLearningPathRequest request) {
        LoginUser loginUser = userContextService.currentLoginUser();
        GenerateLearningPathRequest safeRequest = request == null ? new GenerateLearningPathRequest() : request;
        safeRequest.check();
        requireStudentExists(studentId);
        requireSelfStudentOrAdmin(loginUser, studentId);
        CoursePO targetCourse = safeRequest.getTargetCourseId() == null ? null : requireCourse(safeRequest.getTargetCourseId());

        LearningContext context = loadLearningContext(studentId, safeRequest.getTargetCourseId());
        String prompt = buildLearningPathPrompt(studentId, safeRequest, targetCourse, context);
        AiGenerationTaskPO task = saveGenerationTask(
                AiGenerationBizType.LEARNING_PATH,
                safeRequest.getTargetCourseId() == null ? studentId : safeRequest.getTargetCourseId(),
                loginUser.getUserId(),
                prompt);

        if (context.isEmpty() && targetCourse == null) {
            String message = "学习记录和目标课程都为空，暂不能生成有效学习路径";
            markTaskFailed(task, message);
            throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_LEARNING_DATA_NOT_ENOUGH, message);
        }

        ModelText modelText = generateWithModel(prompt);
        LearningPathResultVO result = buildLearningPathResult(studentId, safeRequest, targetCourse, context, modelText);
        AiLearningPathPO path = saveLearningPath(studentId, safeRequest, result);
        result.setPathId(path.getId());

        task.setStatus(AiGenerationStatus.SUCCESS);
        task.setResultSnapshot(toJson(result));
        generationTaskMapper.updateById(task);
        return result;
    }

    private LoginUser requireTeacher() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (hasRole(loginUser, "TEACHER")) {
            return loginUser;
        }
        throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_FORBIDDEN);
    }

    private void requireTeacherOwnsCourseOrAdmin(LoginUser loginUser, CoursePO course) {
        if (hasRole(loginUser, "ADMIN")) {
            return;
        }
        requireTeacherOwnsCourse(loginUser, course);
    }

    private void requireTeacherOwnsCourse(LoginUser loginUser, CoursePO course) {
        if (!hasRole(loginUser, "TEACHER")) {
            throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_FORBIDDEN);
        }
        if (!Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_COURSE_FORBIDDEN);
        }
    }

    private void requireSelfStudentOrAdmin(LoginUser loginUser, Long studentId) {
        if (hasRole(loginUser, "ADMIN")) {
            return;
        }
        if (hasRole(loginUser, "STUDENT") && Objects.equals(loginUser.getUserId(), studentId)) {
            return;
        }
        throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_FORBIDDEN);
    }

    private boolean hasRole(LoginUser loginUser, String role) {
        return loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(item -> role.equalsIgnoreCase(item));
    }

    private CoursePO requireCourse(Long courseId) {
        CoursePO course = courseId == null ? null : courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_COURSE_NOT_FOUND);
        }
        return course;
    }

    private CourseChapterPO requireChapter(Long chapterId) {
        CourseChapterPO chapter = chapterId == null ? null : courseChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_CHAPTER_NOT_FOUND);
        }
        return chapter;
    }

    private void requireStudentExists(Long studentId) {
        SysUserPO user = studentId == null ? null : sysUserMapper.selectById(studentId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_STUDENT_NOT_FOUND);
        }
    }

    private List<CourseSectionPO> listSections(Long chapterId) {
        return safeList(courseSectionMapper.selectList(Wrappers.<CourseSectionPO>lambdaQuery()
                .eq(CourseSectionPO::getChapterId, chapterId)
                .orderByAsc(CourseSectionPO::getSort)
                .orderByAsc(CourseSectionPO::getId)));
    }

    private List<CourseConceptPO> listConcepts(Long courseId, List<CourseSectionPO> sections) {
        List<Long> sectionIds = sections.stream()
                .map(CourseSectionPO::getId)
                .filter(Objects::nonNull)
                .toList();
        var wrapper = Wrappers.<CourseConceptPO>lambdaQuery()
                .eq(CourseConceptPO::getCourseId, courseId)
                .orderByAsc(CourseConceptPO::getSort)
                .orderByAsc(CourseConceptPO::getId);
        if (!sectionIds.isEmpty()) {
            wrapper.and(item -> item.isNull(CourseConceptPO::getSectionId).or().in(CourseConceptPO::getSectionId, sectionIds));
        }
        return safeList(courseConceptMapper.selectList(wrapper));
    }

    private LearningContext loadLearningContext(Long studentId, Long targetCourseId) {
        var learningCourseWrapper = Wrappers.<LearningCoursePO>lambdaQuery()
                .eq(LearningCoursePO::getUserId, studentId)
                .orderByDesc(LearningCoursePO::getLastLearnTime)
                .orderByDesc(LearningCoursePO::getId);
        if (targetCourseId != null) {
            learningCourseWrapper.eq(LearningCoursePO::getCourseId, targetCourseId);
        }
        List<LearningCoursePO> courses = safeList(learningCourseMapper.selectList(learningCourseWrapper));

        var recordWrapper = Wrappers.<LearningRecordPO>lambdaQuery()
                .eq(LearningRecordPO::getUserId, studentId)
                .orderByDesc(LearningRecordPO::getLastHeartbeatTime)
                .orderByDesc(LearningRecordPO::getId);
        if (targetCourseId != null) {
            recordWrapper.eq(LearningRecordPO::getCourseId, targetCourseId);
        }
        List<LearningRecordPO> records = safeList(learningRecordMapper.selectList(recordWrapper));

        List<LearningWrongBookPO> wrongBooks = safeList(learningWrongBookMapper.selectList(Wrappers.<LearningWrongBookPO>lambdaQuery()
                .eq(LearningWrongBookPO::getStudentId, studentId)
                .orderByDesc(LearningWrongBookPO::getLastWrongTime)
                .orderByDesc(LearningWrongBookPO::getId)));
        return new LearningContext(courses, records, wrongBooks);
    }

    private String buildSummaryPrompt(
            CoursePO course,
            CourseChapterPO chapter,
            List<CourseSectionPO> sections,
            List<CourseConceptPO> concepts,
            GenerateChapterSummaryRequest request) {
        Map<String, Object> prompt = new LinkedHashMap<>();
        prompt.put("任务", "请为 MOOC 教师生成可用于课程详情和学习页展示的章节总结");
        prompt.put("课程", course.getTitle());
        prompt.put("章节", chapter.getTitle());
        prompt.put("章节简介", chapter.getSummary());
        prompt.put("风格", StringUtils.hasText(request.getStyle()) ? request.getStyle() : AiGeneratorConstants.DEFAULT_SUMMARY_STYLE);
        prompt.put("长度", StringUtils.hasText(request.getLength()) ? request.getLength() : AiGeneratorConstants.DEFAULT_SUMMARY_LENGTH);
        prompt.put("小节", sections.stream().map(CourseSectionPO::getTitle).toList());
        prompt.put("知识点", concepts.stream().map(CourseConceptPO::getTitle).toList());
        prompt.put("输出要求", "返回中文总结、3到5个关键点和可追溯来源");
        return toJson(prompt);
    }

    private String buildQuestionPrompt(
            CoursePO course,
            CourseChapterPO chapter,
            List<CourseConceptPO> concepts,
            GenerateQuestionsRequest request) {
        Map<String, Object> prompt = new LinkedHashMap<>();
        prompt.put("任务", "请为教师生成可人工审核的练习题草稿，不能直接发布到正式题库");
        prompt.put("课程", course.getTitle());
        prompt.put("章节", chapter == null ? null : chapter.getTitle());
        prompt.put("题型", request.getQuestionType());
        prompt.put("难度", request.getDifficulty());
        prompt.put("数量", request.getQuestionCount());
        prompt.put("知识点", concepts.stream().map(CourseConceptPO::getTitle).toList());
        prompt.put("输出要求", "每题包含题干、选项、答案和解析，审核状态固定为待审核");
        return toJson(prompt);
    }

    private String buildLearningPathPrompt(
            Long studentId,
            GenerateLearningPathRequest request,
            CoursePO targetCourse,
            LearningContext context) {
        Map<String, Object> prompt = new LinkedHashMap<>();
        prompt.put("任务", "请基于学生学习记录生成分阶段学习路径，不要输出空泛模板");
        prompt.put("学生ID", studentId);
        prompt.put("目标", request.getTarget());
        prompt.put("目标类型", request.getGoalType());
        prompt.put("目标课程", targetCourse == null ? null : targetCourse.getTitle());
        prompt.put("周期天数", request.getHorizonDays());
        prompt.put("每日分钟数", request.getDailyMinutes());
        prompt.put("已学课程数", context.courses().size());
        prompt.put("学习记录数", context.records().size());
        prompt.put("错题数", context.wrongBooks().size());
        prompt.put("输出要求", "返回主题、阶段、阶段任务、每日学习时长和失效时间");
        return toJson(prompt);
    }

    private AiGenerationTaskPO saveGenerationTask(
            AiGenerationBizType bizType,
            Long bizId,
            Long triggerUserId,
            String promptSnapshot) {
        AiGenerationTaskPO task = new AiGenerationTaskPO();
        task.setBizType(bizType);
        task.setBizId(bizId);
        task.setTriggerUserId(triggerUserId);
        task.setStatus(AiGenerationStatus.PROCESSING);
        task.setPromptSnapshot(promptSnapshot);
        generationTaskMapper.insert(task);
        return task;
    }

    private void markTaskFailed(AiGenerationTaskPO task, String errorMessage) {
        task.setStatus(AiGenerationStatus.FAILED);
        task.setResultSnapshot(toJson(Map.of("errorMessage", errorMessage)));
        generationTaskMapper.updateById(task);
    }

    private ModelText generateWithModel(String prompt) {
        try {
            ChatCompletionResult result = aiChatClient.complete(new ChatCompletionRequest(
                    aiChatProperties.getModel(),
                    List.of(
                            new ChatCompletionMessage("system", "你是 MOOC 平台的教学内容生成助手，必须使用中文回答。"),
                            new ChatCompletionMessage("user", prompt))));
            if (result != null && StringUtils.hasText(result.content())) {
                return new ModelText(result.content().trim(), "MODEL:" + result.model());
            }
        } catch (RuntimeException ex) {
            log.info("AI 生成模型不可用，使用规则兜底结果。原因：{}", ex.getMessage());
        }
        return new ModelText(null, AiGeneratorConstants.FALLBACK_SOURCE);
    }

    private ChapterSummaryResultVO buildSummaryResult(
            CourseChapterPO chapter,
            List<GenerationSourceVO> sources,
            List<CourseConceptPO> concepts,
            ModelText modelText) {
        List<String> keyPoints = concepts.stream()
                .map(CourseConceptPO::getTitle)
                .filter(StringUtils::hasText)
                .limit(5)
                .toList();
        if (keyPoints.isEmpty()) {
            keyPoints = List.of(chapter.getTitle(), "学习目标", "章节练习");
        }
        String summary = StringUtils.hasText(modelText.content())
                ? modelText.content()
                : "本章围绕“" + chapter.getTitle() + "”展开，建议先理解章节目标，再结合小节内容和知识点完成练习巩固。";

        ChapterSummaryResultVO result = new ChapterSummaryResultVO();
        result.setChapterId(chapter.getId());
        result.setSummary(summary);
        result.setKeyPoints(keyPoints);
        result.setSources(sources);
        result.setStatus(AiGenerationStatus.SUCCESS);
        result.setGenerationSource(modelText.source());
        return result;
    }

    private List<GenerationSourceVO> buildSummarySources(
            CourseChapterPO chapter,
            List<CourseSectionPO> sections,
            List<CourseConceptPO> concepts) {
        List<GenerationSourceVO> sources = new ArrayList<>();
        sources.add(GenerationSourceVO.builder()
                .title(chapter.getTitle())
                .sourceType(AiGeneratorConstants.SOURCE_TYPE_CHAPTER)
                .sourceId(chapter.getId())
                .similarity(new BigDecimal("1.00"))
                .snippet(chapter.getSummary())
                .build());
        sections.stream().limit(3).forEach(section -> sources.add(GenerationSourceVO.builder()
                .title(section.getTitle())
                .sourceType(AiGeneratorConstants.SOURCE_TYPE_SECTION)
                .sourceId(section.getId())
                .similarity(new BigDecimal("0.90"))
                .snippet("小节时长：" + nullToZero(section.getDurationSeconds()) + "秒")
                .build()));
        concepts.stream().limit(3).forEach(concept -> sources.add(GenerationSourceVO.builder()
                .title(concept.getTitle())
                .sourceType(AiGeneratorConstants.SOURCE_TYPE_CONCEPT)
                .sourceId(concept.getId())
                .similarity(new BigDecimal("0.80"))
                .snippet(concept.getContent())
                .build()));
        return sources;
    }

    private List<GeneratedQuestionDraftVO> buildQuestionDrafts(
            CoursePO course,
            CourseChapterPO chapter,
            List<CourseConceptPO> concepts,
            GenerateQuestionsRequest request,
            ModelText modelText) {
        List<String> conceptTitles = concepts.stream()
                .map(CourseConceptPO::getTitle)
                .filter(StringUtils::hasText)
                .toList();
        List<GeneratedQuestionDraftVO> questions = new ArrayList<>();
        for (int index = 0; index < request.getQuestionCount(); index++) {
            String concept = conceptTitles.isEmpty()
                    ? course.getTitle()
                    : conceptTitles.get(index % conceptTitles.size());
            GeneratedQuestionDraftVO question = new GeneratedQuestionDraftVO();
            question.setType(request.getQuestionType());
            question.setStem(buildQuestionStem(course, chapter, concept, request, index));
            question.setOptions(buildOptions(request.getQuestionType(), concept));
            question.setAnswer(buildAnswer(request.getQuestionType()));
            question.setAnalysis(buildAnalysis(concept, modelText));
            question.setDifficulty(request.getDifficulty());
            question.setReviewStatus(AiQuestionReviewStatus.PENDING);
            questions.add(question);
        }
        return questions;
    }

    private String buildQuestionStem(
            CoursePO course,
            CourseChapterPO chapter,
            String concept,
            GenerateQuestionsRequest request,
            int index) {
        String scope = chapter == null ? course.getTitle() : chapter.getTitle();
        return switch (request.getQuestionType()) {
            case JUDGE -> "判断题：" + concept + " 是理解《" + scope + "》的关键知识点。";
            case SHORT -> "请结合《" + scope + "》说明“" + concept + "”的核心作用。";
            case MULTI -> "关于《" + scope + "》中的“" + concept + "”，下列说法正确的是哪些？";
            case SINGLE -> "关于《" + scope + "》中的“" + concept + "”，下列哪项最准确？";
        };
    }

    private List<String> buildOptions(ExamQuestionType questionType, String concept) {
        return switch (questionType) {
            case SHORT -> Collections.emptyList();
            case JUDGE -> List.of("正确", "错误");
            case MULTI -> List.of(
                    "理解 " + concept + " 的定义",
                    "掌握 " + concept + " 的应用场景",
                    "忽略课程上下文直接记忆答案",
                    "结合练习验证 " + concept);
            case SINGLE -> List.of(
                    "围绕 " + concept + " 理解概念和应用",
                    "只记住术语名称",
                    "跳过小节直接做题",
                    "只关注页面展示效果");
        };
    }

    private String buildAnswer(ExamQuestionType questionType) {
        return switch (questionType) {
            case SHORT -> "围绕定义、应用场景和练习验证三个方面回答。";
            case JUDGE -> "正确";
            case MULTI -> "A,B,D";
            case SINGLE -> "A";
        };
    }

    private String buildAnalysis(String concept, ModelText modelText) {
        if (StringUtils.hasText(modelText.content())) {
            return "模型辅助生成，教师需重点复核答案和解析。关联知识点：" + concept;
        }
        return "规则兜底生成，教师需结合课程内容复核后再进入正式题库。关联知识点：" + concept;
    }

    private AiQuestionDraftPO saveQuestionDraft(
            Long taskId,
            Long courseId,
            CourseChapterPO chapter,
            GeneratedQuestionDraftVO question) {
        AiQuestionDraftPO draft = new AiQuestionDraftPO();
        draft.setTaskId(taskId);
        draft.setCourseId(courseId);
        draft.setChapterId(chapter == null ? null : chapter.getId());
        draft.setQuestionType(question.getType());
        draft.setQuestionContent(toJson(question));
        draft.setDifficultyLevel(question.getDifficulty());
        draft.setReviewStatus(AiQuestionReviewStatus.PENDING);
        questionDraftMapper.insert(draft);
        return draft;
    }

    private LearningPathResultVO buildLearningPathResult(
            Long studentId,
            GenerateLearningPathRequest request,
            CoursePO targetCourse,
            LearningContext context,
            ModelText modelText) {
        int firstDays = Math.max(3, request.getHorizonDays() / 3);
        int secondDays = Math.max(3, request.getHorizonDays() / 3);
        int thirdDays = Math.max(1, request.getHorizonDays() - firstDays - secondDays);
        String theme = targetCourse == null ? request.getTarget() : targetCourse.getTitle() + " 学习路径";
        List<LearningPathStageVO> stages = List.of(
                LearningPathStageVO.builder()
                        .stageName("阶段一：补齐基础")
                        .goal("梳理目标课程和已有学习记录，确认薄弱点")
                        .durationDays(firstDays)
                        .tasks(List.of("复盘最近学习记录", "整理错题和未掌握知识点", "完成基础章节回看"))
                        .build(),
                LearningPathStageVO.builder()
                        .stageName("阶段二：重点突破")
                        .goal("围绕目标和薄弱知识点进行集中练习")
                        .durationDays(secondDays)
                        .tasks(List.of("每天完成专项练习", "记录疑问并使用课程问答", "复测错题掌握情况"))
                        .build(),
                LearningPathStageVO.builder()
                        .stageName("阶段三：综合巩固")
                        .goal("通过总结和模拟练习验证学习效果")
                        .durationDays(thirdDays)
                        .tasks(List.of("完成阶段总结", "进行综合练习", "根据结果调整下一轮目标"))
                        .build());

        LearningPathResultVO result = new LearningPathResultVO();
        result.setStudentId(studentId);
        result.setTheme(theme);
        result.setStages(stages);
        result.setDailyMinutes(request.getDailyMinutes());
        result.setExpireTime(LocalDateTime.now().plusDays(request.getHorizonDays()));
        result.setStatus(AiLearningPathStatus.ACTIVE);
        result.setGenerationSource(modelText.source());
        return result;
    }

    private AiLearningPathPO saveLearningPath(
            Long studentId,
            GenerateLearningPathRequest request,
            LearningPathResultVO result) {
        AiLearningPathPO path = new AiLearningPathPO();
        path.setStudentId(studentId);
        path.setGoalType(request.getGoalType().trim());
        path.setTargetCourseId(request.getTargetCourseId());
        path.setPathContent(toJson(result));
        path.setStatus(AiLearningPathStatus.ACTIVE);
        path.setExpireTime(result.getExpireTime());
        learningPathMapper.insert(path);
        return path;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BizException(AiGeneratorErrorCode.AI_GENERATOR_PARAM_INVALID, "AI 生成快照序列化失败");
        }
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private record ModelText(String content, String source) {
    }

    private record LearningContext(
            List<LearningCoursePO> courses,
            List<LearningRecordPO> records,
            List<LearningWrongBookPO> wrongBooks) {

        private boolean isEmpty() {
            return CollectionUtils.isEmpty(courses)
                    && CollectionUtils.isEmpty(records)
                    && CollectionUtils.isEmpty(wrongBooks);
        }
    }
}
