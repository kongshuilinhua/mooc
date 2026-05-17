package com.elysia.mooc.trade.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 交易模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum TradeErrorCode implements ErrorCode {

    /** 交易参数不正确。 */
    TRADE_PARAM_INVALID(21001, "交易参数不正确"),

    /** 课程不存在或暂不可购买。 */
    TRADE_COURSE_NOT_AVAILABLE(21002, "课程不存在或暂不可购买"),

    /** 免费课程不需要创建付费订单。 */
    TRADE_FREE_COURSE(21003, "免费课程不需要支付"),

    /** 订单不存在。 */
    TRADE_ORDER_NOT_FOUND(21004, "订单不存在"),

    /** 无权操作该订单。 */
    TRADE_ORDER_FORBIDDEN(21005, "无权操作该订单"),

    /** 订单状态不允许当前操作。 */
    TRADE_ORDER_STATUS_INVALID(21006, "订单状态不允许当前操作"),

    /** 当前课程已经购买或已加入学习。 */
    TRADE_COURSE_ALREADY_JOINED(21007, "当前课程已经购买或已加入学习"),

    /** 支付记录异常。 */
    TRADE_PAY_RECORD_INVALID(21008, "支付记录异常");

    private final int code;
    private final String message;

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    /**
     * 交易接口按 HTTP 合同表达权限、资源和状态冲突，避免只返回业务码 200。
     *
     * @return HTTP 状态码
     */
    @Override
    public int httpStatus() {
        return switch (this) {
            case TRADE_ORDER_FORBIDDEN -> 403;
            case TRADE_COURSE_NOT_AVAILABLE, TRADE_ORDER_NOT_FOUND -> 404;
            case TRADE_FREE_COURSE,
                    TRADE_ORDER_STATUS_INVALID,
                    TRADE_COURSE_ALREADY_JOINED,
                    TRADE_PAY_RECORD_INVALID -> 409;
            default -> 400;
        };
    }
}
