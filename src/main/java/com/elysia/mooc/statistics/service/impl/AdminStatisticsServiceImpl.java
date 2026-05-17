package com.elysia.mooc.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import com.elysia.mooc.auth.mapper.SysUserMapper;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.learning.domain.enums.LearningBehaviorType;
import com.elysia.mooc.learning.domain.po.LearningBehaviorLogPO;
import com.elysia.mooc.learning.domain.po.LearningRecordPO;
import com.elysia.mooc.learning.mapper.LearningBehaviorLogMapper;
import com.elysia.mooc.learning.mapper.LearningRecordMapper;
import com.elysia.mooc.statistics.domain.dto.DailyStatsQuery;
import com.elysia.mooc.statistics.domain.po.AdminDailyStatPO;
import com.elysia.mooc.statistics.domain.vo.AdminOverviewVO;
import com.elysia.mooc.statistics.domain.vo.DailyStatsVO;
import com.elysia.mooc.statistics.mapper.AdminDailyStatMapper;
import com.elysia.mooc.statistics.service.AdminStatisticsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 管理端数据统计服务实现。 */
@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final AdminDailyStatMapper adminDailyStatMapper;
    private final SysUserMapper sysUserMapper;
    private final CourseMapper courseMapper;
    private final LearningRecordMapper learningRecordMapper;
    private final LearningBehaviorLogMapper learningBehaviorLogMapper;
    private final AiMessageMapper aiMessageMapper;

    /**
     * 查询后台概览。
     *
     * @return 后台概览
     */
    @Override
    public AdminOverviewVO getOverview() {
        AdminDailyStatPO todayStat = adminDailyStatMapper.selectOne(Wrappers.<AdminDailyStatPO>lambdaQuery()
                .eq(AdminDailyStatPO::getStatDate, LocalDate.now())
                .last("LIMIT 1"));
        AdminOverviewVO vo = new AdminOverviewVO();
        vo.setUserCount(countUsers());
        vo.setCourseCount(countPublishedCourses());
        if (todayStat != null) {
            fillOverviewByDailyStat(vo, todayStat);
            return vo;
        }

        // 统计预计算缺失时只做轻量聚合兜底，保证管理端看板不会因为定时任务缺口不可用。
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        vo.setNewUserCount(toInt(sysUserMapper.selectCount(Wrappers.<SysUserPO>lambdaQuery()
                .ge(SysUserPO::getCreateTime, start)
                .le(SysUserPO::getCreateTime, end))));
        vo.setActiveUserCount(toInt(learningBehaviorLogMapper.selectCount(Wrappers.<LearningBehaviorLogPO>lambdaQuery()
                .ge(LearningBehaviorLogPO::getCreateTime, start)
                .le(LearningBehaviorLogPO::getCreateTime, end))));
        vo.setCourseViewCount(toInt(learningBehaviorLogMapper.selectCount(Wrappers.<LearningBehaviorLogPO>lambdaQuery()
                .eq(LearningBehaviorLogPO::getEventType, LearningBehaviorType.VIEW)
                .ge(LearningBehaviorLogPO::getCreateTime, start)
                .le(LearningBehaviorLogPO::getCreateTime, end))));
        vo.setVideoPlayCount(toInt(learningBehaviorLogMapper.selectCount(Wrappers.<LearningBehaviorLogPO>lambdaQuery()
                .in(LearningBehaviorLogPO::getEventType, LearningBehaviorType.PLAY, LearningBehaviorType.HEARTBEAT)
                .ge(LearningBehaviorLogPO::getCreateTime, start)
                .le(LearningBehaviorLogPO::getCreateTime, end))));
        Long learnSeconds = learningRecordMapper.selectList(Wrappers.<LearningRecordPO>lambdaQuery()
                        .ge(LearningRecordPO::getLastHeartbeatTime, start)
                        .le(LearningRecordPO::getLastHeartbeatTime, end))
                .stream()
                .map(LearningRecordPO::getLearnedSeconds)
                .filter(value -> value != null && value > 0)
                .mapToLong(Integer::longValue)
                .sum();
        vo.setLearnSeconds(learnSeconds);
        vo.setLearningMinutes(toMinutes(learnSeconds));
        vo.setAiRequestCount(toInt(aiMessageMapper.selectCount(Wrappers.<AiMessagePO>lambdaQuery()
                .eq(AiMessagePO::getRole, AiMessageRole.USER)
                .ge(AiMessagePO::getCreateTime, start)
                .le(AiMessagePO::getCreateTime, end))));
        return vo;
    }

    /**
     * 分页查询每日统计。
     *
     * @param query 查询参数
     * @return 每日统计分页
     */
    @Override
    public PageResult<DailyStatsVO> listDailyStats(DailyStatsQuery query) {
        DailyStatsQuery safeQuery = query == null ? new DailyStatsQuery() : query;
        if (safeQuery.getStartDate() != null
                && safeQuery.getEndDate() != null
                && safeQuery.getStartDate().isAfter(safeQuery.getEndDate())) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "开始日期不能晚于结束日期");
        }

        LambdaQueryWrapper<AdminDailyStatPO> wrapper = Wrappers.<AdminDailyStatPO>lambdaQuery();
        if (safeQuery.getStartDate() != null) {
            wrapper.ge(AdminDailyStatPO::getStatDate, safeQuery.getStartDate());
        }
        if (safeQuery.getEndDate() != null) {
            wrapper.le(AdminDailyStatPO::getStatDate, safeQuery.getEndDate());
        }
        wrapper.orderByDesc(AdminDailyStatPO::getStatDate).orderByDesc(AdminDailyStatPO::getId);

        Page<AdminDailyStatPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<AdminDailyStatPO> result = adminDailyStatMapper.selectPage(page, wrapper);
        List<DailyStatsVO> records = result.getRecords().stream()
                .map(this::toDailyStatsVO)
                .toList();
        return PageResult.of(result, records);
    }

    private void fillOverviewByDailyStat(AdminOverviewVO vo, AdminDailyStatPO stat) {
        vo.setNewUserCount(safeInt(stat.getNewUserCount()));
        vo.setActiveUserCount(safeInt(stat.getActiveUserCount()));
        vo.setCourseViewCount(safeInt(stat.getCourseViewCount()));
        vo.setVideoPlayCount(safeInt(stat.getVideoPlayCount()));
        vo.setLearnSeconds(safeLong(stat.getLearnSeconds()));
        vo.setLearningMinutes(toMinutes(stat.getLearnSeconds()));
        vo.setAiRequestCount(safeInt(stat.getAiCallCount()));
    }

    private DailyStatsVO toDailyStatsVO(AdminDailyStatPO stat) {
        return BeanCopyUtils.copyBean(stat, DailyStatsVO.class, (source, target) -> {
            target.setLearnSeconds(safeLong(source.getLearnSeconds()));
            target.setLearningMinutes(toMinutes(source.getLearnSeconds()));
        });
    }

    private Long countUsers() {
        return sysUserMapper.selectCount(Wrappers.<SysUserPO>lambdaQuery()
                .eq(SysUserPO::getStatus, EnableStatus.ENABLED)
                .eq(SysUserPO::getDeleted, SysUserPO.DELETED_NO));
    }

    private Long countPublishedCourses() {
        return courseMapper.selectCount(Wrappers.<CoursePO>lambdaQuery()
                .eq(CoursePO::getStatus, CourseStatus.PUBLISHED));
    }

    private Long toMinutes(Long seconds) {
        long value = safeLong(seconds);
        return value <= 0 ? 0L : value / 60L;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int toInt(Long value) {
        if (value == null) {
            return 0;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : value.intValue();
    }
}
