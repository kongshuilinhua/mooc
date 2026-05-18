package com.elysia.mooc.studyarchive.domain.dto;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.Data;

/** 学习日报查询参数。 */
@Data
public class DailyReportQuery {

    /** 单日报日期，优先使用该字段。 */
    @PastOrPresent(message = "学习报告日期不能晚于今天")
    private LocalDate bizDate;

    /** 兼容文档中的日期范围起始日期。 */
    @PastOrPresent(message = "开始日期不能晚于今天")
    private LocalDate startDate;

    /** 兼容文档中的日期范围结束日期，单日报接口会取结束日期作为查询日期。 */
    @PastOrPresent(message = "结束日期不能晚于今天")
    private LocalDate endDate;
}
