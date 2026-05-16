package com.elysia.mooc.exam.service;

import com.elysia.mooc.exam.domain.dto.SubmitExamRequest;
import com.elysia.mooc.exam.domain.vo.ExamRecordVO;

/** 作答记录服务。 */
public interface ExamRecordService {

    /**
     * 提交试卷作答并自动判分。
     *
     * @param request 提交作答请求
     * @return 作答结果
     */
    ExamRecordVO submit(SubmitExamRequest request);
}
