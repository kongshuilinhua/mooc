package com.elysia.mooc.exam.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.exam.domain.dto.WrongQuestionQuery;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.domain.po.ExamWrongQuestionPO;
import com.elysia.mooc.exam.domain.vo.WrongQuestionVO;
import com.elysia.mooc.exam.mapper.ExamQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamWrongQuestionMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 错题本服务测试。 */
@ExtendWith(MockitoExtension.class)
class WrongQuestionServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private ExamWrongQuestionMapper wrongQuestionMapper;

    @Mock
    private ExamQuestionMapper questionMapper;

    @InjectMocks
    private WrongQuestionServiceImpl wrongQuestionService;

    @Test
    void recordWrongQuestionShouldInsertWhenNotExists() {
        when(wrongQuestionMapper.selectOne(any())).thenReturn(null);

        wrongQuestionService.recordWrongQuestion(3L, 20002L, 3001L);

        ArgumentCaptor<ExamWrongQuestionPO> captor = ArgumentCaptor.forClass(ExamWrongQuestionPO.class);
        verify(wrongQuestionMapper).insert(captor.capture());
        assertThat(captor.getValue().getWrongCount()).isEqualTo(1);
        assertThat(captor.getValue().getResolved()).isZero();
    }

    @Test
    void recordWrongQuestionShouldIncrementWhenExists() {
        ExamWrongQuestionPO existed = new ExamWrongQuestionPO();
        existed.setId(20301L);
        existed.setWrongCount(2);
        existed.setResolved(1);
        when(wrongQuestionMapper.selectOne(any())).thenReturn(existed);

        wrongQuestionService.recordWrongQuestion(3L, 20002L, 3001L);

        assertThat(existed.getWrongCount()).isEqualTo(3);
        assertThat(existed.getResolved()).isZero();
        verify(wrongQuestionMapper).updateById(existed);
    }

    @Test
    void listWrongQuestionsShouldSupportKeywordAndResolvedFilter() {
        when(userContextService.currentUserId()).thenReturn(3L);
        ExamQuestionPO question = new ExamQuestionPO();
        question.setId(20002L);
        question.setStem("RBAC 的核心是什么？");
        when(questionMapper.selectList(any())).thenReturn(List.of(question));

        Page<ExamWrongQuestionPO> resultPage = new Page<>(1, 10);
        resultPage.setTotal(1);
        resultPage.setRecords(List.of(wrongQuestion()));
        when(wrongQuestionMapper.selectPage(any(), any())).thenReturn(resultPage);
        when(questionMapper.selectBatchIds(any())).thenReturn(List.of(question));

        WrongQuestionQuery query = new WrongQuestionQuery();
        query.setKeyword("RBAC");
        query.setResolved(true);

        PageResult<WrongQuestionVO> result = wrongQuestionService.listWrongQuestions(query);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getStem()).isEqualTo("RBAC 的核心是什么？");
        verify(questionMapper).selectList(any());
        verify(wrongQuestionMapper).selectPage(any(), any());
    }

    @Test
    void listWrongQuestionsShouldReturnEmptyWhenKeywordNotMatched() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(questionMapper.selectList(any())).thenReturn(List.of());

        WrongQuestionQuery query = new WrongQuestionQuery();
        query.setKeyword("不存在");

        PageResult<WrongQuestionVO> result = wrongQuestionService.listWrongQuestions(query);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getList()).isEmpty();
    }

    private ExamWrongQuestionPO wrongQuestion() {
        ExamWrongQuestionPO wrongQuestion = new ExamWrongQuestionPO();
        wrongQuestion.setId(20301L);
        wrongQuestion.setUserId(3L);
        wrongQuestion.setQuestionId(20002L);
        wrongQuestion.setCourseId(3001L);
        wrongQuestion.setWrongCount(2);
        wrongQuestion.setResolved(1);
        return wrongQuestion;
    }
}
