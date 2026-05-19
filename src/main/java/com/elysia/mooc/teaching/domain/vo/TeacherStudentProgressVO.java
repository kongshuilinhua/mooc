package com.elysia.mooc.teaching.domain.vo;

import com.elysia.mooc.teaching.domain.enums.TeacherStudentRiskLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 教师端学员进度响应项。 */
@Data
public class TeacherStudentProgressVO {

    /** 学生用户 ID。 */
    private Long studentId;

    /** 学生昵称或用户名。 */
    private String studentName;

    /** 学习进度百分比。 */
    private BigDecimal progressPercent;

    /** 最近学习时间。 */
    private LocalDateTime lastLearnTime;

    /** 旧前端兼容字段，等同 lastLearnTime。 */
    private LocalDateTime latestLearnTime;

    /** 风险等级。 */
    private TeacherStudentRiskLevel riskLevel;

    /** 风险等级中文说明。 */
    private String riskLevelDesc;

    /** 旧前端兼容字段；当前 V027 SQL 暂无独立统计来源。 */
    private Integer completedSectionCount;
}
