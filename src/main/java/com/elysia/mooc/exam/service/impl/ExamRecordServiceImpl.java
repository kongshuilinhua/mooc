package com.elysia.mooc.exam.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.exam.constants.ExamErrorCode;
import com.elysia.mooc.exam.domain.bo.GradingResult;
import com.elysia.mooc.exam.domain.dto.SubmitAnswerRequest;
import com.elysia.mooc.exam.domain.dto.SubmitExamRequest;
import com.elysia.mooc.exam.domain.enums.ExamPaperStatus;
import com.elysia.mooc.exam.domain.enums.ExamRecordStatus;
import com.elysia.mooc.exam.domain.po.ExamAnswerRecordPO;
import com.elysia.mooc.exam.domain.po.ExamPaperPO;
import com.elysia.mooc.exam.domain.po.ExamPaperQuestionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionOptionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.domain.po.ExamRecordPO;
import com.elysia.mooc.exam.domain.vo.ExamAnswerRecordVO;
import com.elysia.mooc.exam.domain.vo.ExamRecordVO;
import com.elysia.mooc.exam.mapper.ExamAnswerRecordMapper;
import com.elysia.mooc.exam.mapper.ExamPaperMapper;
import com.elysia.mooc.exam.mapper.ExamPaperQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamQuestionOptionMapper;
import com.elysia.mooc.exam.mapper.ExamRecordMapper;
import com.elysia.mooc.exam.service.AutoGradingService;
import com.elysia.mooc.exam.service.ExamRecordService;
import com.elysia.mooc.exam.service.WrongQuestionService;
import java.math.BigDecimal;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/** 作答记录服务实现。 */
@Service
@RequiredArgsConstructor
public class ExamRecordServiceImpl implements ExamRecordService {

    private final UserContextService userContextService;
    private final ExamPaperMapper paperMapper;
    private final ExamPaperQuestionMapper paperQuestionMapper;
    private final ExamQuestionMapper questionMapper;
    private final ExamQuestionOptionMapper optionMapper;
    private final ExamRecordMapper recordMapper;
    private final ExamAnswerRecordMapper answerRecordMapper;
    private final AutoGradingService autoGradingService;
    private final WrongQuestionService wrongQuestionService;

    /**
     * 提交试卷作答并自动判分。
     *
     * @param request 提交作答请求
     * @return 作答结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamRecordVO submit(SubmitExamRequest request) {
        Long userId = userContextService.currentUserId();
        ExamPaperPO paper = requirePublishedPaper(request.getPaperId());
        List<ExamPaperQuestionPO> relations = requirePaperQuestions(paper.getId());
        Map<Long, ExamQuestionPO> questionMap = mapQuestions(relations);
        Map<Long, List<ExamQuestionOptionPO>> optionMap = mapOptions(questionMap.keySet());
        Map<Long, String> answerMap = request.getAnswers().stream()
                .collect(Collectors.toMap(SubmitAnswerRequest::getQuestionId, SubmitAnswerRequest::getAnswer));
        Set<Long> paperQuestionIds = relations.stream()
                .map(ExamPaperQuestionPO::getQuestionId)
                .collect(Collectors.toSet());
        if (!paperQuestionIds.containsAll(answerMap.keySet())) {
            throw new BizException(ExamErrorCode.PAPER_QUESTION_INVALID, "答案中包含不属于该试卷的题目");
        }

        LocalDateTime now = LocalDateTime.now();
        ExamRecordPO record = new ExamRecordPO();
        record.setPaperId(paper.getId());
        record.setUserId(userId);
        record.setStartTime(now);
        record.setSubmitTime(now);
        record.setScore(BigDecimal.ZERO);
        record.setStatus(ExamRecordStatus.DOING);
        record.setDeleted(0);
        recordMapper.insert(record);

        BigDecimal totalScore = BigDecimal.ZERO;
        boolean manualReviewRequired = false;
        List<ExamAnswerRecordVO> answers = relations.stream()
                .map(relation -> {
                    ExamQuestionPO question = questionMap.get(relation.getQuestionId());
                    GradingResult result = gradeQuestion(
                            question,
                            optionMap.getOrDefault(relation.getQuestionId(), Collections.emptyList()),
                            answerMap.get(relation.getQuestionId()),
                            relation.getScore());
                    saveAnswerRecord(record.getId(), relation.getQuestionId(), answerMap.get(relation.getQuestionId()), result);
                    if (Boolean.FALSE.equals(result.correct())) {
                        wrongQuestionService.recordWrongQuestion(userId, relation.getQuestionId(), paper.getCourseId());
                    }
                    return ExamAnswerRecordVO.builder()
                            .questionId(relation.getQuestionId())
                            .answerContent(answerMap.get(relation.getQuestionId()))
                            .correct(result.correct())
                            .score(result.score())
                            .teacherComment(result.comment())
                            .build();
                })
                .toList();

        for (ExamAnswerRecordVO answer : answers) {
            totalScore = totalScore.add(answer.getScore() == null ? BigDecimal.ZERO : answer.getScore());
            manualReviewRequired = manualReviewRequired || answer.getCorrect() == null;
        }

        record.setScore(totalScore);
        record.setStatus(manualReviewRequired ? ExamRecordStatus.SUBMITTED : ExamRecordStatus.GRADED);
        record.setPassed(manualReviewRequired ? null : (totalScore.compareTo(safeScore(paper.getPassScore())) >= 0 ? 1 : 0));
        recordMapper.updateById(record);
        return toRecordVO(record, manualReviewRequired, answers);
    }

    private GradingResult gradeQuestion(
            ExamQuestionPO question,
            List<ExamQuestionOptionPO> options,
            String answer,
            BigDecimal score) {
        if (question == null) {
            throw new BizException(ExamErrorCode.PAPER_QUESTION_INVALID, "试卷包含不存在的题目");
        }
        return autoGradingService.grade(question, options, answer, score);
    }

    private void saveAnswerRecord(Long recordId, Long questionId, String answer, GradingResult result) {
        ExamAnswerRecordPO answerRecord = new ExamAnswerRecordPO();
        answerRecord.setRecordId(recordId);
        answerRecord.setQuestionId(questionId);
        answerRecord.setAnswerContent(answer);
        answerRecord.setCorrect(result.correct() == null ? null : (Boolean.TRUE.equals(result.correct()) ? 1 : 0));
        answerRecord.setScore(result.score());
        answerRecord.setTeacherComment(result.comment());
        answerRecordMapper.insert(answerRecord);
    }

    private ExamPaperPO requirePublishedPaper(Long paperId) {
        ExamPaperPO paper = paperId == null ? null : paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BizException(ExamErrorCode.PAPER_NOT_FOUND);
        }
        if (paper.getStatus() != ExamPaperStatus.PUBLISHED) {
            throw new BizException(ExamErrorCode.PAPER_STATUS_INVALID);
        }
        return paper;
    }

    private List<ExamPaperQuestionPO> requirePaperQuestions(Long paperId) {
        List<ExamPaperQuestionPO> relations = paperQuestionMapper.selectList(Wrappers.<ExamPaperQuestionPO>lambdaQuery()
                .eq(ExamPaperQuestionPO::getPaperId, paperId)
                .orderByAsc(ExamPaperQuestionPO::getSort)
                .orderByAsc(ExamPaperQuestionPO::getId));
        if (CollectionUtils.isEmpty(relations)) {
            throw new BizException(ExamErrorCode.PAPER_QUESTION_INVALID);
        }
        return relations;
    }

    private Map<Long, ExamQuestionPO> mapQuestions(List<ExamPaperQuestionPO> relations) {
        Set<Long> questionIds = relations.stream()
                .map(ExamPaperQuestionPO::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(ExamQuestionPO::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, List<ExamQuestionOptionPO>> mapOptions(Set<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyMap();
        }
        return optionMapper.selectList(Wrappers.<ExamQuestionOptionPO>lambdaQuery()
                        .in(ExamQuestionOptionPO::getQuestionId, questionIds)
                        .orderByAsc(ExamQuestionOptionPO::getSort)
                        .orderByAsc(ExamQuestionOptionPO::getId))
                .stream()
                .collect(Collectors.groupingBy(ExamQuestionOptionPO::getQuestionId));
    }

    private ExamRecordVO toRecordVO(
            ExamRecordPO record,
            boolean manualReviewRequired,
            List<ExamAnswerRecordVO> answers) {
        return BeanCopyUtils.copyBean(record, ExamRecordVO.class, (source, target) -> {
            target.setPassed(source.getPassed() == null ? null : Objects.equals(source.getPassed(), 1));
            target.setManualReviewRequired(manualReviewRequired);
            target.setAnswers(answers);
        });
    }

    private BigDecimal safeScore(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
