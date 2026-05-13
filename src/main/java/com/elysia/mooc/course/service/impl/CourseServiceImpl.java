package com.elysia.mooc.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.constants.AuthErrorCode;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import com.elysia.mooc.auth.mapper.SysUserMapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.constants.CourseConstants;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.dto.CoursePageQuery;
import com.elysia.mooc.course.domain.dto.CreateCourseRequest;
import com.elysia.mooc.course.domain.dto.UpdateCourseRequest;
import com.elysia.mooc.course.domain.enums.CourseListScope;
import com.elysia.mooc.course.domain.enums.CoursePriceType;
import com.elysia.mooc.course.domain.enums.CourseSort;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CourseCategoryPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseTagPO;
import com.elysia.mooc.course.domain.po.CourseTagRelationPO;
import com.elysia.mooc.course.domain.vo.CourseCardVO;
import com.elysia.mooc.course.domain.vo.CourseDetailVO;
import com.elysia.mooc.course.domain.vo.CourseMutationVO;
import com.elysia.mooc.course.mapper.CourseCategoryMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseTagMapper;
import com.elysia.mooc.course.mapper.CourseTagRelationMapper;
import com.elysia.mooc.course.service.CourseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 课程基础信息服务实现。 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final BigDecimal ZERO_PRICE = BigDecimal.ZERO;

    private final CourseMapper courseMapper;
    private final CourseCategoryMapper courseCategoryMapper;
    private final CourseTagMapper courseTagMapper;
    private final CourseTagRelationMapper courseTagRelationMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 分页查询课程。
     *
     * @param query 查询条件
     * @return 课程分页
     */
    @Override
    public PageResult<CourseCardVO> listCourses(CoursePageQuery query) {
        CoursePageQuery safeQuery = query == null ? new CoursePageQuery() : query;
        CourseListScope scope = safeQuery.getScope() == null ? CourseListScope.PUBLIC : safeQuery.getScope();

        // 1. 先按查询范围做后端鉴权，避免前端通过 scope 越权读取草稿或审核中课程。
        LoginUser loginUser = currentLoginUserOrNull();
        if (scope == CourseListScope.MINE) {
            requireTeacherOrAdmin(loginUser);
        } else if (scope == CourseListScope.ALL) {
            requireAdmin(loginUser);
        }

        Set<Long> categoryIds = collectCategoryIds(safeQuery.getCategoryId());
        Set<Long> courseIdsByTag = collectCourseIdsByTag(safeQuery.getTagId());
        if (safeQuery.getTagId() != null && courseIdsByTag.isEmpty()) {
            return PageResult.empty(0L, 0);
        }

        // 2. 再组装数据库分页条件，公开范围固定只查已发布课程。
        LambdaQueryWrapper<CoursePO> wrapper = Wrappers.<CoursePO>lambdaQuery();
        if (scope == CourseListScope.PUBLIC) {
            wrapper.eq(CoursePO::getStatus, CourseStatus.PUBLISHED);
        } else if (safeQuery.getStatus() != null) {
            wrapper.eq(CoursePO::getStatus, safeQuery.getStatus());
        }
        if (scope == CourseListScope.MINE) {
            wrapper.eq(CoursePO::getTeacherId, loginUser.getUserId());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(CoursePO::getTitle, safeQuery.getKeyword().trim());
        }
        if (!categoryIds.isEmpty()) {
            wrapper.in(CoursePO::getCategoryId, categoryIds);
        }
        if (safeQuery.getDifficulty() != null) {
            wrapper.eq(CoursePO::getDifficulty, safeQuery.getDifficulty());
        }
        if (safeQuery.getPriceType() == CoursePriceType.FREE) {
            wrapper.eq(CoursePO::getPrice, ZERO_PRICE);
        } else if (safeQuery.getPriceType() == CoursePriceType.PAID) {
            wrapper.gt(CoursePO::getPrice, ZERO_PRICE);
        }
        if (!courseIdsByTag.isEmpty()) {
            wrapper.in(CoursePO::getId, courseIdsByTag);
        }
        applySort(wrapper, safeQuery.getSort());

        Page<CoursePO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<CoursePO> result = courseMapper.selectPage(page, wrapper);
        return PageResult.of(result, toCourseCards(result.getRecords()));
    }

    /**
     * 查询课程详情。
     *
     * @param courseId 课程 ID
     * @return 课程详情
     */
    @Override
    public CourseDetailVO getCourseDetail(Long courseId) {
        CoursePO course = getVisibleCourse(courseId);
        CourseEnrichment enrichment = buildEnrichment(List.of(course));
        return toCourseDetail(course, enrichment);
    }

    /**
     * 创建课程。
     *
     * @param request 创建课程请求
     * @return 课程变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseMutationVO createCourse(CreateCourseRequest request) {
        LoginUser loginUser = requireCourseCreator();
        validateCategory(request.getCategoryId());
        validateTags(request.getTagIds());

        // 1. 创建阶段只保存基础资料，课程状态固定从草稿开始。
        CoursePO course = new CoursePO();
        fillCourseBaseInfo(course, request);
        course.setTeacherId(loginUser.getUserId());
        course.setStatus(CourseStatus.DRAFT);
        course.setLearnCount(0);
        course.setFavoriteCount(0);
        course.setRatingScore(ZERO_PRICE);
        course.setDeleted(0);
        courseMapper.insert(course);
        replaceCourseTags(course.getId(), request.getTagIds());
        return toMutationVO(courseMapper.selectById(course.getId()));
    }

    /**
     * 修改课程。
     *
     * @param courseId 课程 ID
     * @param request  修改课程请求
     * @return 课程变更结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseMutationVO updateCourse(Long courseId, UpdateCourseRequest request) {
        LoginUser loginUser = requireTeacherOrAdmin(currentLoginUserOrNull());
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        if (!isAdmin(loginUser) && !Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            throw new BizException(CourseErrorCode.COURSE_FORBIDDEN);
        }
        if (course.getStatus() == CourseStatus.PENDING) {
            throw new BizException(CourseErrorCode.COURSE_STATUS_INVALID, "课程审核中，不能修改");
        }

        validateCategory(request.getCategoryId());
        validateTags(request.getTagIds());

        // 1. 驳回课程重新编辑后回到草稿，其他状态只保存基础资料，不在 day04 做发布流转。
        fillCourseBaseInfo(course, request);
        if (course.getStatus() == CourseStatus.REJECTED) {
            course.setStatus(CourseStatus.DRAFT);
        }
        courseMapper.updateById(course);
        replaceCourseTags(course.getId(), request.getTagIds());
        return toMutationVO(courseMapper.selectById(course.getId()));
    }

    private CoursePO getVisibleCourse(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BizException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            return course;
        }

        LoginUser loginUser = currentLoginUserOrNull();
        if (loginUser == null) {
            throw new BizException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        if (isAdmin(loginUser) || Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            return course;
        }
        if (hasRole(loginUser, CourseConstants.ROLE_TEACHER)) {
            throw new BizException(CourseErrorCode.COURSE_FORBIDDEN, "无权限查看该课程");
        }
        throw new BizException(CourseErrorCode.COURSE_NOT_FOUND);
    }

    private void fillCourseBaseInfo(CoursePO course, CreateCourseRequest request) {
        BeanCopyUtils.copyProperties(request, course, (source, target) ->
                target.setPrice(source.getPrice() == null ? ZERO_PRICE : source.getPrice()));
    }

    private void validateCategory(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new BizException(CourseErrorCode.COURSE_CATEGORY_INVALID);
        }
        CourseCategoryPO category = courseCategoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() != EnableStatus.ENABLED) {
            throw new BizException(CourseErrorCode.COURSE_CATEGORY_INVALID);
        }
    }

    private void validateTags(List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<CourseTagPO> tags = courseTagMapper.selectBatchIds(tagIds);
        if (tags.size() != tagIds.size()
                || tags.stream().anyMatch(tag -> tag.getStatus() != EnableStatus.ENABLED)) {
            throw new BizException(CourseErrorCode.COURSE_TAG_INVALID);
        }
    }

    private Set<Long> collectCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return Collections.emptySet();
        }
        if (categoryId <= 0) {
            throw new BizException(CourseErrorCode.COURSE_PARAM_INVALID, "课程分类ID必须为正数");
        }
        List<CourseCategoryPO> categories = courseCategoryMapper.selectList(
                Wrappers.<CourseCategoryPO>lambdaQuery().eq(CourseCategoryPO::getStatus, EnableStatus.ENABLED));
        Map<Long, List<CourseCategoryPO>> childrenMap = categories.stream()
                .collect(Collectors.groupingBy(CourseCategoryPO::getParentId));
        boolean exists = categories.stream().anyMatch(category -> Objects.equals(category.getId(), categoryId));
        if (!exists) {
            throw new BizException(CourseErrorCode.COURSE_CATEGORY_INVALID);
        }

        Set<Long> result = new LinkedHashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.add(categoryId);
        while (!queue.isEmpty()) {
            Long currentId = queue.removeFirst();
            if (result.add(currentId)) {
                childrenMap.getOrDefault(currentId, Collections.emptyList())
                        .forEach(child -> queue.addLast(child.getId()));
            }
        }
        return result;
    }

    private Set<Long> collectCourseIdsByTag(Long tagId) {
        if (tagId == null) {
            return Collections.emptySet();
        }
        if (tagId <= 0) {
            throw new BizException(CourseErrorCode.COURSE_PARAM_INVALID, "课程标签ID必须为正数");
        }
        CourseTagPO tag = courseTagMapper.selectById(tagId);
        if (tag == null || tag.getStatus() != EnableStatus.ENABLED) {
            throw new BizException(CourseErrorCode.COURSE_TAG_INVALID);
        }
        return courseTagRelationMapper.selectList(Wrappers.<CourseTagRelationPO>lambdaQuery()
                        .eq(CourseTagRelationPO::getTagId, tagId))
                .stream()
                .map(CourseTagRelationPO::getCourseId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void applySort(LambdaQueryWrapper<CoursePO> wrapper, CourseSort sort) {
        CourseSort safeSort = sort == null ? CourseSort.NEWEST : sort;
        if (safeSort == CourseSort.HOT) {
            wrapper.orderByDesc(CoursePO::getLearnCount)
                    .orderByDesc(CoursePO::getRatingScore)
                    .orderByDesc(CoursePO::getUpdateTime)
                    .orderByDesc(CoursePO::getId);
            return;
        }
        if (safeSort == CourseSort.RATING) {
            wrapper.orderByDesc(CoursePO::getRatingScore)
                    .orderByDesc(CoursePO::getUpdateTime)
                    .orderByDesc(CoursePO::getId);
            return;
        }
        wrapper.orderByDesc(CoursePO::getUpdateTime).orderByDesc(CoursePO::getId);
    }

    private List<CourseCardVO> toCourseCards(List<CoursePO> courses) {
        if (CollectionUtils.isEmpty(courses)) {
            return Collections.emptyList();
        }
        CourseEnrichment enrichment = buildEnrichment(courses);
        return courses.stream()
                .map(course -> toCourseCard(course, enrichment))
                .toList();
    }

    private CourseCardVO toCourseCard(CoursePO course, CourseEnrichment enrichment) {
        return BeanCopyUtils.copyBean(course, CourseCardVO.class, (source, target) -> {
            target.setCategoryName(enrichment.categoryNames().get(source.getCategoryId()));
            target.setTagIds(enrichment.courseTagIds().getOrDefault(source.getId(), Collections.emptyList()));
            target.setTagNames(enrichment.courseTagNames().getOrDefault(source.getId(), Collections.emptyList()));
            target.setTeacherName(enrichment.teacherNames().getOrDefault(
                    source.getTeacherId(),
                    CourseConstants.UNKNOWN_TEACHER_NAME));
            target.setPriceType(resolvePriceType(source.getPrice()));
        });
    }

    private CourseDetailVO toCourseDetail(CoursePO course, CourseEnrichment enrichment) {
        return BeanCopyUtils.copyBean(course, CourseDetailVO.class, (source, target) -> {
            target.setCategoryName(enrichment.categoryNames().get(source.getCategoryId()));
            target.setTagIds(enrichment.courseTagIds().getOrDefault(source.getId(), Collections.emptyList()));
            target.setTagNames(enrichment.courseTagNames().getOrDefault(source.getId(), Collections.emptyList()));
            target.setTeacherName(enrichment.teacherNames().getOrDefault(
                    source.getTeacherId(),
                    CourseConstants.UNKNOWN_TEACHER_NAME));
            target.setPriceType(resolvePriceType(source.getPrice()));
        });
    }

    private CourseMutationVO toMutationVO(CoursePO course) {
        return BeanCopyUtils.copyBean(course, CourseMutationVO.class);
    }

    private CoursePriceType resolvePriceType(BigDecimal price) {
        return price == null || price.compareTo(ZERO_PRICE) <= 0 ? CoursePriceType.FREE : CoursePriceType.PAID;
    }

    private CourseEnrichment buildEnrichment(List<CoursePO> courses) {
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

    private void replaceCourseTags(Long courseId, List<Long> tagIds) {
        courseTagRelationMapper.delete(
                Wrappers.<CourseTagRelationPO>lambdaQuery().eq(CourseTagRelationPO::getCourseId, courseId));
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long tagId : tagIds) {
            CourseTagRelationPO relation = new CourseTagRelationPO();
            relation.setCourseId(courseId);
            relation.setTagId(tagId);
            relation.setCreateTime(now);
            courseTagRelationMapper.insert(relation);
        }
    }

    private LoginUser requireCourseCreator() {
        LoginUser loginUser = requireTeacherOrAdmin(currentLoginUserOrNull());
        if (isAdmin(loginUser)) {
            return loginUser;
        }
        if (!hasPermission(loginUser, CourseConstants.PERMISSION_COURSE_PUBLISH)) {
            throw new BizException(CourseErrorCode.COURSE_FORBIDDEN, "没有创建课程权限");
        }
        return loginUser;
    }

    private LoginUser requireTeacherOrAdmin(LoginUser loginUser) {
        if (loginUser == null) {
            throw new BizException(AuthErrorCode.AUTH_LOGIN_REQUIRED);
        }
        if (!isAdmin(loginUser) && !hasRole(loginUser, CourseConstants.ROLE_TEACHER)) {
            throw new BizException(CourseErrorCode.COURSE_FORBIDDEN);
        }
        return loginUser;
    }

    private void requireAdmin(LoginUser loginUser) {
        if (loginUser == null) {
            throw new BizException(AuthErrorCode.AUTH_LOGIN_REQUIRED);
        }
        if (!isAdmin(loginUser)) {
            throw new BizException(CourseErrorCode.COURSE_FORBIDDEN);
        }
    }

    private boolean isAdmin(LoginUser loginUser) {
        return hasRole(loginUser, CourseConstants.ROLE_ADMIN);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        return loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(role -> roleCode.equalsIgnoreCase(role));
    }

    private boolean hasPermission(LoginUser loginUser, String permissionCode) {
        return loginUser != null
                && loginUser.getPermissions() != null
                && loginUser.getPermissions().contains(permissionCode);
    }

    private LoginUser currentLoginUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    private record CourseEnrichment(
            Map<Long, String> categoryNames,
            Map<Long, String> teacherNames,
            Map<Long, List<Long>> courseTagIds,
            Map<Long, List<String>> courseTagNames) {
    }

    private record CourseTagMaps(
            Map<Long, List<Long>> tagIds,
            Map<Long, List<String>> tagNames) {
    }
}
