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
import com.elysia.mooc.exam.domain.dto.CreateQuestionOptionRequest;
import com.elysia.mooc.exam.domain.dto.CreateQuestionRequest;
import com.elysia.mooc.exam.domain.dto.ExamQuestionQuery;
import com.elysia.mooc.exam.domain.po.ExamQuestionOptionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.domain.vo.QuestionOptionVO;
import com.elysia.mooc.exam.domain.vo.QuestionVO;
import com.elysia.mooc.exam.mapper.ExamQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamQuestionOptionMapper;
import com.elysia.mooc.exam.service.QuestionService;
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

/** 题目服务实现。 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final ExamQuestionMapper questionMapper;
    private final ExamQuestionOptionMapper optionMapper;

    /**
     * 分页查询题目。
     *
     * @param query 查询条件
     * @return 题目分页
     */
    @Override
    public PageResult<QuestionVO> listQuestions(ExamQuestionQuery query) {
        ExamQuestionQuery safeQuery = query == null ? new ExamQuestionQuery() : query;
        LoginUser loginUser = userContextService.currentLoginUser();
        LambdaQueryWrapper<ExamQuestionPO> wrapper = Wrappers.<ExamQuestionPO>lambdaQuery();
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(ExamQuestionPO::getStem, safeQuery.getKeyword().trim());
        }
        if (safeQuery.getCourseId() != null) {
            wrapper.eq(ExamQuestionPO::getCourseId, safeQuery.getCourseId());
        }
        if (safeQuery.getQuestionType() != null) {
            wrapper.eq(ExamQuestionPO::getQuestionType, safeQuery.getQuestionType());
        }
        if (safeQuery.getDifficulty() != null) {
            wrapper.eq(ExamQuestionPO::getDifficulty, safeQuery.getDifficulty());
        }
        if (safeQuery.getStatus() != null) {
            wrapper.eq(ExamQuestionPO::getStatus, safeQuery.getStatus());
        } else if (!isTeacherOrAdmin(loginUser)) {
            wrapper.eq(ExamQuestionPO::getStatus, EnableStatus.ENABLED);
        }
        applySort(wrapper, safeQuery.getSortBy(), safeQuery.getIsAsc());

        Page<ExamQuestionPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<ExamQuestionPO> result = questionMapper.selectPage(page, wrapper);
        return PageResult.of(result, toQuestionVOList(result.getRecords()));
    }

    /**
     * 创建题目。
     *
     * @param request 创建题目请求
     * @return 创建后的题目
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO createQuestion(CreateQuestionRequest request) {
        LoginUser loginUser = userContextService.currentLoginUser();
        CoursePO course = requireCourse(request.getCourseId());
        assertMaintainPermission(loginUser, course);

        ExamQuestionPO question = BeanCopyUtils.copyBean(request, ExamQuestionPO.class, (source, target) -> {
            target.setCreatorId(loginUser.getUserId());
            target.setStatus(source.getStatus() == null ? EnableStatus.ENABLED : source.getStatus());
            target.setDeleted(0);
        });
        questionMapper.insert(question);
        saveOptions(question.getId(), request.getOptions());
        return toQuestionVO(question, listOptions(question.getId()));
    }

    private void saveOptions(Long questionId, List<CreateQuestionOptionRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }
        int index = 1;
        for (CreateQuestionOptionRequest request : requests) {
            ExamQuestionOptionPO option = new ExamQuestionOptionPO();
            option.setQuestionId(questionId);
            option.setOptionKey(request.getOptionKey().trim());
            option.setOptionText(request.getOptionText().trim());
            option.setCorrect(Boolean.TRUE.equals(request.getCorrect()) ? 1 : 0);
            option.setSort(request.getSort() == null ? index : request.getSort());
            optionMapper.insert(option);
            index++;
        }
    }

    private List<QuestionVO> toQuestionVOList(List<ExamQuestionPO> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptyList();
        }
        Set<Long> questionIds = questions.stream()
                .map(ExamQuestionPO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, List<ExamQuestionOptionPO>> optionMap = listOptions(questionIds).stream()
                .collect(Collectors.groupingBy(ExamQuestionOptionPO::getQuestionId));
        return questions.stream()
                .map(question -> toQuestionVO(question, optionMap.getOrDefault(question.getId(), Collections.emptyList())))
                .toList();
    }

    private QuestionVO toQuestionVO(ExamQuestionPO question, List<ExamQuestionOptionPO> options) {
        return BeanCopyUtils.copyBean(question, QuestionVO.class, (source, target) ->
                target.setOptions(toOptionVOList(options)));
    }

    private List<QuestionOptionVO> toOptionVOList(List<ExamQuestionOptionPO> options) {
        if (CollectionUtils.isEmpty(options)) {
            return Collections.emptyList();
        }
        return options.stream()
                .sorted((left, right) -> Integer.compare(safeInt(left.getSort()), safeInt(right.getSort())))
                .map(option -> QuestionOptionVO.builder()
                        .id(option.getId())
                        .optionKey(option.getOptionKey())
                        .optionText(option.getOptionText())
                        .correct(Objects.equals(option.getCorrect(), 1))
                        .sort(option.getSort())
                        .build())
                .toList();
    }

    private List<ExamQuestionOptionPO> listOptions(Long questionId) {
        if (questionId == null) {
            return Collections.emptyList();
        }
        return optionMapper.selectList(Wrappers.<ExamQuestionOptionPO>lambdaQuery()
                .eq(ExamQuestionOptionPO::getQuestionId, questionId)
                .orderByAsc(ExamQuestionOptionPO::getSort)
                .orderByAsc(ExamQuestionOptionPO::getId));
    }

    private List<ExamQuestionOptionPO> listOptions(Set<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyList();
        }
        return optionMapper.selectList(Wrappers.<ExamQuestionOptionPO>lambdaQuery()
                .in(ExamQuestionOptionPO::getQuestionId, questionIds)
                .orderByAsc(ExamQuestionOptionPO::getSort)
                .orderByAsc(ExamQuestionOptionPO::getId));
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

    private void applySort(LambdaQueryWrapper<ExamQuestionPO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("score".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, ExamQuestionPO::getScore);
        } else {
            wrapper.orderBy(true, asc, ExamQuestionPO::getCreateTime);
        }
        wrapper.orderByDesc(ExamQuestionPO::getId);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
