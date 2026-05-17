package com.elysia.mooc.recommend.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.recommend.domain.dto.CourseRecommendQuery;
import com.elysia.mooc.recommend.domain.dto.HotConceptQuery;
import com.elysia.mooc.recommend.domain.vo.HotConceptVO;
import com.elysia.mooc.recommend.domain.vo.HotCourseVO;
import com.elysia.mooc.recommend.domain.vo.RecommendedCourseVO;

/** 课程推荐与热门内容服务。 */
public interface RecommendService {

    /**
     * 查询推荐课程，登录用户优先读取未过期推荐快照，匿名用户走热门兜底。
     *
     * @param query 查询参数
     * @return 推荐课程分页
     */
    PageResult<RecommendedCourseVO> listRecommendations(CourseRecommendQuery query);

    /**
     * 查询热门课程，优先使用当天热度统计，没有统计时回退课程基础热度字段。
     *
     * @param query 查询参数
     * @return 热门课程分页
     */
    PageResult<HotCourseVO> listHotCourses(CourseRecommendQuery query);

    /**
     * 查询热门知识点，当前按公开课程热度和知识点顺序生成可解释排序。
     *
     * @param query 查询参数
     * @return 热门知识点分页
     */
    PageResult<HotConceptVO> listHotConcepts(HotConceptQuery query);
}
