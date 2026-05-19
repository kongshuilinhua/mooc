package com.elysia.mooc.ops.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 导出文件格式。 */
@Getter
@RequiredArgsConstructor
public enum OpsExportFormat implements BaseEnum<String> {

    /** Excel 文件。 */
    XLSX("XLSX", "Excel"),

    /** CSV 文件。 */
    CSV("CSV", "CSV");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OpsExportFormat of(Object value) {
        return BaseEnum.parse(OpsExportFormat.class, value);
    }
}
