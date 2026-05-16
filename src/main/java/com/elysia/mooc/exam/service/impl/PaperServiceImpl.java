package com.elysia.mooc.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.exam.constants.ExamConstants;
import com.elysia.mooc.exam.constants.ExamErrorCode;
import com.elysia.mooc.exam.domain.dto.CreatePaperRequest;
import com.elysia.mooc.exam.domain.dto.ExamPaperQuery;
import com.elysia.mooc.exam.domain.enums.ExamPaperStatus;
import com.elysia.mooc.exam.domain.po.ExamPaperPO;
import com.elysia.mooc.exam.domain.po.ExamPaperQuestionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.domain.vo.PaperQuestionVO;
import com.elysia.mooc.exam.domain.vo.PaperVO;
import com.elysia.mooc.exam.mapper.ExamPaperMapper;
import com.elysia.mooc.exam.mapper.ExamPaperQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamQuestionMapper;
import com.elysia.mooc.exam.service.PaperService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 试卷服务实现。 */
@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final ExamQuestionMapper questionMapper;
    private final ExamPaperMapper paperMapper;
    private final ExamPaperQuestionMapper paperQuestionMapper;

    /**
     * 分页查询试卷。
     *
     * @param query 查询条件
     * @return 试卷分页
     */
    @Override
    public PageResult<PaperVO> listPapers(ExamPaperQuery query) {
        ExamPaperQuery safeQuery = query == null ? new ExamPaperQuery() : query;
        LoginUser loginUser = userContextService.currentLoginUser();
        LambdaQueryWrapper<ExamPaperPO> wrapper = Wrappers.<ExamPaperPO>lambdaQuery();
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(ExamPaperPO::getTitle, safeQuery.getKeyword().trim());
        }
        if (safeQuery.getCourseId() != null) {
            wrapper.eq(ExamPaperPO::getCourseId, safeQuery.getCourseId());
        }
        if (safeQuery.getStatus() != null) {
            wrapper.eq(ExamPaperPO::getStatus, safeQuery.getStatus());
        } else if (!isTeacherOrAdmin(loginUser)) {
            wrapper.eq(ExamPaperPO::getStatus, ExamPaperStatus.PUBLISHED);
        }
        applySort(wrapper, safeQuery.getSortBy(), safeQuery.getIsAsc());

        Page<ExamPaperPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<ExamPaperPO> result = paperMapper.selectPage(page, wrapper);
        return PageResult.of(result, toPaperVOList(result.getRecords()));
    }

    /**
     * 创建试卷。
     *
     * @param request 创建试卷请求
     * @return 创建后的试卷
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaperVO createPaper(CreatePaperRequest request) {
        LoginUser loginUser = userContextService.currentLoginUser();
        CoursePO course = requireCourse(request.getCourseId());
        assertMaintainPermission(loginUser, course);
        List<ExamQuestionPO> questions = requireQuestions(request);
        BigDecimal totalScore = questions.stream()
                .map(question -> safeScore(question.getScore()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ExamPaperPO paper = BeanCopyUtils.copyBean(request, ExamPaperPO.class, (source, target) -> {
            target.setTotalScore(totalScore);
            target.setPassScore(resolvePassScore(source.getPassScore(), totalScore));
            target.setStatus(source.getStatus() == null ? ExamPaperStatus.PUBLISHED : source.getStatus());
            target.setDeleted(0);
        });
        paperMapper.insert(paper);
        savePaperQuestions(paper.getId(), questions);
        return toPaperVO(paper, listPaperQuestions(paper.getId()), questions);
    }

    private List<ExamQuestionPO> requireQuestions(CreatePaperRequest request) {
        List<ExamQuestionPO> questions = questionMapper.selectBatchIds(request.getQuestionIds());
        if (questions.size() != request.getQuestionIds().size()) {
            throw new BizException(ExamErrorCode.PAPER_QUESTION_INVALID, "试卷包含不存在的题目");
        }
        Map<Long, ExamQuestionPO> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestionPO::getId, Function.identity()));
        List<ExamQuestionPO> ordered = request.getQuestionIds().stream().map(questionMap::get).toList();
        if (ordered.stream().anyMatch(question -> !Objects.equals(question.getCourseId(), request.getCourseId()))) {
            throw new BizException(ExamErrorCode.PAPER_QUESTION_INVALID, "试卷题目必须属于同一课程");
        }
        if (ordered.stream().anyMatch(question -> question.getStatus() != EnableStatus.ENABLED)) {
            throw new BizException(ExamErrorCode.PAPER_QUESTION_INVALID, "试卷不能绑定禁用题目");
        }
        return ordered;
    }

    private void savePaperQuestions(Long paperId, List<ExamQuestionPO> questions) {
        int index = 1;
        for (ExamQuestionPO question : questions) {
            ExamPaperQuestionPO relation = new ExamPaperQuestionPO();
            relation.setPaperId(paperId);
            relation.setQuestionId(question.getId());
            relation.setScore(safeScore(question.getScore()));
            relation.setSort(index);
            paperQuestionMapper.insert(relation);
            index++;
        }
    }

    private List<PaperVO> toPaperVOList(List<ExamPaperPO> papers) {
        if (CollectionUtils.isEmpty(papers)) {
            return Collections.emptyList();
        }
        Set<Long> paperIds = papers.stream()
                .map(ExamPaperPO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<ExamPaperQuestionPO> relations = listPaperQuestions(paperIds);
        Set<Long> questionIds = relations.stream()
                .map(ExamPaperQuestionPO::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, ExamQuestionPO> questionMap = mapQuestions(questionIds);
        Map<Long, List<ExamPaperQuestionPO>> relationMap = relations.stream()
                .collect(Collectors.groupingBy(ExamPaperQuestionPO::getPaperId));
        return papers.stream()
                .map(paper -> toPaperVO(paper, relationMap.getOrDefault(paper.getId(), Collections.emptyList()), questionMap))
                .toList();
    }

    private PaperVO toPaperVO(
            ExamPaperPO paper,
            List<ExamPaperQuestionPO> relations,
            List<ExamQuestionPO> questions) {
        Map<Long, ExamQuestionPO> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestionPO::getId, Function.identity(), (left, right) -> left));
        return toPaperVO(paper, relations, questionMap);
    }

    private PaperVO toPaperVO(
            ExamPaperPO paper,
            List<ExamPaperQuestionPO> relations,
            Map<Long, ExamQuestionPO> questionMap) {
        return BeanCopyUtils.copyBean(paper, PaperVO.class, (source, target) ->
                target.setQuestions(toPaperQuestionVOList(relations, questionMap)));
    }

    private List<PaperQuestionVO> toPaperQuestionVOList(
            List<ExamPaperQuestionPO> relations,
            Map<Long, ExamQuestionPO> questionMap) {
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }
        return relations.stream()
                .sorted((left, right) -> Integer.compare(safeInt(left.getSort()), safeInt(right.getSort())))
                .map(relation -> {
                    ExamQuestionPO question = questionMap.get(relation.getQuestionId());
                    return PaperQuestionVO.builder()
                            .questionId(relation.getQuestionId())
                            .questionType(question == null ? null : question.getQuestionType())
                            .stem(question == null ? null : question.getStem())
                            .score(relation.getScore())
                            .sort(relation.getSort())
                            .build();
                })
                .toList();
    }

    private List<ExamPaperQuestionPO> listPaperQuestions(Long paperId) {
        if (paperId == null) {
            return Collections.emptyList();
        }
        return paperQuestionMapper.selectList(Wrappers.<ExamPaperQuestionPO>lambdaQuery()
                .eq(ExamPaperQuestionPO::getPaperId, paperId)
                .orderByAsc(ExamPaperQuestionPO::getSort)
                .orderByAsc(ExamPaperQuestionPO::getId));
    }

    private List<ExamPaperQuestionPO> listPaperQuestions(Set<Long> paperIds) {
        if (CollectionUtils.isEmpty(paperIds)) {
            return Collections.emptyList();
        }
        return paperQuestionMapper.selectList(Wrappers.<ExamPaperQuestionPO>lambdaQuery()
                .in(ExamPaperQuestionPO::getPaperId, paperIds)
                .orderByAsc(ExamPaperQuestionPO::getSort)
                .orderByAsc(ExamPaperQuestionPO::getId));
    }

    private Map<Long, ExamQuestionPO> mapQuestions(Set<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyMap();
        }
        return questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(ExamQuestionPO::getId, Function.identity(), (left, right) -> left));
    }

    private CoursePO requireCourse(Long courseId) {
        CoursePO course = courseId == null ? null : courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(ExamErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }

    private void assertMaintainPermission(LoginUser loginUser, CoursePO course) {
        if (isAdmin(loginUser)) {
            return;
        }
        if (hasRole(loginUser, ExamConstants.ROLE_TEACHER)
                && Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            return;
        }
        throw new BizException(ExamErrorCode.EXAM_FORBIDDEN);
    }

    private boolean isTeacherOrAdmin(LoginUser loginUser) {
        return isAdmin(loginUser) || hasRole(loginUser, ExamConstants.ROLE_TEACHER);
    }

    private boolean isAdmin(LoginUser loginUser) {
        return hasRole(loginUser, ExamConstants.ROLE_ADMIN);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        return loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(roleCode::equalsIgnoreCase);
    }

    private BigDecimal resolvePassScore(BigDecimal requestPassScore, BigDecimal totalScore) {
        if (requestPassScore != null) {
            return requestPassScore;
        }
        return safeScore(totalScore).multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeScore(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void applySort(LambdaQueryWrapper<ExamPaperPO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("totalScore".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, ExamPaperPO::getTotalScore);
        } else {
            wrapper.orderBy(true, asc, ExamPaperPO::getCreateTime);
        }
        wrapper.orderByDesc(ExamPaperPO::getId);
    }
}
