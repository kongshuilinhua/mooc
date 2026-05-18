package com.elysia.mooc.studyarchive.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.studyarchive.domain.dto.CreateLearningNoteRequest;
import com.elysia.mooc.studyarchive.domain.dto.DailyReportQuery;
import com.elysia.mooc.studyarchive.domain.dto.WrongBookQuery;
import com.elysia.mooc.studyarchive.domain.vo.LearningNoteVO;
import com.elysia.mooc.studyarchive.domain.vo.LearningReportVO;
import com.elysia.mooc.studyarchive.domain.vo.WrongBookItemVO;
import com.elysia.mooc.studyarchive.service.StudyArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 学习档案接口。 */
@Tag(name = "学习档案")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-archive")
public class StudyArchiveController {

    private final StudyArchiveService studyArchiveService;

    /**
     * 保存学习笔记。
     *
     * @param request 学习笔记请求
     * @return 保存后的笔记信息
     */
    @Operation(summary = "保存学习笔记")
    @PostMapping("/notes")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<LearningNoteVO> saveNote(@Valid @RequestBody CreateLearningNoteRequest request) {
        return ApiResult.ok(studyArchiveService.saveNote(request));
    }

    /**
     * 查询当前学生错题本。
     *
     * @param query 错题本查询条件
     * @return 错题本分页
     */
    @Operation(summary = "查询错题本")
    @GetMapping("/wrong-book")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<PageResult<WrongBookItemVO>> listWrongBook(@Valid WrongBookQuery query) {
        return ApiResult.ok(studyArchiveService.listWrongBook(query));
    }

    /**
     * 查询当前学生学习日报。
     *
     * @param query 日报查询条件
     * @return 学习日报
     */
    @Operation(summary = "查询学习日报")
    @GetMapping("/reports/daily")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<LearningReportVO> getDailyReport(@Valid DailyReportQuery query) {
        return ApiResult.ok(studyArchiveService.getDailyReport(query));
    }
}
