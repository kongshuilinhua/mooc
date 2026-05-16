package com.elysia.mooc.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.exam.domain.dto.WrongQuestionQuery;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.domain.po.ExamWrongQuestionPO;
import com.elysia.mooc.exam.domain.vo.WrongQuestionVO;
import com.elysia.mooc.exam.mapper.ExamQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamWrongQuestionMapper;
import com.elysia.mooc.exam.service.WrongQuestionService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 错题本服务实现。 */
@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final UserContextService userContextService;
    private final ExamWrongQuestionMapper wrongQuestionMapper;
    private final ExamQuestionMapper questionMapper;

    /**
     * 分页查询当前用户错题。
     *
     * @param query 查询条件
     * @return 错题分页
     */
    @Override
    public PageResult<WrongQuestionVO> listWrongQuestions(WrongQuestionQuery query) {
        WrongQuestionQuery safeQuery = query == null ? new WrongQuestionQuery() : query;
        Long userId = userContextService.currentUserId();
        LambdaQueryWrapper<ExamWrongQuestionPO> wrapper = Wrappers.<ExamWrongQuestionPO>lambdaQuery()
                .eq(ExamWrongQuestionPO::getUserId, userId);
        if (safeQuery.getResolved() != null) {
            wrapper.eq(ExamWrongQuestionPO::getResolved, Boolean.TRUE.equals(safeQuery.getResolved()) ? 1 : 0);
        } else {
            wrapper.eq(ExamWrongQuestionPO::getResolved, 0);
        }
        if (safeQuery.getCourseId() != null) {
            wrapper.eq(ExamWrongQuestionPO::getCourseId, safeQuery.getCourseId());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            List<Long> questionIds = questionMapper.selectList(Wrappers.<ExamQuestionPO>lambdaQuery()
                            .select(ExamQuestionPO::getId)
                            .like(ExamQuestionPO::getStem, safeQuery.getKeyword().trim()))
                    .stream()
                    .map(ExamQuestionPO::getId)
                    .filter(Objects::nonNull)
                    .toList();
            if (questionIds.isEmpty()) {
                return PageResult.empty(0L, 0);
            }
            wrapper.in(ExamWrongQuestionPO::getQuestionId, questionIds);
        }
        wrapper.orderByDesc(ExamWrongQuestionPO::getLastWrongTime)
                .orderByDesc(ExamWrongQuestionPO::getId);

        Page<ExamWrongQuestionPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<ExamWrongQuestionPO> result = wrongQuestionMapper.selectPage(page, wrapper);
        return PageResult.of(result, toWrongQuestionVOList(result.getRecords()));
    }

    /**
     * 写入或累加错题。
     *
     * @param userId 用户 ID
     * @param questionId 题目 ID
     * @param courseId 课程 ID
     */
    @Override
    public void recordWrongQuestion(Long userId, Long questionId, Long courseId) {
        if (userId == null || questionId == null || courseId == null) {
            return;
        }
        ExamWrongQuestionPO existed = wrongQuestionMapper.selectOne(Wrappers.<ExamWrongQuestionPO>lambdaQuery()
                .eq(ExamWrongQuestionPO::getUserId, userId)
                .eq(ExamWrongQuestionPO::getQuestionId, questionId));
        if (existed != null) {
            existed.setWrongCount(safeInt(existed.getWrongCount()) + 1);
            existed.setLastWrongTime(LocalDateTime.now());
            existed.setResolved(0);
            wrongQuestionMapper.updateById(existed);
            return;
        }

        ExamWrongQuestionPO wrongQuestion = new ExamWrongQuestionPO();
        wrongQuestion.setUserId(userId);
        wrongQuestion.setQuestionId(questionId);
        wrongQuestion.setCourseId(courseId);
        wrongQuestion.setWrongCount(1);
        wrongQuestion.setLastWrongTime(LocalDateTime.now());
        wrongQuestion.setResolved(0);
        wrongQuestion.setDeleted(0);
        try {
            wrongQuestionMapper.insert(wrongQuestion);
        } catch (DuplicateKeyException ignored) {
            recordWrongQuestion(userId, questionId, courseId);
        }
    }

    private List<WrongQuestionVO> toWrongQuestionVOList(List<ExamWrongQuestionPO> wrongQuestions) {
        if (CollectionUtils.isEmpty(wrongQuestions)) {
            return Collections.emptyList();
        }
        Set<Long> questionIds = wrongQuestions.stream()
                .map(ExamWrongQuestionPO::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, ExamQuestionPO> questionMap = questionIds.isEmpty()
                ? Collections.emptyMap()
                : questionMapper.selectBatchIds(questionIds).stream()
                        .collect(Collectors.toMap(ExamQuestionPO::getId, Function.identity(), (left, right) -> left));
        return wrongQuestions.stream()
                .map(wrongQuestion -> toWrongQuestionVO(wrongQuestion, questionMap.get(wrongQuestion.getQuestionId())))
                .toList();
    }

    private WrongQuestionVO toWrongQuestionVO(ExamWrongQuestionPO wrongQuestion, ExamQuestionPO question) {
        return BeanCopyUtils.copyBean(wrongQuestion, WrongQuestionVO.class, (source, target) -> {
            target.setResolved(Objects.equals(source.getResolved(), 1));
            if (question != null) {
                target.setQuestionType(question.getQuestionType());
                target.setStem(question.getStem());
                target.setAnswerText(question.getAnswerText());
                target.setAnalysis(question.getAnalysis());
            }
        });
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
