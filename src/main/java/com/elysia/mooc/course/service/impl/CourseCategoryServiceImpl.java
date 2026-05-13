package com.elysia.mooc.course.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.common.utils.TreeUtils;
import com.elysia.mooc.course.domain.po.CourseCategoryPO;
import com.elysia.mooc.course.domain.vo.CourseCategoryVO;
import com.elysia.mooc.course.mapper.CourseCategoryMapper;
import com.elysia.mooc.course.service.CourseCategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 课程分类服务实现。 */
@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl implements CourseCategoryService {

    private static final Long ROOT_PARENT_ID = 0L;

    private final CourseCategoryMapper courseCategoryMapper;

    /**
     * 查询启用分类树。
     *
     * @return 分类树
     */
    @Override
    public List<CourseCategoryVO> listCategoryTree() {
        // 1. 匿名接口只返回启用分类，防止禁用分类继续参与课程筛选。
        List<CourseCategoryPO> categories = courseCategoryMapper.selectList(
                Wrappers.<CourseCategoryPO>lambdaQuery()
                        .eq(CourseCategoryPO::getStatus, EnableStatus.ENABLED)
                        .orderByAsc(CourseCategoryPO::getParentId)
                        .orderByAsc(CourseCategoryPO::getSort)
                        .orderByAsc(CourseCategoryPO::getId));
        List<CourseCategoryVO> voList = BeanCopyUtils.copyList(categories, CourseCategoryVO.class);

        // 2. 按固定根节点 0 组装树，确保接口返回结构稳定。
        return TreeUtils.buildTree(
                voList,
                ROOT_PARENT_ID,
                CourseCategoryVO::getId,
                CourseCategoryVO::getParentId,
                CourseCategoryVO::setChildren);
    }
}
