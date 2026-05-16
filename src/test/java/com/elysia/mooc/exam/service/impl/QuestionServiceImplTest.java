package com.elysia.mooc.exam.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.exam.constants.ExamErrorCode;
import com.elysia.mooc.exam.domain.dto.CreateQuestionOptionRequest;
import com.elysia.mooc.exam.domain.dto.CreateQuestionRequest;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.elysia.mooc.exam.domain.po.ExamQuestionOptionPO;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import com.elysia.mooc.exam.domain.vo.QuestionVO;
import com.elysia.mooc.exam.mapper.ExamQuestionMapper;
import com.elysia.mooc.exam.mapper.ExamQuestionOptionMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 题目服务测试。 */
@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ExamQuestionMapper questionMapper;

    @Mock
    private ExamQuestionOptionMapper optionMapper;

    @InjectMocks
    private QuestionServiceImpl questionService;

    @Test
    void createQuestionShouldInsertQuestionAndOptionsForTeacherOwner() {
        when(userContextService.currentLoginUser())
                .thenReturn(new LoginUser(2L, "teacher", List.of("TEACHER"), List.of()));
        CoursePO course = new CoursePO();
        course.setId(3001L);
        course.setTeacherId(2L);
        when(courseMapper.selectById(3001L)).thenReturn(course);
        doAnswer(invocation -> {
            invocation.<ExamQuestionPO>getArgument(0).setId(20001L);
            return 1;
        }).when(questionMapper).insert(any(ExamQuestionPO.class));
        when(optionMapper.selectList(any())).thenReturn(List.of(option(1L, "A", true), option(2L, "B", false)));
        CreateQuestionRequest request = request();

        QuestionVO result = questionService.createQuestion(request);

        ArgumentCaptor<ExamQuestionPO> questionCaptor = ArgumentCaptor.forClass(ExamQuestionPO.class);
        verify(questionMapper).insert(questionCaptor.capture());
        assertThat(questionCaptor.getValue().getCreatorId()).isEqualTo(2L);
        assertThat(questionCaptor.getValue().getDeleted()).isZero();
        verify(optionMapper, times(2)).insert(any(ExamQuestionOptionPO.class));
        assertThat(result.getStem()).isEqualTo("JWT 的主要特点是什么？");
        assertThat(result.getOptions()).hasSize(2);
    }

    @Test
    void createQuestionShouldRejectStudentInServiceLayer() {
        when(userContextService.currentLoginUser())
                .thenReturn(new LoginUser(3L, "student", List.of("STUDENT"), List.of()));
        CoursePO course = new CoursePO();
        course.setId(3001L);
        course.setTeacherId(2L);
        when(courseMapper.selectById(3001L)).thenReturn(course);

        assertThatThrownBy(() -> questionService.createQuestion(request()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ExamErrorCode.EXAM_FORBIDDEN.code());
    }

    private CreateQuestionRequest request() {
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCourseId(3001L);
        request.setQuestionType(ExamQuestionType.SINGLE);
        request.setStem("JWT 的主要特点是什么？");
        request.setAnswerText("A");
        request.setScore(new BigDecimal("5.00"));
        CreateQuestionOptionRequest optionA = new CreateQuestionOptionRequest();
        optionA.setOptionKey("A");
        optionA.setOptionText("服务端不保存会话状态");
        optionA.setCorrect(true);
        CreateQuestionOptionRequest optionB = new CreateQuestionOptionRequest();
        optionB.setOptionKey("B");
        optionB.setOptionText("只能用于单体项目");
        optionB.setCorrect(false);
        request.setOptions(List.of(optionA, optionB));
        return request;
    }

    private ExamQuestionOptionPO option(Long id, String key, boolean correct) {
        ExamQuestionOptionPO option = new ExamQuestionOptionPO();
        option.setId(id);
        option.setQuestionId(20001L);
        option.setOptionKey(key);
        option.setOptionText(key);
        option.setCorrect(correct ? 1 : 0);
        return option;
    }
}
