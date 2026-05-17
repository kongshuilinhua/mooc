package com.elysia.mooc.homework.service;

import com.elysia.mooc.homework.domain.dto.GradeHomeworkRequest;
import com.elysia.mooc.homework.domain.dto.PublishHomeworkRequest;
import com.elysia.mooc.homework.domain.dto.SubmitHomeworkRequest;
import com.elysia.mooc.homework.domain.vo.HomeworkAssignmentVO;
import com.elysia.mooc.homework.domain.vo.HomeworkGradeVO;
import com.elysia.mooc.homework.domain.vo.HomeworkSubmissionVO;

/** 作业发布、提交与批改服务。 */
public interface HomeworkService {

    /**
     * 教师发布作业。
     *
     * @param request 发布作业请求
     * @return 作业响应
     */
    HomeworkAssignmentVO publishAssignment(PublishHomeworkRequest request);

    /**
     * 学生提交作业。
     *
     * @param request 提交作业请求
     * @return 提交记录响应
     */
    HomeworkSubmissionVO submitHomework(SubmitHomeworkRequest request);

    /**
     * 教师批改作业。
     *
     * @param submissionId 提交记录 ID
     * @param request 批改请求
     * @return 批改结果响应
     */
    HomeworkGradeVO gradeSubmission(Long submissionId, GradeHomeworkRequest request);
}
