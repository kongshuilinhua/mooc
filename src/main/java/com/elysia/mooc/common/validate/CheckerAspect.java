package com.elysia.mooc.common.validate;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/** 参数自校验切面，调用实现 Checker 的请求对象。 */
@Aspect
@Component
public class CheckerAspect {

    @Before("@annotation(com.elysia.mooc.common.validate.ParamChecker)")
    public void before(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        for (Object arg : args) {
            checkArgument(arg);
        }
    }

    @SuppressWarnings("rawtypes")
    private void checkArgument(Object arg) {
        if (arg instanceof Checker checker) {
            checker.check();
            return;
        }
        if (arg instanceof Iterable iterable) {
            for (Object item : iterable) {
                checkArgument(item);
            }
        }
    }
}
