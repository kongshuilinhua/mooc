package com.elysia.mooc.exam.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.exam.domain.dto.CreatePaperRequest;
import com.elysia.mooc.exam.domain.dto.ExamPaperQuery;
import com.elysia.mooc.exam.domain.vo.PaperVO;

/** 试卷服务。 */
public interface PaperService {

    /**
     * 分页查询试卷。
     *
     * @param query 查询条件
     * @return 试卷分页
     */
    PageResult<PaperVO> listPapers(ExamPaperQuery query);

    /**
     * 创建试卷。
     *
     * @param request 创建试卷请求
     * @return 创建后的试卷
     */
    PaperVO createPaper(CreatePaperRequest request);
}
