package com.elysia.mooc.common.exception;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.error.ValidationErrorItem;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一把异常转换成中文错误响应。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param ex 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResult<Void>> handleBizException(BizException ex) {
        log.warn("业务异常：{}", ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理 Spring Security 方法级授权异常，避免 @PreAuthorize 拒绝访问时落入系统 500。
     *
     * @param ex 权限异常，包含 AuthorizationDeniedException 等子类
     * @return 统一无权限响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("权限不足：{}", ex.getMessage());
        return ResponseEntity
                .status(CommonErrorCode.FORBIDDEN.httpStatus())
                .body(ApiResult.fail(CommonErrorCode.FORBIDDEN.code(), CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * 处理参数校验和请求体格式异常。
     *
     * @param ex 参数相关异常
     * @return 包含字段错误明细的失败响应
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class
    })
    public ResponseEntity<ApiResult<Map<String, List<ValidationErrorItem>>>> handleParamException(Exception ex) {
        // 先提取字段级错误明细，再优先返回首个中文提示，方便前端直接展示。
        List<ValidationErrorItem> errors = extractValidationErrors(ex);
        String message = errors.isEmpty()
                ? CommonErrorCode.PARAM_INVALID.message()
                : errors.get(0).getMessage();
        return ResponseEntity
                .status(CommonErrorCode.PARAM_INVALID.httpStatus())
                .body(ApiResult.fail(
                        CommonErrorCode.PARAM_INVALID.code(),
                        message,
                        Map.of("errors", errors)));
    }

    /**
     * 处理未预期的系统异常。
     *
     * @param ex 未捕获异常
     * @return 统一系统错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception ex) {
        log.error("未处理的系统异常", ex);
        return ResponseEntity
                .status(CommonErrorCode.SYSTEM_ERROR.httpStatus())
                .body(ApiResult.fail(CommonErrorCode.SYSTEM_ERROR.code(), CommonErrorCode.SYSTEM_ERROR.message()));
    }

    /**
     * 从不同参数异常中提取字段级错误明细。
     *
     * @param ex 参数相关异常
     * @return 字段校验错误列表
     */
    private List<ValidationErrorItem> extractValidationErrors(Exception ex) {
        List<ValidationErrorItem> errors = new ArrayList<>();

        // 按异常类型分别提取字段级错误，统一收敛为前端可直接展示的错误列表。
        if (ex instanceof MethodArgumentNotValidException argumentException) {
            argumentException.getBindingResult().getFieldErrors().forEach(fieldError -> errors.add(
                    new ValidationErrorItem(fieldError.getField(), resolveFieldErrorMessage(fieldError))));
            argumentException.getBindingResult().getGlobalErrors().forEach(globalError -> errors.add(
                    new ValidationErrorItem("请求", globalError.getDefaultMessage())));
            return errors;
        }

        if (ex instanceof BindException bindException) {
            bindException.getBindingResult().getFieldErrors().forEach(fieldError -> errors.add(
                    new ValidationErrorItem(fieldError.getField(), resolveFieldErrorMessage(fieldError))));
            bindException.getBindingResult().getGlobalErrors().forEach(globalError -> errors.add(
                    new ValidationErrorItem("请求", globalError.getDefaultMessage())));
            return errors;
        }

        if (ex instanceof ConstraintViolationException violationException) {
            violationException.getConstraintViolations().forEach(violation -> errors.add(
                    new ValidationErrorItem(violation.getPropertyPath().toString(), violation.getMessage())));
            return errors;
        }

        if (ex instanceof MethodArgumentTypeMismatchException mismatchException) {
            errors.add(new ValidationErrorItem(mismatchException.getName(), "参数类型错误或枚举值不合法"));
            return errors;
        }

        if (ex instanceof ConversionFailedException) {
            errors.add(new ValidationErrorItem("请求", "参数类型错误或枚举值不合法"));
            return errors;
        }

        if (ex instanceof HttpMessageNotReadableException) {
            errors.add(new ValidationErrorItem("请求", "请求体格式错误或枚举值不合法"));
        }
        return errors;
    }

    private String resolveFieldErrorMessage(org.springframework.validation.FieldError fieldError) {
        if (fieldError != null && fieldError.getCodes() != null) {
            for (String code : fieldError.getCodes()) {
                if (code != null && code.startsWith("typeMismatch")) {
                    return "参数类型错误或枚举值不合法";
                }
            }
        }
        return fieldError == null ? CommonErrorCode.PARAM_INVALID.message() : fieldError.getDefaultMessage();
    }
}
