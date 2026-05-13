package com.elysia.mooc.course.domain.dto;

import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.validate.EnumValid;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 修改小节请求参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateSectionRequest extends CreateSectionRequest {

    /** 小节启停状态。 */
    @EnumValid(enumClass = EnableStatus.class, message = "小节状态只能是0或1")
    private EnableStatus status;

    @Override
    public void check() {
        super.check();
        if (status == null) {
            status = EnableStatus.ENABLED;
        }
    }
}
