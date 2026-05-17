package com.elysia.mooc.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.event.service.BusinessEventPublisher;
import com.elysia.mooc.learning.constants.LearningConstants;
import com.elysia.mooc.learning.constants.LearningErrorCode;
import com.elysia.mooc.learning.domain.dto.JoinCourseRequest;
import com.elysia.mooc.learning.domain.dto.LearningCourseQuery;
import com.elysia.mooc.learning.domain.dto.LearningHeartbeatRequest;
import com.elysia.mooc.learning.domain.dto.LearningHistoryQuery;
import com.elysia.mooc.learning.domain.enums.LearningBehaviorType;
import com.elysia.mooc.learning.domain.enums.LearningCourseStatus;
import com.elysia.mooc.learning.domain.enums.LearningFinishedStatus;
import com.elysia.mooc.learning.domain.enums.LearningSource;
import com.elysia.mooc.learning.domain.po.LearningBehaviorLogPO;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.domain.po.LearningRecordPO;
import com.elysia.mooc.learning.domain.vo.LearningCourseItem;
import com.elysia.mooc.learning.domain.vo.LearningHistoryItem;
import com.elysia.mooc.learning.domain.vo.LearningRecordVO;
import com.elysia.mooc.learning.domain.vo.LearningStatisticsVO;
import com.elysia.mooc.learning.mapper.LearningBehaviorLogMapper;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.learning.mapper.LearningRecordMapper;
import com.elysia.mooc.learning.service.LearningService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 学习进度服务实现。 */
@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final CourseSectionMapper courseSectionMapper;
    private final LearningCourseMapper learningCourseMapper;
    private final LearningRecordMapper learningRecordMapper;
    private final LearningBehaviorLogMapper learningBehaviorLogMapper;
    private final BusinessEventPublisher businessEventPublisher;

    /**
     * 加入课程。
     *
     * @param request 加入课程请求
     * @return 是否加入成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean joinCourse(JoinCourseRequest request) {
        Long userId = userContextService.currentUserId();
        CoursePO course = requirePublishedCourse(request.getCourseId());
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(LearningErrorCode.LEARNING_COURSE_NOT_AVAILABLE, "付费课程需要购买后学习");
        }

        LearningCoursePO existed = getJoinedCourse(userId, course.getId());
        if (existed != null) {
            return Boolean.TRUE;
        }

        // 幂等先查再插，唯一索引兜底并发重复加入场景。
        LearningCoursePO learningCourse = new LearningCoursePO();
        learningCourse.setUserId(userId);
        learningCourse.setCourseId(course.getId());
        learningCourse.setSource(LearningSource.FREE);
        learningCourse.setProgressPercent(BigDecimal.ZERO);
        learningCourse.setLearnedSeconds(0);
        learningCourse.setFinished(LearningFinishedStatus.UNFINISHED);
        learningCourse.setDeleted(0);
        try {
            learningCourseMapper.insert(learningCourse);
            courseMapper.update(null, Wrappers.<CoursePO>lambdaUpdate()
                    .eq(CoursePO::getId, course.getId())
                    .setSql("learn_count = learn_count + 1"));
        } catch (DuplicateKeyException ignored) {
            return Boolean.TRUE;
        }
        return Boolean.TRUE;
    }

    /**
     * 支付成功后发放课程学习权益。
     *
     * @param userId 购买用户 ID
     * @param courseId 课程 ID
     * @return true 表示权益已存在或本次发放成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean grantPurchasedCourse(Long userId, Long courseId) {
        if (userId == null || userId <= 0 || courseId == null || courseId <= 0) {
            throw new BizException(LearningErrorCode.LEARNING_PARAM_INVALID, "支付加课参数不正确");
        }
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(LearningErrorCode.LEARNING_COURSE_NOT_AVAILABLE);
        }
        LearningCoursePO existed = getJoinedCourse(userId, course.getId());
        if (existed != null) {
            return Boolean.TRUE;
        }

        // 支付权益可能因为重复回调或事件重放被多次触发，唯一索引用来兜底并发幂等。
        LearningCoursePO learningCourse = new LearningCoursePO();
        learningCourse.setUserId(userId);
        learningCourse.setCourseId(course.getId());
        learningCourse.setSource(LearningSource.PURCHASE);
        learningCourse.setProgressPercent(BigDecimal.ZERO);
        learningCourse.setLearnedSeconds(0);
        learningCourse.setFinished(LearningFinishedStatus.UNFINISHED);
        learningCourse.setDeleted(0);
        try {
            learningCourseMapper.insert(learningCourse);
            courseMapper.update(null, Wrappers.<CoursePO>lambdaUpdate()
                    .eq(CoursePO::getId, course.getId())
                    .setSql("learn_count = learn_count + 1"));
        } catch (DuplicateKeyException ignored) {
            return Boolean.TRUE;
        }
        return Boolean.TRUE;
    }

    /**
     * 分页查询我的课程。
     *
     * @param query 查询参数
     * @return 我的课程分页
     */
    @Override
    public PageResult<LearningCourseItem> listMyCourses(LearningCourseQuery query) {
        LearningCourseQuery safeQuery = query == null ? new LearningCourseQuery() : query;
        Long userId = userContextService.currentUserId();
        Set<Long> courseIdsByKeyword = collectCourseIdsByKeyword(safeQuery.getKeyword());
        if (courseIdsByKeyword != null && courseIdsByKeyword.isEmpty()) {
            return PageResult.empty(0L, 0);
        }

        LambdaQueryWrapper<LearningCoursePO> wrapper = Wrappers.<LearningCoursePO>lambdaQuery()
                .eq(LearningCoursePO::getUserId, userId);
        if (safeQuery.getStatus() == LearningCourseStatus.LEARNING) {
            wrapper.eq(LearningCoursePO::getFinished, LearningFinishedStatus.UNFINISHED);
        } else if (safeQuery.getStatus() == LearningCourseStatus.COMPLETED) {
            wrapper.eq(LearningCoursePO::getFinished, LearningFinishedStatus.FINISHED);
        }
        if (courseIdsByKeyword != null) {
            wrapper.in(LearningCoursePO::getCourseId, courseIdsByKeyword);
        }
        applyCourseSort(wrapper, safeQuery.getSortBy(), safeQuery.getIsAsc());

        Page<LearningCoursePO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<LearningCoursePO> result = learningCourseMapper.selectPage(page, wrapper);
        return PageResult.of(result, toLearningCourseItems(result.getRecords()));
    }

    /**
     * 上报学习心跳。
     *
     * @param request 心跳请求
     * @return 更新后的学习记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningRecordVO heartbeat(LearningHeartbeatRequest request) {
        Long userId = userContextService.currentUserId();
        LearningCoursePO learningCourse = requireJoinedCourse(userId, request.getCourseId());
        CourseSectionPO section = requireEnabledSection(request.getCourseId(), request.getSectionId());
        int duration = resolveDuration(request.getDuration(), section.getDurationSeconds());
        if (duration > 0 && request.getPosition() > duration) {
            throw new BizException(LearningErrorCode.LEARNING_PARAM_INVALID, "播放位置不能超过视频总时长");
        }

        LearningRecordPO record = getLearningRecord(userId, request.getSectionId());
        LearningRecordPO updated = upsertLearningRecord(userId, request, record, duration);

        // 心跳成功后立即刷新课程维度汇总，保证“我的课程”和“继续学习”入口一致。
        refreshCourseProgress(learningCourse, section.getId());
        writeBehaviorLog(
                userId,
                request.getCourseId(),
                request.getSectionId(),
                updated.getFinished().isFinished() ? LearningBehaviorType.FINISH : LearningBehaviorType.HEARTBEAT,
                updated.getLastPosition(),
                Math.max(0, updated.getLearnedSeconds() - safeInt(record == null ? null : record.getLearnedSeconds())));

        return toRecordVO(updated);
    }

    /**
     * 分页查询学习历史。
     *
     * @param query 查询参数
     * @return 学习历史分页
     */
    @Override
    public PageResult<LearningHistoryItem> listHistory(LearningHistoryQuery query) {
        LearningHistoryQuery safeQuery = query == null ? new LearningHistoryQuery() : query;
        Long userId = userContextService.currentUserId();
        HistoryKeywordMatch keywordMatch = collectHistoryKeywordMatch(safeQuery.getKeyword());
        if (keywordMatch != null && keywordMatch.isEmpty()) {
            return PageResult.empty(0L, 0);
        }

        LambdaQueryWrapper<LearningRecordPO> wrapper = Wrappers.<LearningRecordPO>lambdaQuery()
                .eq(LearningRecordPO::getUserId, userId);
        if (safeQuery.getCourseId() != null) {
            wrapper.eq(LearningRecordPO::getCourseId, safeQuery.getCourseId());
        }
        if (safeQuery.getSectionId() != null) {
            wrapper.eq(LearningRecordPO::getSectionId, safeQuery.getSectionId());
        }
        if (keywordMatch != null) {
            wrapper.and(nested -> {
                boolean hasCourse = !keywordMatch.courseIds().isEmpty();
                boolean hasSection = !keywordMatch.sectionIds().isEmpty();
                if (hasCourse) {
                    nested.in(LearningRecordPO::getCourseId, keywordMatch.courseIds());
                }
                if (hasCourse && hasSection) {
                    nested.or();
                }
                if (hasSection) {
                    nested.in(LearningRecordPO::getSectionId, keywordMatch.sectionIds());
                }
            });
        }
        applyHistorySort(wrapper, safeQuery.getSortBy(), safeQuery.getIsAsc());

        Page<LearningRecordPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<LearningRecordPO> result = learningRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result, toHistoryItems(result.getRecords()));
    }

    /**
     * 查询当前用户学习统计。
     *
     * @return 学习统计
     */
    @Override
    public LearningStatisticsVO getStatistics() {
        Long userId = userContextService.currentUserId();
        List<LearningCoursePO> courses = learningCourseMapper.selectList(Wrappers.<LearningCoursePO>lambdaQuery()
                .eq(LearningCoursePO::getUserId, userId));
        List<LearningRecordPO> records = learningRecordMapper.selectList(Wrappers.<LearningRecordPO>lambdaQuery()
                .eq(LearningRecordPO::getUserId, userId));
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        int todaySeconds = records.stream()
                .filter(record -> record.getLastHeartbeatTime() != null
                        && !record.getLastHeartbeatTime().isBefore(todayStart))
                .map(LearningRecordPO::getLearnedSeconds)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        int totalSeconds = records.stream()
                .map(LearningRecordPO::getLearnedSeconds)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        long completedCourseCount = courses.stream()
                .filter(course -> course.getFinished() == LearningFinishedStatus.FINISHED)
                .count();
        long completedSectionCount = records.stream()
                .filter(record -> record.getFinished() == LearningFinishedStatus.FINISHED)
                .count();

        return LearningStatisticsVO.builder()
                .todayMinutes(toMinutes(todaySeconds))
                .totalMinutes(toMinutes(totalSeconds))
                .completedCourseCount(toInt(completedCourseCount))
                .completedSectionCount(toInt(completedSectionCount))
                .build();
    }

    private CoursePO requirePublishedCourse(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BizException(LearningErrorCode.LEARNING_COURSE_NOT_AVAILABLE);
        }
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BizException(LearningErrorCode.LEARNING_COURSE_NOT_AVAILABLE);
        }
        return course;
    }

    private CourseSectionPO requireEnabledSection(Long courseId, Long sectionId) {
        CourseSectionPO section = sectionId == null ? null : courseSectionMapper.selectById(sectionId);
        if (section == null
                || !Objects.equals(section.getCourseId(), courseId)
                || section.getStatus() != EnableStatus.ENABLED) {
            throw new BizException(LearningErrorCode.LEARNING_SECTION_NOT_FOUND);
        }
        return section;
    }

    private LearningCoursePO requireJoinedCourse(Long userId, Long courseId) {
        LearningCoursePO learningCourse = getJoinedCourse(userId, courseId);
        if (learningCourse == null) {
            throw new BizException(LearningErrorCode.LEARNING_COURSE_NOT_JOINED);
        }
        return learningCourse;
    }

    private LearningCoursePO getJoinedCourse(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            return null;
        }
        return learningCourseMapper.selectOne(Wrappers.<LearningCoursePO>lambdaQuery()
                .eq(LearningCoursePO::getUserId, userId)
                .eq(LearningCoursePO::getCourseId, courseId));
    }

    private LearningRecordPO getLearningRecord(Long userId, Long sectionId) {
        return learningRecordMapper.selectOne(Wrappers.<LearningRecordPO>lambdaQuery()
                .eq(LearningRecordPO::getUserId, userId)
                .eq(LearningRecordPO::getSectionId, sectionId));
    }

    private LearningRecordPO upsertLearningRecord(
            Long userId,
            LearningHeartbeatRequest request,
            LearningRecordPO existed,
            int duration) {
        int oldPosition = safeInt(existed == null ? null : existed.getLastPosition());
        int oldLearnedSeconds = safeInt(existed == null ? null : existed.getLearnedSeconds());
        int newPosition = Math.max(oldPosition, request.getPosition());
        int deltaSeconds = Math.min(
                Math.max(0, newPosition - oldPosition),
                LearningConstants.MAX_HEARTBEAT_SECONDS);
        LearningFinishedStatus finished = resolveFinishedStatus(existed, newPosition, duration);

        if (existed == null) {
            LearningRecordPO record = new LearningRecordPO();
            record.setUserId(userId);
            record.setCourseId(request.getCourseId());
            record.setSectionId(request.getSectionId());
            record.setLastPosition(newPosition);
            record.setLearnedSeconds(deltaSeconds);
            record.setDurationSeconds(duration);
            record.setFinished(finished);
            record.setLastHeartbeatTime(LocalDateTime.now());
            record.setDeleted(0);
            learningRecordMapper.insert(record);
            return record;
        }

        existed.setLastPosition(newPosition);
        existed.setLearnedSeconds(oldLearnedSeconds + deltaSeconds);
        existed.setDurationSeconds(Math.max(safeInt(existed.getDurationSeconds()), duration));
        existed.setFinished(finished);
        existed.setLastHeartbeatTime(LocalDateTime.now());
        learningRecordMapper.updateById(existed);
        return existed;
    }

    private LearningFinishedStatus resolveFinishedStatus(LearningRecordPO existed, int position, int duration) {
        if (existed != null && existed.getFinished() == LearningFinishedStatus.FINISHED) {
            return LearningFinishedStatus.FINISHED;
        }
        boolean finished = duration > 0
                && position >= (int) Math.ceil(duration * LearningConstants.SECTION_FINISH_THRESHOLD);
        return LearningFinishedStatus.fromBoolean(finished);
    }

    private void refreshCourseProgress(LearningCoursePO learningCourse, Long lastSectionId) {
        Long totalSectionCount = courseSectionMapper.selectCount(Wrappers.<CourseSectionPO>lambdaQuery()
                .eq(CourseSectionPO::getCourseId, learningCourse.getCourseId())
                .eq(CourseSectionPO::getStatus, EnableStatus.ENABLED));
        List<LearningRecordPO> records = learningRecordMapper.selectList(Wrappers.<LearningRecordPO>lambdaQuery()
                .eq(LearningRecordPO::getUserId, learningCourse.getUserId())
                .eq(LearningRecordPO::getCourseId, learningCourse.getCourseId()));
        long completedSectionCount = records.stream()
                .filter(record -> record.getFinished() == LearningFinishedStatus.FINISHED)
                .count();
        int learnedSeconds = records.stream()
                .map(LearningRecordPO::getLearnedSeconds)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        // 没有目录的小课程不能误判为完成，等待 day05 目录数据补齐后再计算进度。
        learningCourse.setProgressPercent(calculateProgress(completedSectionCount, totalSectionCount));
        learningCourse.setLearnedSeconds(learnedSeconds);
        learningCourse.setLastSectionId(lastSectionId);
        learningCourse.setLastLearnTime(LocalDateTime.now());
        learningCourse.setFinished(LearningFinishedStatus.fromBoolean(
                totalSectionCount != null && totalSectionCount > 0 && completedSectionCount >= totalSectionCount));
        learningCourseMapper.updateById(learningCourse);
    }

    private BigDecimal calculateProgress(long completedSectionCount, Long totalSectionCount) {
        if (totalSectionCount == null || totalSectionCount <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completedSectionCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSectionCount), 2, RoundingMode.HALF_UP);
    }

    private void writeBehaviorLog(
            Long userId,
            Long courseId,
            Long sectionId,
            LearningBehaviorType eventType,
            Integer position,
            Integer deltaSeconds) {
        LearningBehaviorLogPO log = new LearningBehaviorLogPO();
        log.setUserId(userId);
        log.setCourseId(courseId);
        log.setSectionId(sectionId);
        log.setEventType(eventType);
        log.setPositionSecond(position);
        log.setExtra("{\"deltaSeconds\":" + safeInt(deltaSeconds) + "}");
        log.setCreateTime(LocalDateTime.now());
        learningBehaviorLogMapper.insert(log);

        // 学习行为是后续统计、推荐和积分的共同来源，当前只发布轻量 ID 和状态字段。
        businessEventPublisher.publishLearningBehaviorCreated(
                log.getId(), userId, courseId, sectionId, eventType, position, safeInt(deltaSeconds));
    }

    private List<LearningCourseItem> toLearningCourseItems(List<LearningCoursePO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> courseIds = records.stream()
                .map(LearningCoursePO::getCourseId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> sectionIds = records.stream()
                .map(LearningCoursePO::getLastSectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, CoursePO> courseMap = mapCourses(courseIds);
        Map<Long, CourseSectionPO> sectionMap = mapSections(sectionIds);

        return records.stream()
                .map(record -> {
                    CoursePO course = courseMap.get(record.getCourseId());
                    CourseSectionPO section = sectionMap.get(record.getLastSectionId());
                    return LearningCourseItem.builder()
                            .courseId(record.getCourseId())
                            .courseName(course == null ? "未知课程" : course.getTitle())
                            .coverUrl(course == null ? null : course.getCoverUrl())
                            .progressPercent(record.getProgressPercent())
                            .learnedSeconds(safeInt(record.getLearnedSeconds()))
                            .lastVideoId(record.getLastSectionId())
                            .lastSectionTitle(section == null ? null : section.getTitle())
                            .lastLearnTime(record.getLastLearnTime())
                            .status(record.getFinished() == LearningFinishedStatus.FINISHED
                                    ? LearningCourseStatus.COMPLETED
                                    : LearningCourseStatus.LEARNING)
                            .build();
                })
                .toList();
    }

    private List<LearningHistoryItem> toHistoryItems(List<LearningRecordPO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> courseIds = records.stream()
                .map(LearningRecordPO::getCourseId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> sectionIds = records.stream()
                .map(LearningRecordPO::getSectionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, CoursePO> courseMap = mapCourses(courseIds);
        Map<Long, CourseSectionPO> sectionMap = mapSections(sectionIds);

        return records.stream()
                .map(record -> {
                    CoursePO course = courseMap.get(record.getCourseId());
                    CourseSectionPO section = sectionMap.get(record.getSectionId());
                    LocalDateTime time = record.getLastHeartbeatTime() == null
                            ? record.getCreateTime()
                            : record.getLastHeartbeatTime();
                    return LearningHistoryItem.builder()
                            .id(record.getId())
                            .courseId(record.getCourseId())
                            .courseName(course == null ? "未知课程" : course.getTitle())
                            .videoId(record.getSectionId())
                            .videoTitle(section == null ? null : section.getTitle())
                            .sectionId(record.getSectionId())
                            .sectionName(section == null ? null : section.getTitle())
                            .learnedSeconds(safeInt(record.getLearnedSeconds()))
                            .lastPosition(safeInt(record.getLastPosition()))
                            .durationSeconds(safeInt(record.getDurationSeconds()))
                            .createTime(time)
                            .build();
                })
                .toList();
    }

    private LearningRecordVO toRecordVO(LearningRecordPO record) {
        return LearningRecordVO.builder()
                .courseId(record.getCourseId())
                .sectionId(record.getSectionId())
                .videoId(record.getSectionId())
                .lastPlayTime(safeInt(record.getLastPosition()))
                .maxHistoryTime(safeInt(record.getLastPosition()))
                .completed(record.getFinished() == LearningFinishedStatus.FINISHED)
                .position(safeInt(record.getLastPosition()))
                .duration(safeInt(record.getDurationSeconds()))
                .build();
    }

    private Map<Long, CoursePO> mapCourses(Set<Long> courseIds) {
        if (CollectionUtils.isEmpty(courseIds)) {
            return Collections.emptyMap();
        }
        return courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(CoursePO::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, CourseSectionPO> mapSections(Set<Long> sectionIds) {
        if (CollectionUtils.isEmpty(sectionIds)) {
            return Collections.emptyMap();
        }
        return courseSectionMapper.selectBatchIds(sectionIds).stream()
                .collect(Collectors.toMap(CourseSectionPO::getId, Function.identity(), (left, right) -> left));
    }

    private Set<Long> collectCourseIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return courseMapper.selectList(Wrappers.<CoursePO>lambdaQuery()
                        .like(CoursePO::getTitle, keyword.trim()))
                .stream()
                .map(CoursePO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private HistoryKeywordMatch collectHistoryKeywordMatch(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String trimmed = keyword.trim();
        Set<Long> courseIds = courseMapper.selectList(Wrappers.<CoursePO>lambdaQuery()
                        .like(CoursePO::getTitle, trimmed))
                .stream()
                .map(CoursePO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> sectionIds = courseSectionMapper.selectList(Wrappers.<CourseSectionPO>lambdaQuery()
                        .like(CourseSectionPO::getTitle, trimmed))
                .stream()
                .map(CourseSectionPO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new HistoryKeywordMatch(courseIds, sectionIds);
    }

    private void applyCourseSort(LambdaQueryWrapper<LearningCoursePO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("createTime".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, LearningCoursePO::getCreateTime);
        } else if ("progressPercent".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, LearningCoursePO::getProgressPercent);
        } else {
            wrapper.orderBy(true, asc, LearningCoursePO::getLastLearnTime);
        }
        wrapper.orderByDesc(LearningCoursePO::getId);
    }

    private void applyHistorySort(LambdaQueryWrapper<LearningRecordPO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("createTime".equalsIgnoreCase(sortBy) || "lastHeartbeatTime".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, LearningRecordPO::getLastHeartbeatTime);
        } else if ("learnedSeconds".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, LearningRecordPO::getLearnedSeconds);
        } else {
            wrapper.orderByDesc(LearningRecordPO::getLastHeartbeatTime);
        }
        wrapper.orderByDesc(LearningRecordPO::getId);
    }

    private int resolveDuration(Integer requestDuration, Integer sectionDuration) {
        return Math.max(safeInt(requestDuration), safeInt(sectionDuration));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int toMinutes(int seconds) {
        return seconds <= 0 ? 0 : seconds / 60;
    }

    private int toInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private record HistoryKeywordMatch(Set<Long> courseIds, Set<Long> sectionIds) {

        private boolean isEmpty() {
            return courseIds.isEmpty() && sectionIds.isEmpty();
        }
    }
}
