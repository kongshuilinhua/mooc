package com.elysia.mooc.recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import com.elysia.mooc.auth.mapper.SysUserMapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.constants.CourseConstants;
import com.elysia.mooc.course.domain.enums.CoursePriceType;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CourseCategoryPO;
import com.elysia.mooc.course.domain.po.CourseConceptPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseTagPO;
import com.elysia.mooc.course.domain.po.CourseTagRelationPO;
import com.elysia.mooc.course.mapper.CourseCategoryMapper;
import com.elysia.mooc.course.mapper.CourseConceptMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseTagMapper;
import com.elysia.mooc.course.mapper.CourseTagRelationMapper;
import com.elysia.mooc.recommend.constants.RecommendConstants;
import com.elysia.mooc.recommend.domain.dto.CourseRecommendQuery;
import com.elysia.mooc.recommend.domain.dto.HotConceptQuery;
import com.elysia.mooc.recommend.domain.po.UserRecommendSnapshotPO;
import com.elysia.mooc.recommend.domain.vo.HotConceptVO;
import com.elysia.mooc.recommend.domain.vo.HotCourseVO;
import com.elysia.mooc.recommend.domain.vo.RecommendedCourseVO;
import com.elysia.mooc.recommend.mapper.UserRecommendSnapshotMapper;
import com.elysia.mooc.recommend.service.RecommendService;
import com.elysia.mooc.statistics.domain.po.CourseHotStatsPO;
import com.elysia.mooc.statistics.mapper.CourseHotStatsMapper;
import com.elysia.mooc.statistics.service.SearchLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 推荐与热门课程服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private static final BigDecimal ZERO_PRICE = BigDecimal.ZERO;

    private final CourseMapper courseMapper;
    private final CourseCategoryMapper courseCategoryMapper;
    private final CourseTagMapper courseTagMapper;
    private final CourseTagRelationMapper courseTagRelationMapper;
    private final CourseConceptMapper courseConceptMapper;
    private final SysUserMapper sysUserMapper;
    private final CourseHotStatsMapper courseHotStatsMapper;
    private final UserRecommendSnapshotMapper userRecommendSnapshotMapper;
    private final SearchLogService searchLogService;
    private final ObjectMapper objectMapper;

    /**
     * 查询推荐课程。
     *
     * @param query 查询参数
     * @return 推荐课程分页
     */
    @Override
    public PageResult<RecommendedCourseVO> listRecommendations(CourseRecommendQuery query) {
        CourseRecommendQuery safeQuery = query == null ? new CourseRecommendQuery() : query;
        SnapshotMatch snapshotMatch = loadSnapshotMatch(currentUserIdOrNull(), safeQuery);
        PageResult<RecommendedCourseVO> result = snapshotMatch.hasValidSnapshot()
                ? listBySnapshot(snapshotMatch, safeQuery)
                : listHotCoursesAsRecommendations(safeQuery);
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            searchLogService.recordSearch(safeQuery.getKeyword(), toSafeTotal(result.getTotal()));
        }
        return result;
    }

    /**
     * 查询热门课程。
     *
     * @param query 查询参数
     * @return 热门课程分页
     */
    @Override
    public PageResult<HotCourseVO> listHotCourses(CourseRecommendQuery query) {
        CourseRecommendQuery safeQuery = query == null ? new CourseRecommendQuery() : query;
        PageResult<HotCourseVO> result = listHotCoursesInternal(safeQuery);
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            searchLogService.recordSearch(safeQuery.getKeyword(), toSafeTotal(result.getTotal()));
        }
        return result;
    }

    private PageResult<HotCourseVO> listHotCoursesInternal(CourseRecommendQuery query) {
        return hasTodayHotStats()
                ? listHotCoursesByStats(query)
                : listHotCoursesByCourseFields(query);
    }

    /**
     * 查询热门知识点。
     *
     * @param query 查询参数
     * @return 热门知识点分页
     */
    @Override
    public PageResult<HotConceptVO> listHotConcepts(HotConceptQuery query) {
        HotConceptQuery safeQuery = query == null ? new HotConceptQuery() : query;
        Set<Long> publishedCourseIds = collectPublishedCourseIds(safeQuery.getCourseId());
        if (publishedCourseIds.isEmpty()) {
            return PageResult.empty(0L, 0);
        }

        LambdaQueryWrapper<CourseConceptPO> wrapper = Wrappers.<CourseConceptPO>lambdaQuery()
                .in(CourseConceptPO::getCourseId, publishedCourseIds);
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(CourseConceptPO::getTitle, safeQuery.getKeyword().trim());
        }
        wrapper.orderByAsc(CourseConceptPO::getSort).orderByDesc(CourseConceptPO::getId);

        List<CourseConceptPO> concepts = courseConceptMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(concepts)) {
            return PageResult.empty(0L, 0);
        }

        Map<Long, CoursePO> courseMap = mapCourses(publishedCourseIds);
        Map<Long, CourseHotStatsPO> hotStatsMap = mapTodayHotStats(publishedCourseIds);
        List<HotConceptVO> all = concepts.stream()
                .map(concept -> toHotConcept(concept, courseMap.get(concept.getCourseId()), hotStatsMap))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(HotConceptVO::getScore, Comparator.nullsLast(BigDecimal::compareTo))
                        .reversed()
                        .thenComparing(HotConceptVO::getConceptId, Comparator.nullsLast(Long::compareTo)))
                .toList();

        if (StringUtils.hasText(safeQuery.getKeyword())) {
            searchLogService.recordSearch(safeQuery.getKeyword(), all.size());
        }
        return manualPage(all, safeQuery.getPageNo(), safeQuery.getPageSize());
    }

    private PageResult<RecommendedCourseVO> listBySnapshot(SnapshotMatch snapshotMatch, CourseRecommendQuery query) {
        List<Long> filteredIds = snapshotMatch.courseIds().stream()
                .filter(snapshotMatch.visibleCourseIds()::contains)
                .toList();
        if (filteredIds.isEmpty()) {
            return listHotCoursesAsRecommendations(query);
        }
        int pageNo = safePageNo(query.getPageNo());
        int pageSize = safePageSize(query.getPageSize());
        int from = Math.min((pageNo - 1) * pageSize, filteredIds.size());
        int to = Math.min(from + pageSize, filteredIds.size());
        List<Long> pageIds = filteredIds.subList(from, to);

        Map<Long, CoursePO> courseMap = mapCourses(new LinkedHashSet<>(pageIds));
        Map<Long, CourseHotStatsPO> hotStatsMap = mapTodayHotStats(new LinkedHashSet<>(pageIds));
        CourseEnrichment enrichment = buildEnrichment(new ArrayList<>(courseMap.values()));
        List<RecommendedCourseVO> records = pageIds.stream()
                .map(courseMap::get)
                .filter(Objects::nonNull)
                .map(course -> toRecommendedCourse(
                        course,
                        enrichment,
                        hotStatsMap.get(course.getId()),
                        snapshotMatch.reasons().getOrDefault(course.getId(), RecommendConstants.SNAPSHOT_REASON)))
                .toList();
        return PageResult.of((long) filteredIds.size(), pageSize, records);
    }

    private PageResult<RecommendedCourseVO> listHotCoursesAsRecommendations(CourseRecommendQuery query) {
        PageResult<HotCourseVO> hotPage = listHotCoursesInternal(query);
        List<RecommendedCourseVO> records = hotPage.getList().stream()
                .map(hotCourse -> {
                    RecommendedCourseVO vo = BeanCopyUtils.copyBean(hotCourse, RecommendedCourseVO.class);
                    vo.setReason(RecommendConstants.DEFAULT_REASON);
                    return vo;
                })
                .toList();
        return new PageResult<>(hotPage.getTotal(), hotPage.getTotalPage(), records);
    }

    private PageResult<HotCourseVO> listHotCoursesByStats(CourseRecommendQuery query) {
        CourseFilter filter = buildCourseFilter(query);
        if (filter.tagFiltered() && filter.courseIds().isEmpty()) {
            return PageResult.empty(0L, 0);
        }
        Set<Long> visibleCourseIds = collectPublishedCourseIdsByQuery(query, filter);
        if (visibleCourseIds.isEmpty()) {
            return PageResult.empty(0L, 0);
        }

        LambdaQueryWrapper<CourseHotStatsPO> wrapper = Wrappers.<CourseHotStatsPO>lambdaQuery()
                .eq(CourseHotStatsPO::getStatDate, LocalDate.now())
                .in(CourseHotStatsPO::getCourseId, visibleCourseIds);
        applyHotStatsSort(wrapper, query.getSortBy(), query.getIsAsc());

        Page<CourseHotStatsPO> page = new Page<>(query.getPageNo(), query.getPageSize());
        Page<CourseHotStatsPO> result = courseHotStatsMapper.selectPage(page, wrapper);
        List<Long> courseIds = result.getRecords().stream()
                .map(CourseHotStatsPO::getCourseId)
                .toList();
        Map<Long, CoursePO> courseMap = mapCourses(new LinkedHashSet<>(courseIds));
        CourseEnrichment enrichment = buildEnrichment(new ArrayList<>(courseMap.values()));
        List<HotCourseVO> records = result.getRecords().stream()
                .map(stats -> toHotCourse(courseMap.get(stats.getCourseId()), enrichment, stats))
                .filter(Objects::nonNull)
                .toList();
        return PageResult.of(result, records);
    }

    private PageResult<HotCourseVO> listHotCoursesByCourseFields(CourseRecommendQuery query) {
        CourseFilter filter = buildCourseFilter(query);
        if (filter.tagFiltered() && filter.courseIds().isEmpty()) {
            return PageResult.empty(0L, 0);
        }
        LambdaQueryWrapper<CoursePO> wrapper = Wrappers.<CoursePO>lambdaQuery()
                .eq(CoursePO::getStatus, CourseStatus.PUBLISHED);
        applyCourseFilters(wrapper, query, filter);
        applyCourseSort(wrapper, query.getSortBy(), query.getIsAsc());

        Page<CoursePO> page = new Page<>(query.getPageNo(), query.getPageSize());
        Page<CoursePO> result = courseMapper.selectPage(page, wrapper);
        CourseEnrichment enrichment = buildEnrichment(result.getRecords());
        List<HotCourseVO> records = result.getRecords().stream()
                .map(course -> toHotCourse(course, enrichment, null))
                .toList();
        return PageResult.of(result, records);
    }

    private SnapshotMatch loadSnapshotMatch(Long userId, CourseRecommendQuery query) {
        if (userId == null) {
            return SnapshotMatch.empty();
        }
        UserRecommendSnapshotPO snapshot = userRecommendSnapshotMapper.selectOne(
                Wrappers.<UserRecommendSnapshotPO>lambdaQuery()
                        .eq(UserRecommendSnapshotPO::getUserId, userId)
                        .gt(UserRecommendSnapshotPO::getExpireTime, LocalDateTime.now())
                        .orderByDesc(UserRecommendSnapshotPO::getExpireTime)
                        .last("LIMIT 1"));
        if (snapshot == null) {
            return SnapshotMatch.empty();
        }

        List<Long> snapshotCourseIds = parseCourseIds(snapshot.getCourseIds());
        if (snapshotCourseIds.isEmpty()) {
            return SnapshotMatch.empty();
        }
        Map<Long, String> reasons = parseReasons(snapshot.getReasonJson());
        Set<Long> visibleCourseIds = collectVisibleSnapshotCourseIds(snapshotCourseIds, query);
        return new SnapshotMatch(snapshotCourseIds, reasons, visibleCourseIds, true);
    }

    private List<Long> parseCourseIds(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<Long> ids = objectMapper.readValue(json, new TypeReference<>() {
            });
            return ids.stream()
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .limit(RecommendConstants.MAX_SNAPSHOT_COURSE_SIZE)
                    .toList();
        } catch (Exception ex) {
            log.warn("推荐快照课程 JSON 解析失败，已降级热门推荐", ex);
            return Collections.emptyList();
        }
    }

    private Map<Long, String> parseReasons(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new HashMap<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                return Collections.emptyMap();
            }
            for (JsonNode node : root) {
                Long courseId = node.path("courseId").canConvertToLong() ? node.path("courseId").asLong() : null;
                String reason = node.path("reason").asText(null);
                if (courseId != null && StringUtils.hasText(reason)) {
                    result.put(courseId, reason);
                }
            }
        } catch (Exception ex) {
            log.warn("推荐原因 JSON 解析失败，已使用默认推荐原因", ex);
        }
        return result;
    }

    private Set<Long> collectVisibleSnapshotCourseIds(List<Long> snapshotCourseIds, CourseRecommendQuery query) {
        CourseFilter filter = buildCourseFilter(query);
        if (filter.tagFiltered() && filter.courseIds().isEmpty()) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<CoursePO> wrapper = Wrappers.<CoursePO>lambdaQuery()
                .select(CoursePO::getId)
                .eq(CoursePO::getStatus, CourseStatus.PUBLISHED)
                .in(CoursePO::getId, snapshotCourseIds);
        applyCourseFilters(wrapper, query, filter);
        return courseMapper.selectList(wrapper).stream()
                .map(CoursePO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean hasTodayHotStats() {
        return courseHotStatsMapper.selectCount(Wrappers.<CourseHotStatsPO>lambdaQuery()
                .eq(CourseHotStatsPO::getStatDate, LocalDate.now())) > 0;
    }

    private CourseFilter buildCourseFilter(CourseRecommendQuery query) {
        Set<Long> courseIds = collectCourseIdsByTag(query.getTagId());
        return new CourseFilter(query.getTagId() != null, courseIds);
    }

    private void applyCourseFilters(
            LambdaQueryWrapper<CoursePO> wrapper,
            CourseRecommendQuery query,
            CourseFilter filter) {
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(CoursePO::getTitle, query.getKeyword().trim());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(CoursePO::getCategoryId, query.getCategoryId());
        }
        if (!filter.courseIds().isEmpty()) {
            wrapper.in(CoursePO::getId, filter.courseIds());
        }
    }

    private void applyHotStatsSort(
            LambdaQueryWrapper<CourseHotStatsPO> wrapper,
            String sortBy,
            Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("ratingScore".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, CourseHotStatsPO::getRatingScore);
        } else if ("learnCount".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, CourseHotStatsPO::getLearnCount);
        } else if ("favoriteCount".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, CourseHotStatsPO::getFavoriteCount);
        } else {
            wrapper.orderBy(true, asc, CourseHotStatsPO::getHotScore);
        }
        wrapper.orderByDesc(CourseHotStatsPO::getId);
    }

    private void applyCourseSort(LambdaQueryWrapper<CoursePO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("ratingScore".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, CoursePO::getRatingScore);
        } else if ("learnCount".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, CoursePO::getLearnCount);
        } else if ("favoriteCount".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, CoursePO::getFavoriteCount);
        } else if ("createTime".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, CoursePO::getCreateTime);
        } else {
            wrapper.orderByDesc(CoursePO::getLearnCount)
                    .orderByDesc(CoursePO::getRatingScore)
                    .orderByDesc(CoursePO::getFavoriteCount);
        }
        wrapper.orderByDesc(CoursePO::getId);
    }

    private Set<Long> collectCourseIdsByTag(Long tagId) {
        if (tagId == null) {
            return Collections.emptySet();
        }
        if (tagId <= 0) {
            return Collections.emptySet();
        }
        return courseTagRelationMapper.selectList(Wrappers.<CourseTagRelationPO>lambdaQuery()
                        .eq(CourseTagRelationPO::getTagId, tagId))
                .stream()
                .map(CourseTagRelationPO::getCourseId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> collectPublishedCourseIds(Long courseId) {
        LambdaQueryWrapper<CoursePO> wrapper = Wrappers.<CoursePO>lambdaQuery()
                .select(CoursePO::getId)
                .eq(CoursePO::getStatus, CourseStatus.PUBLISHED);
        if (courseId != null) {
            wrapper.eq(CoursePO::getId, courseId);
        }
        return courseMapper.selectList(wrapper).stream()
                .map(CoursePO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> collectPublishedCourseIdsByQuery(CourseRecommendQuery query, CourseFilter filter) {
        LambdaQueryWrapper<CoursePO> wrapper = Wrappers.<CoursePO>lambdaQuery()
                .select(CoursePO::getId)
                .eq(CoursePO::getStatus, CourseStatus.PUBLISHED);
        applyCourseFilters(wrapper, query, filter);
        return courseMapper.selectList(wrapper).stream()
                .map(CoursePO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, CoursePO> mapCourses(Set<Long> courseIds) {
        if (CollectionUtils.isEmpty(courseIds)) {
            return Collections.emptyMap();
        }
        return courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(CoursePO::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, CourseHotStatsPO> mapTodayHotStats(Set<Long> courseIds) {
        if (CollectionUtils.isEmpty(courseIds)) {
            return Collections.emptyMap();
        }
        return courseHotStatsMapper.selectList(Wrappers.<CourseHotStatsPO>lambdaQuery()
                        .eq(CourseHotStatsPO::getStatDate, LocalDate.now())
                        .in(CourseHotStatsPO::getCourseId, courseIds))
                .stream()
                .collect(Collectors.toMap(CourseHotStatsPO::getCourseId, Function.identity(), (left, right) -> left));
    }

    private CourseEnrichment buildEnrichment(List<CoursePO> courses) {
        if (CollectionUtils.isEmpty(courses)) {
            return CourseEnrichment.empty();
        }
        Set<Long> categoryIds = courses.stream()
                .map(CoursePO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> teacherIds = courses.stream()
                .map(CoursePO::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> courseIds = courses.stream()
                .map(CoursePO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, String> categoryNames = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : courseCategoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(
                                CourseCategoryPO::getId,
                                CourseCategoryPO::getName,
                                (left, right) -> left));
        Map<Long, String> teacherNames = buildTeacherNameMap(teacherIds);
        CourseTagMaps tagMaps = buildCourseTagMaps(courseIds);
        return new CourseEnrichment(categoryNames, teacherNames, tagMaps.tagIds(), tagMaps.tagNames());
    }

    private Map<Long, String> buildTeacherNameMap(Set<Long> teacherIds) {
        if (teacherIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(
                        SysUserPO::getId,
                        user -> StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername(),
                        (left, right) -> left));
    }

    private CourseTagMaps buildCourseTagMaps(Set<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return new CourseTagMaps(Collections.emptyMap(), Collections.emptyMap());
        }
        List<CourseTagRelationPO> relations = courseTagRelationMapper.selectList(
                Wrappers.<CourseTagRelationPO>lambdaQuery()
                        .in(CourseTagRelationPO::getCourseId, courseIds)
                        .orderByAsc(CourseTagRelationPO::getId));
        if (relations.isEmpty()) {
            return new CourseTagMaps(Collections.emptyMap(), Collections.emptyMap());
        }

        Set<Long> tagIds = relations.stream()
                .map(CourseTagRelationPO::getTagId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, CourseTagPO> tagMap = courseTagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(CourseTagPO::getId, Function.identity(), (left, right) -> left));
        Map<Long, List<Long>> courseTagIds = new LinkedHashMap<>();
        Map<Long, List<String>> courseTagNames = new LinkedHashMap<>();
        for (CourseTagRelationPO relation : relations) {
            CourseTagPO tag = tagMap.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            courseTagIds.computeIfAbsent(relation.getCourseId(), key -> new ArrayList<>()).add(tag.getId());
            courseTagNames.computeIfAbsent(relation.getCourseId(), key -> new ArrayList<>()).add(tag.getName());
        }
        return new CourseTagMaps(courseTagIds, courseTagNames);
    }

    private HotCourseVO toHotCourse(CoursePO course, CourseEnrichment enrichment, CourseHotStatsPO stats) {
        if (course == null) {
            return null;
        }
        return BeanCopyUtils.copyBean(course, HotCourseVO.class, (source, target) -> {
            target.setCategoryName(enrichment.categoryNames().get(source.getCategoryId()));
            target.setTagIds(enrichment.courseTagIds().getOrDefault(source.getId(), Collections.emptyList()));
            target.setTagNames(enrichment.courseTagNames().getOrDefault(source.getId(), Collections.emptyList()));
            target.setTeacherName(enrichment.teacherNames().getOrDefault(
                    source.getTeacherId(),
                    CourseConstants.UNKNOWN_TEACHER_NAME));
            target.setPriceType(resolvePriceType(source.getPrice()));
            target.setViewCount(stats == null ? 0 : safeInt(stats.getViewCount()));
            target.setLearnCount(stats == null ? safeInt(source.getLearnCount()) : safeInt(stats.getLearnCount()));
            target.setFavoriteCount(stats == null
                    ? safeInt(source.getFavoriteCount())
                    : safeInt(stats.getFavoriteCount()));
            target.setRatingScore(stats == null ? safeDecimal(source.getRatingScore()) : safeDecimal(stats.getRatingScore()));
            target.setHotScore(stats == null ? calculateFallbackHotScore(source) : safeDecimal(stats.getHotScore()));
        });
    }

    private RecommendedCourseVO toRecommendedCourse(
            CoursePO course,
            CourseEnrichment enrichment,
            CourseHotStatsPO stats,
            String reason) {
        return BeanCopyUtils.copyBean(course, RecommendedCourseVO.class, (source, target) -> {
            target.setCategoryName(enrichment.categoryNames().get(source.getCategoryId()));
            target.setTagIds(enrichment.courseTagIds().getOrDefault(source.getId(), Collections.emptyList()));
            target.setTagNames(enrichment.courseTagNames().getOrDefault(source.getId(), Collections.emptyList()));
            target.setTeacherName(enrichment.teacherNames().getOrDefault(
                    source.getTeacherId(),
                    CourseConstants.UNKNOWN_TEACHER_NAME));
            target.setPriceType(resolvePriceType(source.getPrice()));
            target.setHotScore(stats == null ? calculateFallbackHotScore(source) : safeDecimal(stats.getHotScore()));
            target.setReason(StringUtils.hasText(reason) ? reason : RecommendConstants.DEFAULT_REASON);
        });
    }

    private HotConceptVO toHotConcept(
            CourseConceptPO concept,
            CoursePO course,
            Map<Long, CourseHotStatsPO> hotStatsMap) {
        if (course == null) {
            return null;
        }
        CourseHotStatsPO stats = hotStatsMap.get(course.getId());
        BigDecimal score = stats == null ? calculateFallbackHotScore(course) : safeDecimal(stats.getHotScore());
        HotConceptVO vo = new HotConceptVO();
        vo.setConceptId(concept.getId());
        vo.setConceptName(concept.getTitle());
        vo.setCourseId(course.getId());
        vo.setCourseTitle(course.getTitle());
        vo.setScore(score);
        vo.setHitCount(Math.max(RecommendConstants.HOT_CONCEPT_DEFAULT_SCORE, score.intValue()));
        return vo;
    }

    private CoursePriceType resolvePriceType(BigDecimal price) {
        return price == null || price.compareTo(ZERO_PRICE) <= 0 ? CoursePriceType.FREE : CoursePriceType.PAID;
    }

    private BigDecimal calculateFallbackHotScore(CoursePO course) {
        int learn = safeInt(course.getLearnCount());
        int favorite = safeInt(course.getFavoriteCount());
        BigDecimal rating = safeDecimal(course.getRatingScore());
        return BigDecimal.valueOf(learn)
                .multiply(BigDecimal.valueOf(2))
                .add(BigDecimal.valueOf(favorite).multiply(BigDecimal.valueOf(3)))
                .add(rating.multiply(BigDecimal.TEN));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }

    private int safePageNo(Integer pageNo) {
        return pageNo == null || pageNo <= 0 ? 1 : pageNo;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private int toSafeTotal(Long total) {
        if (total == null) {
            return 0;
        }
        return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : total.intValue();
    }

    private <T> PageResult<T> manualPage(List<T> all, Integer pageNo, Integer pageSize) {
        int safePageNo = safePageNo(pageNo);
        int safePageSize = safePageSize(pageSize);
        int total = all == null ? 0 : all.size();
        if (total == 0) {
            return PageResult.empty(0L, 0);
        }
        int from = Math.min((safePageNo - 1) * safePageSize, total);
        int to = Math.min(from + safePageSize, total);
        return PageResult.of((long) total, safePageSize, all.subList(from, to));
    }

    private record SnapshotMatch(
            List<Long> courseIds,
            Map<Long, String> reasons,
            Set<Long> visibleCourseIds,
            boolean hasValidSnapshot) {

        private static SnapshotMatch empty() {
            return new SnapshotMatch(Collections.emptyList(), Collections.emptyMap(), Collections.emptySet(), false);
        }
    }

    private record CourseFilter(boolean tagFiltered, Set<Long> courseIds) {
    }

    private record CourseEnrichment(
            Map<Long, String> categoryNames,
            Map<Long, String> teacherNames,
            Map<Long, List<Long>> courseTagIds,
            Map<Long, List<String>> courseTagNames) {

        private static CourseEnrichment empty() {
            return new CourseEnrichment(
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }
    }

    private record CourseTagMaps(
            Map<Long, List<Long>> tagIds,
            Map<Long, List<String>> tagNames) {
    }
}
