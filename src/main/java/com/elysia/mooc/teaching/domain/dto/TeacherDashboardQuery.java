package com.elysia.mooc.teaching.domain.dto;

import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/** 教师看板日期范围查询参数。 */
@Data
public class TeacherDashboardQuery {

    /** 统计开始日期，格式 yyyy-MM-dd。 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 统计结束日期，格式 yyyy-MM-dd。 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
