package com.elysia.mooc.studyarchive.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.studyarchive.domain.dto.CreateLearningNoteRequest;
import com.elysia.mooc.studyarchive.domain.dto.DailyReportQuery;
import com.elysia.mooc.studyarchive.domain.dto.WrongBookQuery;
import com.elysia.mooc.studyarchive.domain.vo.LearningNoteVO;
import com.elysia.mooc.studyarchive.domain.vo.LearningReportVO;
import com.elysia.mooc.studyarchive.domain.vo.WrongBookItemVO;

/** 学习档案服务。 */
public interface StudyArchiveService {

    /**
     * 保存当前学生的学习笔记。
     *
     * @param request 笔记保存请求
     * @return 保存后的笔记信息
     */
    LearningNoteVO saveNote(CreateLearningNoteRequest request);

    /**
     * 分页查询当前学生错题本。
     *
     * @param query 错题本查询条件
     * @return 错题本分页
     */
    PageResult<WrongBookItemVO> listWrongBook(WrongBookQuery query);

    /**
     * 查询当前学生学习日报。
     *
     * @param query 日报查询条件
     * @return 学习日报
     */
    LearningReportVO getDailyReport(DailyReportQuery query);
}
