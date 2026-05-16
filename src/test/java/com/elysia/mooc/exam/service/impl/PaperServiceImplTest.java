package com.elysia.mooc.exam.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.exam.domain.dto.CreatePaperRequest;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.elysia.mooc.exam.domain.po.ExamPaperPO;
import com.elysia.mooc.exam.domain.po.ExamPaperQuestionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.domain.vo.PaperVO;
import com.elysia.mooc.exam.mapper.ExamPaperMapper;
import com.elysia.mooc.exam.mapper.ExamPaperQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamQuestionMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 试卷服务测试。 */
@ExtendWith(MockitoExtension.class)
class PaperServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ExamQuestionMapper questionMapper;

    @Mock
    private ExamPaperMapper paperMapper;

    @Mock
    private ExamPaperQuestionMapper paperQuestionMapper;

    @InjectMocks
    private PaperServiceImpl paperService;

    @Test
    void createPaperShouldCalculateTotalScoreAndBindQuestions() {
        when(userContextService.currentLoginUser())
                .thenReturn(new LoginUser(1L, "admin", List.of("ADMIN"), List.of()));
        CoursePO course = new CoursePO();
        course.setId(3001L);
        when(courseMapper.selectById(3001L)).thenReturn(course);
        doAnswer(invocation -> {
            invocation.<ExamPaperPO>getArgument(0).setId(20101L);
            return 1;
        }).when(paperMapper).insert(any(ExamPaperPO.class));
        when(questionMapper.selectBatchIds(List.of(20001L, 20002L)))
                .thenReturn(List.of(question(20001L, "单选", "5.00"), question(20002L, "判断", "5.00")));
        when(paperQuestionMapper.selectList(any())).thenReturn(List.of(
                relation(20101L, 20001L, "5.00", 1),
                relation(20101L, 20002L, "5.00", 2)));
        CreatePaperRequest request = new CreatePaperRequest();
        request.setCourseId(3001L);
        request.setTitle("认证基础测验");
        request.setQuestionIds(List.of(20001L, 20002L));

        PaperVO result = paperService.createPaper(request);

        ArgumentCaptor<ExamPaperPO> paperCaptor = ArgumentCaptor.forClass(ExamPaperPO.class);
        verify(paperMapper).insert(paperCaptor.capture());
        assertThat(paperCaptor.getValue().getTotalScore()).isEqualByComparingTo("10.00");
        assertThat(paperCaptor.getValue().getPassScore()).isEqualByComparingTo("6.00");
        verify(paperQuestionMapper, times(2)).insert(any(ExamPaperQuestionPO.class));
        assertThat(result.getQuestions()).hasSize(2);
    }

    private ExamQuestionPO question(Long id, String stem, String score) {
        ExamQuestionPO question = new ExamQuestionPO();
        question.setId(id);
        question.setCourseId(3001L);
        question.setQuestionType(ExamQuestionType.SINGLE);
        question.setStem(stem);
        question.setScore(new BigDecimal(score));
        question.setStatus(EnableStatus.ENABLED);
        return question;
    }

    private ExamPaperQuestionPO relation(Long paperId, Long questionId, String score, int sort) {
        ExamPaperQuestionPO relation = new ExamPaperQuestionPO();
        relation.setPaperId(paperId);
        relation.setQuestionId(questionId);
        relation.setScore(new BigDecimal(score));
        relation.setSort(sort);
        return relation;
    }
}
