package com.elysia.mooc.studyarchive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.chat.domain.po.AiConversationPO;
import com.elysia.mooc.ai.chat.mapper.AiConversationMapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.learning.domain.enums.LearningFinishedStatus;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.domain.po.LearningRecordPO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.learning.mapper.LearningRecordMapper;
import com.elysia.mooc.studyarchive.constants.StudyArchiveConstants;
import com.elysia.mooc.studyarchive.constants.StudyArchiveErrorCode;
import com.elysia.mooc.studyarchive.domain.dto.CreateLearningNoteRequest;
import com.elysia.mooc.studyarchive.domain.dto.DailyReportQuery;
import com.elysia.mooc.studyarchive.domain.dto.WrongBookQuery;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteStatus;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteType;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookMasteryLevel;
import com.elysia.mooc.studyarchive.domain.po.LearningNotePO;
import com.elysia.mooc.studyarchive.domain.po.LearningReportPO;
import com.elysia.mooc.studyarchive.domain.po.LearningWrongBookPO;
import com.elysia.mooc.studyarchive.domain.vo.LearningNoteVO;
import com.elysia.mooc.studyarchive.domain.vo.LearningReportVO;
import com.elysia.mooc.studyarchive.domain.vo.WrongBookItemVO;
import com.elysia.mooc.studyarchive.mapper.LearningNoteMapper;
import com.elysia.mooc.studyarchive.mapper.LearningReportMapper;
import com.elysia.mooc.studyarchive.mapper.LearningWrongBookMapper;
import com.elysia.mooc.studyarchive.service.StudyArchiveService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 学习档案服务实现。 */
@Service
@RequiredArgsConstructor
public class StudyArchiveServiceImpl implements StudyArchiveService {

    private static final String REPORT_SOURCE_PERSISTED = "PERSISTED";
    private static final String REPORT_SOURCE_AGGREGATED = "AGGREGATED";
    private static final String REVIEW_LEVEL_HIGH = "HIGH";
    private static final String REVIEW_LEVEL_MEDIUM = "MEDIUM";
    private static final String REVIEW_LEVEL_LOW = "LOW";

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final CourseSectionMapper courseSectionMapper;
    private final LearningCourseMapper learningCourseMapper;
    private final LearningRecordMapper learningRecordMapper;
    private final AiConversationMapper aiConversationMapper;
    private final LearningNoteMapper learningNoteMapper;
    private final LearningWrongBookMapper learningWrongBookMapper;
    private final LearningReportMapper learningReportMapper;

    /**
     * 保存当前学生的学习笔记。
     *
     * @param request 笔记保存请求
     * @return 保存后的笔记信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningNoteVO saveNote(CreateLearningNoteRequest request) {
        LoginUser loginUser = requireStudentArchivePermission();
        CoursePO course = requireCourse(request.getCourseId());
        validateSection(request.getSectionId(), course.getId());
        requireJoinedCourse(loginUser.getUserId(), course.getId());

        LearningNotePO note = new LearningNotePO();
        note.setStudentId(loginUser.getUserId());
        note.setCourseId(course.getId());
        note.setSectionId(request.getSectionId());
        note.setContent(buildContent(request));
        note.setNoteType(request.getNoteType() == null ? LearningNoteType.TEXT : request.getNoteType());
        note.setStatus(request.getStatus() == null ? LearningNoteStatus.NORMAL : request.getStatus());
        note.setDeleted(0);
        learningNoteMapper.insert(note);
        return toNoteVO(note);
    }

    /**
     * 分页查询当前学生错题本。
     *
     * @param query 错题本查询条件
     * @return 错题本分页
     */
    @Override
    public PageResult<WrongBookItemVO> listWrongBook(WrongBookQuery query) {
        LoginUser loginUser = requireStudentArchivePermission();
        WrongBookQuery safeQuery = query == null ? new WrongBookQuery() : query;
        LambdaQueryWrapper<LearningWrongBookPO> wrapper = Wrappers.<LearningWrongBookPO>lambdaQuery()
                .eq(LearningWrongBookPO::getStudentId, loginUser.getUserId());
        if (safeQuery.getSourceType() != null) {
            wrapper.eq(LearningWrongBookPO::getSourceType, safeQuery.getSourceType());
        }
        if (safeQuery.getMasteryLevel() != null) {
            wrapper.eq(LearningWrongBookPO::getMasteryLevel, safeQuery.getMasteryLevel());
        }
        wrapper.orderByDesc(LearningWrongBookPO::getLastWrongTime)
                .orderByDesc(LearningWrongBookPO::getId);

        Page<LearningWrongBookPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<LearningWrongBookPO> result = learningWrongBookMapper.selectPage(page, wrapper);
        return PageResult.of(result, this::toWrongBookItemVO);
    }

    /**
     * 查询当前学生学习日报。
     *
     * @param query 日报查询条件
     * @return 学习日报
     */
    @Override
    public LearningReportVO getDailyReport(DailyReportQuery query) {
        LoginUser loginUser = requireStudentArchivePermission();
        Long userId = loginUser.getUserId();
        LocalDate reportDate = resolveReportDate(query);
        LearningReportPO report = learningReportMapper.selectOne(Wrappers.<LearningReportPO>lambdaQuery()
                .eq(LearningReportPO::getStudentId, userId)
                .eq(LearningReportPO::getReportDate, reportDate)
                .last("LIMIT 1"));
        if (report != null) {
            return toReportVO(report, REPORT_SOURCE_PERSISTED);
        }
        return aggregateDailyReport(userId, reportDate);
    }

    private LoginUser requireStudentArchivePermission() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (hasRole(loginUser, StudyArchiveConstants.ROLE_STUDENT)) {
            return loginUser;
        }
        throw new BizException(StudyArchiveErrorCode.STUDY_ARCHIVE_FORBIDDEN);
    }

    private CoursePO requireCourse(Long courseId) {
        CoursePO course = courseId == null ? null : courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(StudyArchiveErrorCode.STUDY_ARCHIVE_COURSE_NOT_FOUND);
        }
        return course;
    }

    private void validateSection(Long sectionId, Long courseId) {
        if (sectionId == null) {
            return;
        }
        CourseSectionPO section = courseSectionMapper.selectById(sectionId);
        if (section == null || !Objects.equals(section.getCourseId(), courseId)) {
            throw new BizException(StudyArchiveErrorCode.STUDY_ARCHIVE_SECTION_INVALID);
        }
    }

    private void requireJoinedCourse(Long userId, Long courseId) {
        Long count = learningCourseMapper.selectCount(Wrappers.<LearningCoursePO>lambdaQuery()
                .eq(LearningCoursePO::getUserId, userId)
                .eq(LearningCoursePO::getCourseId, courseId));
        if (count == null || count <= 0) {
            throw new BizException(StudyArchiveErrorCode.STUDY_ARCHIVE_COURSE_NOT_JOINED);
        }
    }

    private String buildContent(CreateLearningNoteRequest request) {
        // 前端旧字段 title/tags 没有独立 SQL 字段，按兼容信息并入正文，避免临时扩表破坏 day26 合同。
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(request.getTitle())) {
            builder.append("标题：").append(request.getTitle().trim()).append(System.lineSeparator());
        }
        if (!CollectionUtils.isEmpty(request.getTags())) {
            String tags = request.getTags().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.joining(","));
            if (StringUtils.hasText(tags)) {
                builder.append("标签：").append(tags).append(System.lineSeparator());
            }
        }
        builder.append(request.getContent().trim());
        return builder.toString();
    }

    private LocalDate resolveReportDate(DailyReportQuery query) {
        DailyReportQuery safeQuery = query == null ? new DailyReportQuery() : query;
        LocalDate reportDate = safeQuery.getBizDate();
        if (reportDate == null) {
            reportDate = safeQuery.getEndDate();
        }
        if (reportDate == null) {
            reportDate = safeQuery.getStartDate();
        }
        if (reportDate == null) {
            reportDate = LocalDate.now();
        }
        if (safeQuery.getStartDate() != null
                && safeQuery.getEndDate() != null
                && safeQuery.getStartDate().isAfter(safeQuery.getEndDate())) {
            throw new BizException(StudyArchiveErrorCode.STUDY_ARCHIVE_REPORT_DATE_INVALID, "开始日期不能晚于结束日期");
        }
        if (reportDate.isAfter(LocalDate.now())) {
            throw new BizException(StudyArchiveErrorCode.STUDY_ARCHIVE_REPORT_DATE_INVALID);
        }
        return reportDate;
    }

    private LearningReportVO aggregateDailyReport(Long userId, LocalDate reportDate) {
        LocalDateTime start = reportDate.atStartOfDay();
        LocalDateTime end = reportDate.plusDays(1).atStartOfDay();
        List<LearningRecordPO> records = learningRecordMapper.selectList(Wrappers.<LearningRecordPO>lambdaQuery()
                .eq(LearningRecordPO::getUserId, userId)
                .ge(LearningRecordPO::getLastHeartbeatTime, start)
                .lt(LearningRecordPO::getLastHeartbeatTime, end));
        int studyMinutes = records.stream()
                .map(LearningRecordPO::getLearnedSeconds)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum() / 60;
        int completedSections = (int) records.stream()
                .filter(record -> record.getFinished() == LearningFinishedStatus.FINISHED)
                .map(LearningRecordPO::getSectionId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        int wrongCount = countWrongBook(userId, start, end);
        int aiAskCount = countAiConversations(userId, start, end);

        LearningReportVO report = new LearningReportVO();
        report.setReportDate(reportDate);
        report.setStudyMinutes(studyMinutes);
        report.setLearningMinutes(studyMinutes);
        report.setCompletedSections(completedSections);
        report.setWrongCount(wrongCount);
        report.setAiAskCount(aiAskCount);
        report.setSource(REPORT_SOURCE_AGGREGATED);
        return report;
    }

    private int countWrongBook(Long userId, LocalDateTime start, LocalDateTime end) {
        Long count = learningWrongBookMapper.selectCount(Wrappers.<LearningWrongBookPO>lambdaQuery()
                .eq(LearningWrongBookPO::getStudentId, userId)
                .ge(LearningWrongBookPO::getLastWrongTime, start)
                .lt(LearningWrongBookPO::getLastWrongTime, end));
        return count == null ? 0 : count.intValue();
    }

    private int countAiConversations(Long userId, LocalDateTime start, LocalDateTime end) {
        Long count = aiConversationMapper.selectCount(Wrappers.<AiConversationPO>lambdaQuery()
                .eq(AiConversationPO::getUserId, userId)
                .ge(AiConversationPO::getCreateTime, start)
                .lt(AiConversationPO::getCreateTime, end));
        return count == null ? 0 : count.intValue();
    }

    private LearningNoteVO toNoteVO(LearningNotePO note) {
        return BeanCopyUtils.copyBean(note, LearningNoteVO.class, (source, target) -> {
            target.setNoteTypeDesc(source.getNoteType() == null ? null : source.getNoteType().getDesc());
            target.setStatusDesc(source.getStatus() == null ? null : source.getStatus().getDesc());
        });
    }

    private WrongBookItemVO toWrongBookItemVO(LearningWrongBookPO wrongBook) {
        return BeanCopyUtils.copyBean(wrongBook, WrongBookItemVO.class, (source, target) -> {
            target.setSourceTypeDesc(source.getSourceType() == null ? null : source.getSourceType().getDesc());
            target.setMasteryLevelDesc(source.getMasteryLevel() == null ? null : source.getMasteryLevel().getDesc());
            target.setReviewLevel(resolveReviewLevel(source.getMasteryLevel(), source.getWrongCount()));
        });
    }

    private LearningReportVO toReportVO(LearningReportPO report, String source) {
        return BeanCopyUtils.copyBean(report, LearningReportVO.class, (po, target) -> {
            target.setLearningMinutes(po.getStudyMinutes());
            target.setSource(source);
        });
    }

    private String resolveReviewLevel(WrongBookMasteryLevel masteryLevel, Integer wrongCount) {
        if (masteryLevel == WrongBookMasteryLevel.LOW || safeInt(wrongCount) >= 3) {
            return REVIEW_LEVEL_HIGH;
        }
        if (masteryLevel == WrongBookMasteryLevel.MEDIUM || safeInt(wrongCount) == 2) {
            return REVIEW_LEVEL_MEDIUM;
        }
        return REVIEW_LEVEL_LOW;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        return loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(roleCode::equalsIgnoreCase);
    }

}
