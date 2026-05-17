package com.elysia.mooc.recommend.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.recommend.domain.dto.CourseRecommendQuery;
import com.elysia.mooc.recommend.domain.dto.HotConceptQuery;
import com.elysia.mooc.recommend.domain.vo.HotConceptVO;
import com.elysia.mooc.recommend.domain.vo.HotCourseVO;
import com.elysia.mooc.recommend.domain.vo.RecommendedCourseVO;
import com.elysia.mooc.recommend.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 推荐与热门内容接口。 */
@Tag(name = "推荐与热门内容")
@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 查询推荐课程。
     *
     * @param query 查询参数
     * @return 推荐课程分页
     */
    @Operation(summary = "查询推荐课程")
    @GetMapping("/courses/recommendations")
    public ApiResult<PageResult<RecommendedCourseVO>> listRecommendations(@Valid CourseRecommendQuery query) {
        return ApiResult.ok(recommendService.listRecommendations(query));
    }

    /**
     * 查询热门课程。
     *
     * @param query 查询参数
     * @return 热门课程分页
     */
    @Operation(summary = "查询热门课程")
    @GetMapping("/courses/hot")
    public ApiResult<PageResult<HotCourseVO>> listHotCourses(@Valid CourseRecommendQuery query) {
        return ApiResult.ok(recommendService.listHotCourses(query));
    }

    /**
     * 查询热门知识点。
     *
     * @param query 查询参数
     * @return 热门知识点分页
     */
    @Operation(summary = "查询热门知识点")
    @GetMapping("/concepts/hot")
    public ApiResult<PageResult<HotConceptVO>> listHotConcepts(@Valid HotConceptQuery query) {
        return ApiResult.ok(recommendService.listHotConcepts(query));
    }
}
