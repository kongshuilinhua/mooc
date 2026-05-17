package com.elysia.mooc.statistics.service.impl;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.statistics.domain.po.UserSearchLogPO;
import com.elysia.mooc.statistics.mapper.UserSearchLogMapper;
import com.elysia.mooc.statistics.service.SearchLogService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 搜索日志服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchLogServiceImpl implements SearchLogService {

    private final UserSearchLogMapper userSearchLogMapper;

    /**
     * 记录搜索日志。
     *
     * @param keyword     搜索关键词
     * @param resultCount 结果数量
     */
    @Override
    public void recordSearch(String keyword, int resultCount) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        try {
            UserSearchLogPO log = new UserSearchLogPO();
            log.setUserId(currentUserIdOrNull());
            log.setKeyword(keyword.trim());
            log.setResultCount(Math.max(resultCount, 0));
            log.setCreateTime(LocalDateTime.now());
            userSearchLogMapper.insert(log);
        } catch (RuntimeException ex) {
            log.warn("记录搜索日志失败，keyword={}", keyword, ex);
        }
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
