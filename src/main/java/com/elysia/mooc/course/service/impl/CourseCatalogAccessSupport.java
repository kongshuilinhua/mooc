package com.elysia.mooc.course.service.impl;

import com.elysia.mooc.auth.constants.AuthErrorCode;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.constants.CourseConstants;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 课程目录鉴权支持，集中处理课程可见性、归属和状态校验。 */
abstract class CourseCatalogAccessSupport {

    /**
     * 查询目录可见课程。
     *
     * @param courseMapper 课程 Mapper
     * @param courseId     课程 ID
     * @return 当前用户可见的课程
     */
    protected CoursePO getCatalogVisibleCourse(CourseMapper courseMapper, Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BizException(CourseErrorCode.CATALOG_COURSE_NOT_FOUND);
        }
        CoursePO course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(CourseErrorCode.CATALOG_COURSE_NOT_FOUND);
        }
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            return course;
        }

        LoginUser loginUser = currentLoginUserOrNull();
        if (loginUser == null) {
            throw new BizException(CourseErrorCode.CATALOG_COURSE_NOT_FOUND);
        }
        if (isAdmin(loginUser) || Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            return course;
        }
        if (hasRole(loginUser, CourseConstants.ROLE_TEACHER)) {
            throw new BizException(CourseErrorCode.CATALOG_FORBIDDEN);
        }
        throw new BizException(CourseErrorCode.CATALOG_COURSE_NOT_FOUND);
    }

    /**
     * 校验目录维护权限。
     *
     * @param course 被维护课程
     * @return 当前登录用户
     */
    protected LoginUser requireCatalogMaintainer(CoursePO course) {
        LoginUser loginUser = currentLoginUserOrNull();
        if (loginUser == null) {
            throw new BizException(AuthErrorCode.AUTH_LOGIN_REQUIRED);
        }
        if (isAdmin(loginUser)) {
            validateCourseEditable(course);
            return loginUser;
        }
        if (!hasRole(loginUser, CourseConstants.ROLE_TEACHER)
                || !hasPermission(loginUser, CourseConstants.PERMISSION_COURSE_PUBLISH)
                || !Objects.equals(course.getTeacherId(), loginUser.getUserId())) {
            throw new BizException(CourseErrorCode.CATALOG_FORBIDDEN);
        }
        validateCourseEditable(course);
        return loginUser;
    }

    /**
     * 校验课程是否允许维护目录。
     *
     * @param course 被维护课程
     */
    protected void validateCourseEditable(CoursePO course) {
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new BizException(CourseErrorCode.CATALOG_STATUS_INVALID);
        }
    }

    protected boolean isAdmin(LoginUser loginUser) {
        return hasRole(loginUser, CourseConstants.ROLE_ADMIN);
    }

    protected boolean hasRole(LoginUser loginUser, String roleCode) {
        return loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(roleCode::equalsIgnoreCase);
    }

    protected boolean hasPermission(LoginUser loginUser, String permissionCode) {
        return loginUser != null
                && loginUser.getPermissions() != null
                && loginUser.getPermissions().contains(permissionCode);
    }

    protected LoginUser currentLoginUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }
}
