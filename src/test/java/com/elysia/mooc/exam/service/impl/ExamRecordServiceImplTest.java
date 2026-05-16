package com.elysia.mooc.exam.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.exam.domain.dto.SubmitAnswerRequest;
import com.elysia.mooc.exam.domain.dto.SubmitExamRequest;
import com.elysia.mooc.exam.domain.enums.ExamPaperStatus;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.elysia.mooc.exam.domain.po.ExamAnswerRecordPO;
import com.elysia.mooc.exam.domain.po.ExamPaperPO;
import com.elysia.mooc.exam.domain.po.ExamPaperQuestionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionOptionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.domain.po.ExamRecordPO;
import com.elysia.mooc.exam.domain.vo.ExamRecordVO;
import com.elysia.mooc.exam.mapper.ExamAnswerRecordMapper;
import com.elysia.mooc.exam.mapper.ExamPaperMapper;
import com.elysia.mooc.exam.mapper.ExamPaperQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamQuestionOptionMapper;
import com.elysia.mooc.exam.mapper.ExamRecordMapper;
import com.elysia.mooc.exam.service.WrongQuestionService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 作答服务测试。 */
@ExtendWith(MockitoExtension.class)
class ExamRecordServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private ExamPaperMapper paperMapper;

    @Mock
    private ExamPaperQuestionMapper paperQuestionMapper;

    @Mock
    private ExamQuestionMapper questionMapper;

    @Mock
    private ExamQuestionOptionMapper optionMapper;

    @Mock
    private ExamRecordMapper recordMapper;

    @Mock
    private ExamAnswerRecordMapper answerRecordMapper;

    @Mock
    private WrongQuestionService wrongQuestionService;

    private ExamRecordServiceImpl examRecordService;

    @BeforeEach
    void setUp() {
        examRecordService = new ExamRecordServiceImpl(
                userContextService,
                paperMapper,
                paperQuestionMapper,
                questionMapper,
                optionMapper,
                recordMapper,
                answerRecordMapper,
                new AutoGradingServiceImpl(),
                wrongQuestionService);
    }

    @Test
    void submitShouldGradeObjectiveQuestionsAndRecordWrongQuestion() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(paperMapper.selectById(20101L)).thenReturn(paper());
        when(paperQuestionMapper.selectList(any())).thenReturn(List.of(
                relation(20001L, "5.00", 1),
                relation(20002L, "5.00", 2)));
        when(questionMapper.selectBatchIds(any())).thenReturn(List.of(
                question(20001L, ExamQuestionType.SINGLE, "A"),
                question(20002L, ExamQuestionType.JUDGE, "正确")));
        when(optionMapper.selectList(any())).thenReturn(List.of(
                option(20001L, "A", true),
                option(20001L, "B", false),
                option(20002L, "正确", true),
                option(20002L, "错误", false)));
        SubmitExamRequest request = new SubmitExamRequest();
        request.setPaperId(20101L);
        request.setAnswers(List.of(answer(20001L, "A"), answer(20002L, "错误")));

        ExamRecordVO result = examRecordService.submit(request);

        assertThat(result.getScore()).isEqualByComparingTo("5.00");
        assertThat(result.getPassed()).isFalse();
        assertThat(result.getManualReviewRequired()).isFalse();
        assertThat(result.getAnswers()).hasSize(2);
        verify(wrongQuestionService).recordWrongQuestion(3L, 20002L, 3001L);
        verify(answerRecordMapper, times(2)).insert(any(ExamAnswerRecordPO.class));
        verify(recordMapper).updateById(any(ExamRecordPO.class));
    }

    @Test
    void submitShouldReturnManualReviewWhenContainsShortAnswer() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(paperMapper.selectById(20101L)).thenReturn(paper());
        when(paperQuestionMapper.selectList(any())).thenReturn(List.of(relation(20003L, "10.00", 1)));
        when(questionMapper.selectBatchIds(any())).thenReturn(List.of(question(20003L, ExamQuestionType.SHORT, "参考")));
        when(optionMapper.selectList(any())).thenReturn(List.of());
        SubmitExamRequest request = new SubmitExamRequest();
        request.setPaperId(20101L);
        request.setAnswers(List.of(answer(20003L, "学生答案")));

        ExamRecordVO result = examRecordService.submit(request);

        assertThat(result.getManualReviewRequired()).isTrue();
        assertThat(result.getPassed()).isNull();
        assertThat(result.getAnswers().get(0).getCorrect()).isNull();
        ArgumentCaptor<ExamAnswerRecordPO> captor = ArgumentCaptor.forClass(ExamAnswerRecordPO.class);
        verify(answerRecordMapper).insert(captor.capture());
        assertThat(captor.getValue().getTeacherComment()).isEqualTo("简答题需要人工批改");
    }

    private ExamPaperPO paper() {
        ExamPaperPO paper = new ExamPaperPO();
        paper.setId(20101L);
        paper.setCourseId(3001L);
        paper.setStatus(ExamPaperStatus.PUBLISHED);
        paper.setPassScore(new BigDecimal("6.00"));
        return paper;
    }

    private ExamPaperQuestionPO relation(Long questionId, String score, int sort) {
        ExamPaperQuestionPO relation = new ExamPaperQuestionPO();
        relation.setPaperId(20101L);
        relation.setQuestionId(questionId);
        relation.setScore(new BigDecimal(score));
        relation.setSort(sort);
        return relation;
    }

    private ExamQuestionPO question(Long id, ExamQuestionType type, String answer) {
        ExamQuestionPO question = new ExamQuestionPO();
        question.setId(id);
        question.setQuestionType(type);
        question.setAnswerText(answer);
        return question;
    }

    private ExamQuestionOptionPO option(Long questionId, String key, boolean correct) {
        ExamQuestionOptionPO option = new ExamQuestionOptionPO();
        option.setQuestionId(questionId);
        option.setOptionKey(key);
        option.setOptionText(key);
        option.setCorrect(correct ? 1 : 0);
        return option;
    }

    private SubmitAnswerRequest answer(Long questionId, String value) {
        SubmitAnswerRequest answer = new SubmitAnswerRequest();
        answer.setQuestionId(questionId);
        answer.setAnswer(value);
        return answer;
    }
}
