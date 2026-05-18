package com.elysia.mooc.studyarchive.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.chat.mapper.AiConversationMapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.learning.domain.enums.LearningFinishedStatus;
import com.elysia.mooc.learning.domain.po.LearningRecordPO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.learning.mapper.LearningRecordMapper;
import com.elysia.mooc.studyarchive.constants.StudyArchiveErrorCode;
import com.elysia.mooc.studyarchive.domain.dto.CreateLearningNoteRequest;
import com.elysia.mooc.studyarchive.domain.dto.DailyReportQuery;
import com.elysia.mooc.studyarchive.domain.dto.WrongBookQuery;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteStatus;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteType;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookMasteryLevel;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookSourceType;
import com.elysia.mooc.studyarchive.domain.po.LearningNotePO;
import com.elysia.mooc.studyarchive.domain.po.LearningReportPO;
import com.elysia.mooc.studyarchive.domain.po.LearningWrongBookPO;
import com.elysia.mooc.studyarchive.domain.vo.LearningNoteVO;
import com.elysia.mooc.studyarchive.domain.vo.LearningReportVO;
import com.elysia.mooc.studyarchive.domain.vo.WrongBookItemVO;
import com.elysia.mooc.studyarchive.mapper.LearningNoteMapper;
import com.elysia.mooc.studyarchive.mapper.LearningReportMapper;
import com.elysia.mooc.studyarchive.mapper.LearningWrongBookMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 学习档案服务测试。 */
@ExtendWith(MockitoExtension.class)
class StudyArchiveServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseSectionMapper courseSectionMapper;

    @Mock
    private LearningCourseMapper learningCourseMapper;

    @Mock
    private LearningRecordMapper learningRecordMapper;

    @Mock
    private AiConversationMapper aiConversationMapper;

    @Mock
    private LearningNoteMapper learningNoteMapper;

    @Mock
    private LearningWrongBookMapper learningWrongBookMapper;

    @Mock
    private LearningReportMapper learningReportMapper;

    @InjectMocks
    private StudyArchiveServiceImpl studyArchiveService;

    @Test
    void saveNoteShouldInsertCurrentStudentNoteWhenJoinedCourse() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L));
        when(courseSectionMapper.selectById(5003L)).thenReturn(section(5003L, 3001L));
        when(learningCourseMapper.selectCount(any())).thenReturn(1L);
        doAnswer(invocation -> {
            LearningNotePO note = invocation.getArgument(0);
            note.setId(26001L);
            note.setCreateTime(LocalDateTime.now());
            return 1;
        }).when(learningNoteMapper).insert(any(LearningNotePO.class));

        CreateLearningNoteRequest request = new CreateLearningNoteRequest();
        request.setCourseId(3001L);
        request.setSectionId(5003L);
        request.setTitle("认证笔记");
        request.setTags(List.of("JWT", "RBAC"));
        request.setContent("refresh token 需要轮换");

        LearningNoteVO result = studyArchiveService.saveNote(request);

        ArgumentCaptor<LearningNotePO> captor = ArgumentCaptor.forClass(LearningNotePO.class);
        verify(learningNoteMapper).insert(captor.capture());
        assertThat(captor.getValue().getStudentId()).isEqualTo(3L);
        assertThat(captor.getValue().getNoteType()).isEqualTo(LearningNoteType.TEXT);
        assertThat(captor.getValue().getStatus()).isEqualTo(LearningNoteStatus.NORMAL);
        assertThat(captor.getValue().getContent()).contains("标题：认证笔记", "标签：JWT,RBAC", "refresh token");
        assertThat(result.getId()).isEqualTo(26001L);
    }

    @Test
    void saveNoteShouldRejectWhenStudentNotJoinedCourse() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L));
        when(learningCourseMapper.selectCount(any())).thenReturn(0L);

        CreateLearningNoteRequest request = new CreateLearningNoteRequest();
        request.setCourseId(3001L);
        request.setContent("未选课笔记");

        assertThatThrownBy(() -> studyArchiveService.saveNote(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(StudyArchiveErrorCode.STUDY_ARCHIVE_COURSE_NOT_JOINED.code());
    }

    @Test
    void saveNoteShouldRejectSectionFromOtherCourse() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L));
        when(courseSectionMapper.selectById(5003L)).thenReturn(section(5003L, 9999L));

        CreateLearningNoteRequest request = new CreateLearningNoteRequest();
        request.setCourseId(3001L);
        request.setSectionId(5003L);
        request.setContent("小节不匹配");

        assertThatThrownBy(() -> studyArchiveService.saveNote(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(StudyArchiveErrorCode.STUDY_ARCHIVE_SECTION_INVALID.code());
    }

    @Test
    void listWrongBookShouldFilterByCurrentStudentAndReturnPageResult() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        Page<LearningWrongBookPO> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(wrongBook()));
        when(learningWrongBookMapper.selectPage(any(), any())).thenReturn(page);

        WrongBookQuery query = new WrongBookQuery();
        query.setSourceType(WrongBookSourceType.EXAM);
        query.setMasteryLevel(WrongBookMasteryLevel.LOW);

        PageResult<WrongBookItemVO> result = studyArchiveService.listWrongBook(query);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getReviewLevel()).isEqualTo("HIGH");
        verify(learningWrongBookMapper).selectPage(any(), any());
    }

    @Test
    void getDailyReportShouldReadPersistedReportFirst() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(learningReportMapper.selectOne(any())).thenReturn(report(LocalDate.of(2026, 5, 18)));

        DailyReportQuery query = new DailyReportQuery();
        query.setBizDate(LocalDate.of(2026, 5, 18));

        LearningReportVO result = studyArchiveService.getDailyReport(query);

        assertThat(result.getReportDate()).isEqualTo(LocalDate.of(2026, 5, 18));
        assertThat(result.getStudyMinutes()).isEqualTo(68);
        assertThat(result.getLearningMinutes()).isEqualTo(68);
        assertThat(result.getSource()).isEqualTo("PERSISTED");
    }

    @Test
    void getDailyReportShouldAggregateWhenPersistedReportMissing() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(learningReportMapper.selectOne(any())).thenReturn(null);
        LearningRecordPO finished = learningRecord(5003L, 1800, LearningFinishedStatus.FINISHED);
        LearningRecordPO unfinished = learningRecord(5004L, 900, LearningFinishedStatus.UNFINISHED);
        when(learningRecordMapper.selectList(any())).thenReturn(List.of(finished, unfinished));
        when(learningWrongBookMapper.selectCount(any())).thenReturn(2L);
        when(aiConversationMapper.selectCount(any())).thenReturn(3L);

        DailyReportQuery query = new DailyReportQuery();
        query.setBizDate(LocalDate.of(2026, 5, 18));

        LearningReportVO result = studyArchiveService.getDailyReport(query);

        assertThat(result.getStudyMinutes()).isEqualTo(45);
        assertThat(result.getCompletedSections()).isEqualTo(1);
        assertThat(result.getWrongCount()).isEqualTo(2);
        assertThat(result.getAiAskCount()).isEqualTo(3);
        assertThat(result.getSource()).isEqualTo("AGGREGATED");
    }

    @Test
    void teacherShouldNotAccessStudentArchiveService() {
        when(userContextService.currentLoginUser()).thenReturn(new LoginUser(2L, "teacher", List.of("TEACHER"), List.of()));

        WrongBookQuery query = new WrongBookQuery();

        assertThatThrownBy(() -> studyArchiveService.listWrongBook(query))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(StudyArchiveErrorCode.STUDY_ARCHIVE_FORBIDDEN.code());
    }

    private LoginUser student() {
        return new LoginUser(3L, "student", List.of("STUDENT"), List.of());
    }

    private CoursePO course(Long id) {
        CoursePO course = new CoursePO();
        course.setId(id);
        return course;
    }

    private CourseSectionPO section(Long id, Long courseId) {
        CourseSectionPO section = new CourseSectionPO();
        section.setId(id);
        section.setCourseId(courseId);
        return section;
    }

    private LearningWrongBookPO wrongBook() {
        LearningWrongBookPO wrongBook = new LearningWrongBookPO();
        wrongBook.setId(26101L);
        wrongBook.setStudentId(3L);
        wrongBook.setQuestionId(20002L);
        wrongBook.setSourceType(WrongBookSourceType.EXAM);
        wrongBook.setWrongCount(3);
        wrongBook.setMasteryLevel(WrongBookMasteryLevel.LOW);
        wrongBook.setLastWrongTime(LocalDateTime.now());
        return wrongBook;
    }

    private LearningReportPO report(LocalDate reportDate) {
        LearningReportPO report = new LearningReportPO();
        report.setId(26201L);
        report.setStudentId(3L);
        report.setReportDate(reportDate);
        report.setStudyMinutes(68);
        report.setCompletedSections(2);
        report.setWrongCount(1);
        report.setAiAskCount(3);
        return report;
    }

    private LearningRecordPO learningRecord(Long sectionId, Integer learnedSeconds, LearningFinishedStatus finished) {
        LearningRecordPO record = new LearningRecordPO();
        record.setSectionId(sectionId);
        record.setLearnedSeconds(learnedSeconds);
        record.setFinished(finished);
        return record;
    }
}
