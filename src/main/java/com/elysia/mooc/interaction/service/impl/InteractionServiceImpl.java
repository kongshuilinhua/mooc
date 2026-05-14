package com.elysia.mooc.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.interaction.constants.InteractionConstants;
import com.elysia.mooc.interaction.constants.InteractionErrorCode;
import com.elysia.mooc.interaction.domain.dto.AcceptAnswerRequest;
import com.elysia.mooc.interaction.domain.dto.CreateAnswerRequest;
import com.elysia.mooc.interaction.domain.dto.CreateQuestionRequest;
import com.elysia.mooc.interaction.domain.dto.CreateRatingRequest;
import com.elysia.mooc.interaction.domain.dto.HandleReportRequest;
import com.elysia.mooc.interaction.domain.dto.LikeRequest;
import com.elysia.mooc.interaction.domain.dto.QuestionQuery;
import com.elysia.mooc.interaction.domain.dto.ReportQuery;
import com.elysia.mooc.interaction.domain.dto.ReportRequest;
import com.elysia.mooc.interaction.domain.enums.AnswerAcceptedStatus;
import com.elysia.mooc.interaction.domain.enums.AnswerStatus;
import com.elysia.mooc.interaction.domain.enums.InteractionDeletedStatus;
import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import com.elysia.mooc.interaction.domain.enums.QuestionStatus;
import com.elysia.mooc.interaction.domain.enums.RatingStatus;
import com.elysia.mooc.interaction.domain.enums.ReportStatus;
import com.elysia.mooc.interaction.domain.po.CourseFavoritePO;
import com.elysia.mooc.interaction.domain.po.CourseRatingPO;
import com.elysia.mooc.interaction.domain.po.InteractionAnswerPO;
import com.elysia.mooc.interaction.domain.po.InteractionLikePO;
import com.elysia.mooc.interaction.domain.po.InteractionQuestionPO;
import com.elysia.mooc.interaction.domain.po.InteractionReportPO;
import com.elysia.mooc.interaction.domain.vo.AnswerItemVO;
import com.elysia.mooc.interaction.domain.vo.InteractionCreateResultVO;
import com.elysia.mooc.interaction.domain.vo.LikeResultVO;
import com.elysia.mooc.interaction.domain.vo.QuestionItemVO;
import com.elysia.mooc.interaction.domain.vo.RatingResultVO;
import com.elysia.mooc.interaction.domain.vo.ReportItemVO;
import com.elysia.mooc.interaction.domain.vo.ReportResultVO;
import com.elysia.mooc.interaction.mapper.CourseFavoriteMapper;
import com.elysia.mooc.interaction.mapper.CourseRatingMapper;
import com.elysia.mooc.interaction.mapper.InteractionAnswerMapper;
import com.elysia.mooc.interaction.mapper.InteractionLikeMapper;
import com.elysia.mooc.interaction.mapper.InteractionQuestionMapper;
import com.elysia.mooc.interaction.mapper.InteractionReportMapper;
import com.elysia.mooc.interaction.service.InteractionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

/** 课程互动服务实现。 */
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final InteractionQuestionMapper questionMapper;
    private final InteractionAnswerMapper answerMapper;
    private final CourseRatingMapper ratingMapper;
    private final CourseFavoriteMapper favoriteMapper;
    private final InteractionLikeMapper likeMapper;
    private final InteractionReportMapper reportMapper;

    /**
     * 分页查询课程问题。
     *
     * @param courseId 课程 ID
     * @param query    查询参数
     * @return 问题分页结果
     */
    @Override
    public PageResult<QuestionItemVO> listQuestions(Long courseId, QuestionQuery query) {
        QuestionQuery safeQuery = query == null ? new QuestionQuery() : query;
        requireCourseVisible(courseId);

        LambdaQueryWrapper<InteractionQuestionPO> wrapper = Wrappers.<InteractionQuestionPO>lambdaQuery()
                .eq(InteractionQuestionPO::getCourseId, courseId)
                .in(InteractionQuestionPO::getStatus, visibleQuestionStatuses());
        if (safeQuery.getStatus() != null) {
            if (!visibleQuestionStatuses().contains(safeQuery.getStatus())) {
                return PageResult.empty(0L, 0);
            }
            wrapper.eq(InteractionQuestionPO::getStatus, safeQuery.getStatus());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(nested -> nested
                    .like(InteractionQuestionPO::getTitle, keyword)
                    .or()
                    .like(InteractionQuestionPO::getContent, keyword));
        }
        applyQuestionSort(wrapper, safeQuery.getSortBy(), safeQuery.getIsAsc());

        Page<InteractionQuestionPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<InteractionQuestionPO> result = questionMapper.selectPage(page, wrapper);
        return PageResult.of(result, toQuestionItems(result.getRecords()));
    }

    /**
     * 创建课程问题。
     *
     * @param courseId 课程 ID
     * @param request  创建问题请求
     * @return 创建结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InteractionCreateResultVO createQuestion(Long courseId, CreateQuestionRequest request) {
        Long userId = userContextService.currentUserId();
        requireCourseVisible(courseId);

        InteractionQuestionPO question = new InteractionQuestionPO();
        question.setCourseId(courseId);
        question.setSectionId(request.getSectionId());
        question.setUserId(userId);
        question.setTitle(trimRequired(request.getTitle(), "问题标题不能为空"));
        question.setContent(trimRequired(request.getContent(), "问题内容不能为空"));
        question.setAnswerCount(0);
        question.setStatus(QuestionStatus.OPEN);
        question.setDeleted(InteractionDeletedStatus.NORMAL.getValue());
        questionMapper.insert(question);

        return InteractionCreateResultVO.builder()
                .id(question.getId())
                .questionId(question.getId())
                .status(question.getStatus().getValue())
                .createTime(question.getCreateTime())
                .build();
    }

    /**
     * 创建问题回答。
     *
     * @param questionId 问题 ID
     * @param request    创建回答请求
     * @return 创建结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InteractionCreateResultVO createAnswer(Long questionId, CreateAnswerRequest request) {
        Long userId = userContextService.currentUserId();
        InteractionQuestionPO question = requireVisibleQuestion(questionId);

        InteractionAnswerPO answer = new InteractionAnswerPO();
        answer.setQuestionId(question.getId());
        answer.setUserId(userId);
        answer.setContent(trimRequired(request.getContent(), "回答内容不能为空"));
        answer.setAccepted(AnswerAcceptedStatus.NOT_ACCEPTED);
        answer.setLikeCount(0);
        answer.setStatus(AnswerStatus.NORMAL);
        answer.setDeleted(InteractionDeletedStatus.NORMAL.getValue());
        answerMapper.insert(answer);

        questionMapper.update(null, Wrappers.<InteractionQuestionPO>lambdaUpdate()
                .eq(InteractionQuestionPO::getId, question.getId())
                .setSql("answer_count = answer_count + 1"));

        return InteractionCreateResultVO.builder()
                .id(answer.getId())
                .answerId(answer.getId())
                .status(answer.getStatus().getValue())
                .createTime(answer.getCreateTime())
                .build();
    }

    /**
     * 采纳回答。
     *
     * @param answerId 回答 ID
     * @param request  采纳请求
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean acceptAnswer(Long answerId, AcceptAnswerRequest request) {
        Long userId = userContextService.currentUserId();
        InteractionAnswerPO answer = requireVisibleAnswer(answerId);
        InteractionQuestionPO question = requireVisibleQuestion(answer.getQuestionId());
        if (request != null && request.getQuestionId() != null && !Objects.equals(request.getQuestionId(), question.getId())) {
            throw new BizException(InteractionErrorCode.INTERACTION_PARAM_INVALID, "问题ID与回答不匹配");
        }
        if (!Objects.equals(question.getUserId(), userId)) {
            throw new BizException(InteractionErrorCode.INTERACTION_FORBIDDEN, "只有提问者可以采纳回答");
        }

        InteractionAnswerPO accepted = answerMapper.selectOne(Wrappers.<InteractionAnswerPO>lambdaQuery()
                .eq(InteractionAnswerPO::getQuestionId, question.getId())
                .eq(InteractionAnswerPO::getAccepted, AnswerAcceptedStatus.ACCEPTED));
        if (accepted != null && !Objects.equals(accepted.getId(), answerId)) {
            throw new BizException(InteractionErrorCode.INTERACTION_STATUS_INVALID, "该问题已有采纳回答");
        }

        answer.setAccepted(AnswerAcceptedStatus.ACCEPTED);
        answerMapper.updateById(answer);
        question.setStatus(QuestionStatus.RESOLVED);
        questionMapper.updateById(question);
        return Boolean.TRUE;
    }

    /**
     * 创建或更新课程评价。
     *
     * @param courseId 课程 ID
     * @param request  评分请求
     * @return 评价结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RatingResultVO rateCourse(Long courseId, CreateRatingRequest request) {
        Long userId = userContextService.currentUserId();
        requireCourseVisible(courseId);
        String comment = resolveRatingComment(request);

        CourseRatingPO rating = ratingMapper.selectOne(Wrappers.<CourseRatingPO>lambdaQuery()
                .eq(CourseRatingPO::getCourseId, courseId)
                .eq(CourseRatingPO::getUserId, userId));
        if (rating == null) {
            rating = new CourseRatingPO();
            rating.setCourseId(courseId);
            rating.setUserId(userId);
            rating.setDeleted(InteractionDeletedStatus.NORMAL.getValue());
            rating.setStatus(RatingStatus.NORMAL);
            rating.setScore(request.getScore());
            rating.setComment(comment);
            try {
                ratingMapper.insert(rating);
            } catch (DuplicateKeyException ignored) {
                rating = ratingMapper.selectOne(Wrappers.<CourseRatingPO>lambdaQuery()
                        .eq(CourseRatingPO::getCourseId, courseId)
                        .eq(CourseRatingPO::getUserId, userId));
            }
        } else {
            rating.setScore(request.getScore());
            rating.setComment(comment);
            rating.setStatus(RatingStatus.NORMAL);
            ratingMapper.updateById(rating);
        }

        BigDecimal ratingScore = refreshRatingScore(courseId);
        return RatingResultVO.builder()
                .id(rating.getId())
                .courseId(courseId)
                .score(rating.getScore())
                .content(rating.getComment())
                .status(rating.getStatus())
                .ratingScore(ratingScore)
                .createTime(rating.getCreateTime())
                .build();
    }

    /**
     * 收藏课程。
     *
     * @param courseId 课程 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean favoriteCourse(Long courseId) {
        Long userId = userContextService.currentUserId();
        requireCourseVisible(courseId);

        CourseFavoritePO existed = favoriteMapper.selectOne(Wrappers.<CourseFavoritePO>lambdaQuery()
                .eq(CourseFavoritePO::getCourseId, courseId)
                .eq(CourseFavoritePO::getUserId, userId)
                .eq(CourseFavoritePO::getDeleted, InteractionDeletedStatus.NORMAL.getValue()));
        if (existed != null) {
            return Boolean.TRUE;
        }

        CourseFavoritePO favorite = new CourseFavoritePO();
        favorite.setCourseId(courseId);
        favorite.setUserId(userId);
        favorite.setCreateTime(LocalDateTime.now());
        favorite.setDeleted(InteractionDeletedStatus.NORMAL.getValue());
        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException ignored) {
            return Boolean.TRUE;
        }
        refreshFavoriteCount(courseId);
        return Boolean.TRUE;
    }

    /**
     * 取消收藏课程。
     *
     * @param courseId 课程 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unfavoriteCourse(Long courseId) {
        Long userId = userContextService.currentUserId();
        CourseFavoritePO existed = favoriteMapper.selectOne(Wrappers.<CourseFavoritePO>lambdaQuery()
                .eq(CourseFavoritePO::getCourseId, courseId)
                .eq(CourseFavoritePO::getUserId, userId)
                .eq(CourseFavoritePO::getDeleted, InteractionDeletedStatus.NORMAL.getValue()));
        if (existed == null) {
            return Boolean.TRUE;
        }
        // 唯一索引包含 deleted，先清理旧的删除态记录，避免多次收藏/取消时 deleted=1 冲突。
        favoriteMapper.delete(Wrappers.<CourseFavoritePO>lambdaQuery()
                .eq(CourseFavoritePO::getCourseId, courseId)
                .eq(CourseFavoritePO::getUserId, userId)
                .eq(CourseFavoritePO::getDeleted, InteractionDeletedStatus.DELETED.getValue()));
        existed.setDeleted(InteractionDeletedStatus.DELETED.getValue());
        favoriteMapper.updateById(existed);
        refreshFavoriteCount(courseId);
        return Boolean.TRUE;
    }

    /**
     * 点赞互动目标。
     *
     * @param request 点赞请求
     * @return 点赞结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResultVO like(LikeRequest request) {
        Long userId = userContextService.currentUserId();
        requireLikeTargetVisible(request.getTargetType(), request.getTargetId());

        InteractionLikePO existed = likeMapper.selectOne(Wrappers.<InteractionLikePO>lambdaQuery()
                .eq(InteractionLikePO::getTargetType, request.getTargetType())
                .eq(InteractionLikePO::getTargetId, request.getTargetId())
                .eq(InteractionLikePO::getUserId, userId)
                .eq(InteractionLikePO::getDeleted, InteractionDeletedStatus.NORMAL.getValue()));
        if (existed == null) {
            InteractionLikePO like = new InteractionLikePO();
            like.setTargetType(request.getTargetType());
            like.setTargetId(request.getTargetId());
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            like.setDeleted(InteractionDeletedStatus.NORMAL.getValue());
            try {
                likeMapper.insert(like);
                refreshTargetLikeCount(request.getTargetType(), request.getTargetId());
            } catch (DuplicateKeyException ignored) {
                // 并发重复点赞按幂等成功处理，之后统一读取当前点赞数。
            }
        }

        return LikeResultVO.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .currentLikeCount(currentLikeCount(request.getTargetType(), request.getTargetId()))
                .liked(Boolean.TRUE)
                .build();
    }

    /**
     * 创建举报。
     *
     * @param request 举报请求
     * @return 举报结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportResultVO report(ReportRequest request) {
        Long userId = userContextService.currentUserId();
        requireLikeTargetVisible(request.getTargetType(), request.getTargetId());

        InteractionReportPO report = new InteractionReportPO();
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReporterId(userId);
        report.setReason(trimRequired(request.getReason(), "举报原因不能为空"));
        report.setDetail(resolveReportDetail(request));
        report.setStatus(ReportStatus.PENDING);
        report.setDeleted(InteractionDeletedStatus.NORMAL.getValue());
        reportMapper.insert(report);

        return ReportResultVO.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .status(report.getStatus())
                .createTime(report.getCreateTime())
                .build();
    }

    /**
     * 管理端分页查询举报。
     *
     * @param query 查询参数
     * @return 举报分页
     */
    @Override
    public PageResult<ReportItemVO> listReports(ReportQuery query) {
        requireInteractionManager();
        ReportQuery safeQuery = query == null ? new ReportQuery() : query;
        LambdaQueryWrapper<InteractionReportPO> wrapper = Wrappers.<InteractionReportPO>lambdaQuery();
        if (safeQuery.getStatus() != null) {
            wrapper.eq(InteractionReportPO::getStatus, safeQuery.getStatus());
        }
        if (safeQuery.getTargetType() != null) {
            wrapper.eq(InteractionReportPO::getTargetType, safeQuery.getTargetType());
        }
        wrapper.orderByDesc(InteractionReportPO::getCreateTime).orderByDesc(InteractionReportPO::getId);

        Page<InteractionReportPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return PageResult.of(reportMapper.selectPage(page, wrapper), this::toReportItem);
    }

    /**
     * 管理端处理举报。
     *
     * @param reportId 举报 ID
     * @param request  处理请求
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleReport(Long reportId, HandleReportRequest request) {
        LoginUser loginUser = requireInteractionManager();
        InteractionReportPO report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BizException(InteractionErrorCode.INTERACTION_REPORT_NOT_FOUND);
        }
        if (request.getStatus() == ReportStatus.PENDING) {
            throw new BizException(InteractionErrorCode.INTERACTION_PARAM_INVALID, "处理结果不能回退为待处理");
        }
        report.setStatus(request.getStatus());
        report.setHandlerId(loginUser.getUserId());
        report.setHandleResult(resolveHandleResult(request));
        reportMapper.updateById(report);
        return Boolean.TRUE;
    }

    private CoursePO requireCourseVisible(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BizException(InteractionErrorCode.INTERACTION_COURSE_NOT_AVAILABLE);
        }
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BizException(InteractionErrorCode.INTERACTION_COURSE_NOT_AVAILABLE);
        }
        return course;
    }

    private InteractionQuestionPO requireVisibleQuestion(Long questionId) {
        if (questionId == null || questionId <= 0) {
            throw new BizException(InteractionErrorCode.INTERACTION_QUESTION_NOT_FOUND);
        }
        InteractionQuestionPO question = questionMapper.selectById(questionId);
        if (question == null || !visibleQuestionStatuses().contains(question.getStatus())) {
            throw new BizException(InteractionErrorCode.INTERACTION_QUESTION_NOT_FOUND);
        }
        return question;
    }

    private InteractionAnswerPO requireVisibleAnswer(Long answerId) {
        if (answerId == null || answerId <= 0) {
            throw new BizException(InteractionErrorCode.INTERACTION_ANSWER_NOT_FOUND);
        }
        InteractionAnswerPO answer = answerMapper.selectById(answerId);
        if (answer == null || answer.getStatus() != AnswerStatus.NORMAL) {
            throw new BizException(InteractionErrorCode.INTERACTION_ANSWER_NOT_FOUND);
        }
        return answer;
    }

    private void requireLikeTargetVisible(InteractionTargetType targetType, Long targetId) {
        if (targetType == InteractionTargetType.QUESTION) {
            requireVisibleQuestion(targetId);
            return;
        }
        if (targetType == InteractionTargetType.ANSWER) {
            requireVisibleAnswer(targetId);
            return;
        }
        if (targetType == InteractionTargetType.COURSE) {
            requireCourseVisible(targetId);
            return;
        }
        throw new BizException(InteractionErrorCode.INTERACTION_PARAM_INVALID, "暂不支持该互动对象类型");
    }

    private LoginUser requireInteractionManager() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (!hasRole(loginUser, InteractionConstants.ROLE_ADMIN)
                && !hasPermission(loginUser, InteractionConstants.PERMISSION_MANAGE)) {
            throw new BizException(InteractionErrorCode.INTERACTION_FORBIDDEN, "无权限管理互动举报");
        }
        return loginUser;
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        return loginUser.getRoles() != null && loginUser.getRoles().contains(roleCode);
    }

    private boolean hasPermission(LoginUser loginUser, String permissionCode) {
        return loginUser.getPermissions() != null && loginUser.getPermissions().contains(permissionCode);
    }

    private BigDecimal refreshRatingScore(Long courseId) {
        List<CourseRatingPO> ratings = ratingMapper.selectList(Wrappers.<CourseRatingPO>lambdaQuery()
                .eq(CourseRatingPO::getCourseId, courseId)
                .eq(CourseRatingPO::getStatus, RatingStatus.NORMAL));
        BigDecimal ratingScore = ratings.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(ratings.stream().mapToInt(CourseRatingPO::getScore).average().orElse(0D))
                        .setScale(2, RoundingMode.HALF_UP);
        courseMapper.update(null, Wrappers.<CoursePO>update()
                .eq("id", courseId)
                .set("rating_score", ratingScore));
        return ratingScore;
    }

    private void refreshFavoriteCount(Long courseId) {
        Long count = favoriteMapper.selectCount(Wrappers.<CourseFavoritePO>lambdaQuery()
                .eq(CourseFavoritePO::getCourseId, courseId)
                .eq(CourseFavoritePO::getDeleted, InteractionDeletedStatus.NORMAL.getValue()));
        courseMapper.update(null, Wrappers.<CoursePO>update()
                .eq("id", courseId)
                .set("favorite_count", toInt(count)));
    }

    private void refreshTargetLikeCount(InteractionTargetType targetType, Long targetId) {
        if (targetType == InteractionTargetType.ANSWER) {
            int count = currentLikeCount(targetType, targetId);
            answerMapper.update(null, Wrappers.<InteractionAnswerPO>lambdaUpdate()
                    .eq(InteractionAnswerPO::getId, targetId)
                    .set(InteractionAnswerPO::getLikeCount, count));
        }
    }

    private int currentLikeCount(InteractionTargetType targetType, Long targetId) {
        Long count = likeMapper.selectCount(Wrappers.<InteractionLikePO>lambdaQuery()
                .eq(InteractionLikePO::getTargetType, targetType)
                .eq(InteractionLikePO::getTargetId, targetId)
                .eq(InteractionLikePO::getDeleted, InteractionDeletedStatus.NORMAL.getValue()));
        return toInt(count);
    }

    private List<QuestionStatus> visibleQuestionStatuses() {
        return List.of(QuestionStatus.OPEN, QuestionStatus.RESOLVED);
    }

    private List<QuestionItemVO> toQuestionItems(List<InteractionQuestionPO> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptyList();
        }
        Set<Long> questionIds = questions.stream()
                .map(InteractionQuestionPO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, List<InteractionAnswerPO>> answersByQuestionId = answerMapper.selectList(Wrappers.<InteractionAnswerPO>lambdaQuery()
                        .in(InteractionAnswerPO::getQuestionId, questionIds)
                        .eq(InteractionAnswerPO::getStatus, AnswerStatus.NORMAL)
                        .orderByDesc(InteractionAnswerPO::getAccepted)
                        .orderByAsc(InteractionAnswerPO::getCreateTime))
                .stream()
                .collect(Collectors.groupingBy(InteractionAnswerPO::getQuestionId));
        Map<Long, Integer> likeCountMap = mapLikeCounts(InteractionTargetType.QUESTION, questionIds);

        return questions.stream()
                .map(question -> toQuestionItem(question, answersByQuestionId.get(question.getId()), likeCountMap.get(question.getId())))
                .toList();
    }

    private QuestionItemVO toQuestionItem(InteractionQuestionPO question, List<InteractionAnswerPO> answers, Integer likeCount) {
        return QuestionItemVO.builder()
                .id(question.getId())
                .courseId(question.getCourseId())
                .sectionId(question.getSectionId())
                .userId(question.getUserId())
                .title(question.getTitle())
                .content(question.getContent())
                .answerCount(question.getAnswerCount())
                .likeCount(likeCount == null ? 0 : likeCount)
                .status(question.getStatus())
                .createTime(question.getCreateTime())
                .updateTime(question.getUpdateTime())
                .answers(toAnswerItems(answers))
                .build();
    }

    private List<AnswerItemVO> toAnswerItems(List<InteractionAnswerPO> answers) {
        if (CollectionUtils.isEmpty(answers)) {
            return Collections.emptyList();
        }
        return answers.stream()
                .map(this::toAnswerItem)
                .toList();
    }

    private AnswerItemVO toAnswerItem(InteractionAnswerPO answer) {
        return AnswerItemVO.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .userId(answer.getUserId())
                .content(answer.getContent())
                .accepted(answer.getAccepted())
                .likeCount(answer.getLikeCount())
                .status(answer.getStatus())
                .createTime(answer.getCreateTime())
                .updateTime(answer.getUpdateTime())
                .build();
    }

    private ReportItemVO toReportItem(InteractionReportPO report) {
        return ReportItemVO.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reporterId(report.getReporterId())
                .reason(report.getReason())
                .detail(report.getDetail())
                .status(report.getStatus())
                .handlerId(report.getHandlerId())
                .handleResult(report.getHandleResult())
                .createTime(report.getCreateTime())
                .updateTime(report.getUpdateTime())
                .build();
    }

    private Map<Long, Integer> mapLikeCounts(InteractionTargetType targetType, Set<Long> targetIds) {
        if (CollectionUtils.isEmpty(targetIds)) {
            return Collections.emptyMap();
        }
        return targetIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> currentLikeCount(targetType, id), (left, right) -> left));
    }

    private void applyQuestionSort(LambdaQueryWrapper<InteractionQuestionPO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("answerCount".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, InteractionQuestionPO::getAnswerCount);
        } else {
            wrapper.orderBy(true, asc, InteractionQuestionPO::getCreateTime);
        }
        wrapper.orderByDesc(InteractionQuestionPO::getId);
    }

    private String resolveRatingComment(CreateRatingRequest request) {
        if (StringUtils.hasText(request.getContent())) {
            return request.getContent().trim();
        }
        if (StringUtils.hasText(request.getComment())) {
            return request.getComment().trim();
        }
        return null;
    }

    private String resolveReportDetail(ReportRequest request) {
        if (StringUtils.hasText(request.getDetail())) {
            return request.getDetail().trim();
        }
        if (StringUtils.hasText(request.getContent())) {
            return request.getContent().trim();
        }
        return null;
    }

    private String resolveHandleResult(HandleReportRequest request) {
        if (StringUtils.hasText(request.getHandleResult())) {
            return request.getHandleResult().trim();
        }
        if (StringUtils.hasText(request.getRemark())) {
            return request.getRemark().trim();
        }
        return request.getStatus().getDesc();
    }

    private String trimRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(InteractionErrorCode.INTERACTION_PARAM_INVALID, message);
        }
        return value.trim();
    }

    private int toInt(Long value) {
        if (value == null) {
            return 0;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : value.intValue();
    }
}
