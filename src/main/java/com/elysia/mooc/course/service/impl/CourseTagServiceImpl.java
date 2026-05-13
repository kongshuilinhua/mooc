package com.elysia.mooc.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.domain.dto.CourseTagQuery;
import com.elysia.mooc.course.domain.po.CourseTagPO;
import com.elysia.mooc.course.domain.vo.CourseTagVO;
import com.elysia.mooc.course.mapper.CourseTagMapper;
import com.elysia.mooc.course.service.CourseTagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 课程标签服务实现。 */
@Service
@RequiredArgsConstructor
public class CourseTagServiceImpl implements CourseTagService {

    private final CourseTagMapper courseTagMapper;

    /**
     * 查询启用标签列表。
     *
     * @param query 查询条件
     * @return 标签列表
     */
    @Override
    public List<CourseTagVO> listTags(CourseTagQuery query) {
        // 1. 标签选择器只暴露启用标签，并支持轻量关键词过滤。
        LambdaQueryWrapper<CourseTagPO> wrapper = Wrappers.<CourseTagPO>lambdaQuery()
                .eq(CourseTagPO::getStatus, EnableStatus.ENABLED);
        if (query != null && StringUtils.hasText(query.getKeyword())) {
            wrapper.like(CourseTagPO::getName, query.getKeyword().trim());
        }
        wrapper.orderByDesc(CourseTagPO::getUseCount).orderByAsc(CourseTagPO::getId);
        return BeanCopyUtils.copyList(courseTagMapper.selectList(wrapper), CourseTagVO.class);
    }
}
