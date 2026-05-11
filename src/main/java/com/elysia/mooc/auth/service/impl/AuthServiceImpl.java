package com.elysia.mooc.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.auth.constants.AuthErrorCode;
import com.elysia.mooc.auth.domain.dto.LoginRequest;
import com.elysia.mooc.auth.domain.dto.LogoutRequest;
import com.elysia.mooc.auth.domain.dto.RefreshTokenRequest;
import com.elysia.mooc.auth.domain.dto.RegisterRequest;
import com.elysia.mooc.auth.domain.po.AuthRefreshTokenPO;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import com.elysia.mooc.auth.domain.vo.CurrentUserVO;
import com.elysia.mooc.auth.domain.vo.LoginResult;
import com.elysia.mooc.auth.domain.vo.TokenPairVO;
import com.elysia.mooc.auth.mapper.AuthRefreshTokenMapper;
import com.elysia.mooc.auth.mapper.SysUserMapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.AuthService;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.auth.util.JwtTokenProvider;
import com.elysia.mooc.common.exception.BizException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证业务服务实现，处理注册、登录、刷新令牌和当前用户查询。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final SysUserMapper sysUserMapper;
    private final AuthRefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserContextService userContextService;

    /**
     * 注册用户并返回用户基础信息。
     *
     * @param request 注册请求
     * @return 新用户基础信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CurrentUserVO register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = normalizeBlank(request.getEmail());
        String phone = normalizeBlank(request.getPhone());

        // 1. 先做账号唯一性校验，避免注册阶段写入重复用户名、邮箱或手机号。
        ensureUsernameNotExists(username);
        ensureEmailNotExists(email);
        ensurePhoneNotExists(phone);

        // 2. 再构建用户实体并完成密码加密，保证数据库中只保存 BCrypt 密文。
        SysUserPO user = SysUserPO.createRegisteredUser(
                username,
                passwordEncoder.encode(request.getPassword()),
                request.getNickname().trim(),
                email,
                phone);
        sysUserMapper.insert(user);
        return toCurrentUser(user);
    }

    /**
     * 校验账号密码并签发 accessToken 和 refreshToken。
     *
     * @param request 登录请求
     * @param clientIp 客户端 IP
     * @return 登录结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult login(LoginRequest request, String clientIp) {
        // 1. 先根据用户名、邮箱或手机号定位用户，并统一处理账号密码错误提示。
        SysUserPO user = findUserByAccount(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(AuthErrorCode.AUTH_BAD_CREDENTIALS);
        }
        if (SysUserPO.STATUS_DISABLED == user.getStatus()) {
            throw new BizException(AuthErrorCode.AUTH_USER_DISABLED);
        }

        // 2. 校验通过后签发 accessToken、refreshToken，并把 refreshToken 摘要落库。
        String accessToken = jwtTokenProvider.createAccessToken(new LoginUser(user.getId(), user.getUsername()));
        String refreshToken = jwtTokenProvider.createRefreshToken();
        saveRefreshToken(user.getId(), refreshToken, request.getDeviceId(), request.getClientType());

        // 3. 回写最近登录时间和登录 IP，便于审计和账号安全追踪。
        user.recordLogin(clientIp);
        sysUserMapper.updateById(user);

        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(jwtTokenProvider.accessTokenSeconds())
                .user(toCurrentUser(user))
                .build();
    }

    /**
     * 刷新 accessToken，同时轮换 refreshToken。
     *
     * @param request 刷新 Token 请求
     * @return 新 Token
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenPairVO refresh(RefreshTokenRequest request) {
        // 1. 先校验旧 refreshToken 是否存在且仍然有效。
        AuthRefreshTokenPO oldToken = findActiveRefreshToken(request.getRefreshToken());
        SysUserPO user = sysUserMapper.selectById(oldToken.getUserId());
        if (!isUserAvailable(user)) {
            throw new BizException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }
        
        // 追加设备校验，防止 refreshToken 被恶意迁移使用
        if (StringUtils.hasText(request.getDeviceId()) && StringUtils.hasText(oldToken.getDeviceId())) {
            if (!request.getDeviceId().equals(oldToken.getDeviceId())) {
                throw new BizException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID, "设备不匹配，请重新登录");
            }
        }

        // 2. 轮换 refreshToken，先撤销旧记录，再写入新摘要，避免旧令牌继续使用。
        revokeRefreshToken(oldToken);
        String newRefreshToken = jwtTokenProvider.createRefreshToken();
        saveRefreshToken(user.getId(), newRefreshToken, request.getDeviceId(), oldToken.getClientType());
        String accessToken = jwtTokenProvider.createAccessToken(new LoginUser(user.getId(), user.getUsername()));

        return TokenPairVO.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(jwtTokenProvider.accessTokenSeconds())
                .build();
    }

    /**
     * 撤销刷新令牌。
     *
     * @param request 退出登录请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(LogoutRequest request) {
        AuthRefreshTokenPO refreshToken = refreshTokenMapper.selectOne(Wrappers.<AuthRefreshTokenPO>lambdaQuery()
                .eq(AuthRefreshTokenPO::getTokenHash, jwtTokenProvider.hashToken(request.getRefreshToken()))
                .eq(AuthRefreshTokenPO::getDeleted, AuthRefreshTokenPO.DELETED_NO)
                .last("LIMIT 1"));
        if (refreshToken != null && refreshToken.isActive()) {
            revokeRefreshToken(refreshToken);
        }
    }

    /**
     * 查询当前登录用户。
     *
     * @return 当前用户信息
     */
    @Override
    public CurrentUserVO currentUser() {
        LoginUser loginUser = userContextService.currentLoginUser();
        SysUserPO user = sysUserMapper.selectById(loginUser.getUserId());
        if (!isUserAvailable(user)) {
            throw new BizException(AuthErrorCode.AUTH_USER_UNAVAILABLE);
        }
        return toCurrentUser(user);
    }

    /**
     * 校验用户名是否已存在。
     *
     * @param username 用户名
     */
    private void ensureUsernameNotExists(String username) {
        Long count = sysUserMapper.selectCount(Wrappers.<SysUserPO>lambdaQuery()
                .eq(SysUserPO::getUsername, username)
                .eq(SysUserPO::getDeleted, SysUserPO.DELETED_NO));
        if (count > 0) {
            throw new BizException(AuthErrorCode.AUTH_USERNAME_EXISTS);
        }
    }

    /**
     * 校验邮箱是否已被占用。
     *
     * @param email 邮箱
     */
    private void ensureEmailNotExists(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        Long count = sysUserMapper.selectCount(Wrappers.<SysUserPO>lambdaQuery()
                .eq(SysUserPO::getEmail, email)
                .eq(SysUserPO::getDeleted, SysUserPO.DELETED_NO));
        if (count > 0) {
            throw new BizException(AuthErrorCode.AUTH_EMAIL_EXISTS);
        }
    }

    /**
     * 校验手机号是否已被占用。
     *
     * @param phone 手机号
     */
    private void ensurePhoneNotExists(String phone) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        Long count = sysUserMapper.selectCount(Wrappers.<SysUserPO>lambdaQuery()
                .eq(SysUserPO::getPhone, phone)
                .eq(SysUserPO::getDeleted, SysUserPO.DELETED_NO));
        if (count > 0) {
            throw new BizException(AuthErrorCode.AUTH_PHONE_EXISTS);
        }
    }

    /**
     * 按用户名、邮箱或手机号查询用户。
     *
     * @param account 账号标识
     * @return 用户实体，未找到时返回 null
     */
    private SysUserPO findUserByAccount(String account) {
        String normalizedAccount = account.trim();
        return sysUserMapper.selectOne(Wrappers.<SysUserPO>lambdaQuery()
                .eq(SysUserPO::getDeleted, SysUserPO.DELETED_NO)
                .and(wrapper -> wrapper
                        .eq(SysUserPO::getUsername, normalizedAccount)
                        .or()
                        .eq(SysUserPO::getEmail, normalizedAccount)
                        .or()
                        .eq(SysUserPO::getPhone, normalizedAccount))
                .last("LIMIT 1"));
    }

    /**
     * 查询并校验有效的 refreshToken 记录。
     *
     * @param refreshToken refreshToken 明文
     * @return 有效的 refreshToken 实体
     */
    private AuthRefreshTokenPO findActiveRefreshToken(String refreshToken) {
        AuthRefreshTokenPO token = refreshTokenMapper.selectOne(Wrappers.<AuthRefreshTokenPO>lambdaQuery()
                .eq(AuthRefreshTokenPO::getTokenHash, jwtTokenProvider.hashToken(refreshToken))
                .eq(AuthRefreshTokenPO::getDeleted, AuthRefreshTokenPO.DELETED_NO)
                .last("LIMIT 1"));
        if (token == null || !token.isActive()) {
            throw new BizException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }
        return token;
    }

    /**
     * 保存 refreshToken 摘要记录。
     *
     * @param userId 用户 ID
     * @param refreshToken refreshToken 明文
     * @param deviceId 设备标识
     * @param clientType 客户端类型
     */
    private void saveRefreshToken(Long userId, String refreshToken, String deviceId, String clientType) {
        AuthRefreshTokenPO token = AuthRefreshTokenPO.issue(
                userId,
                jwtTokenProvider.hashToken(refreshToken),
                normalizeBlank(deviceId),
                StringUtils.hasText(clientType) ? clientType.trim() : "web",
                LocalDateTime.ofInstant(jwtTokenProvider.refreshTokenExpireAt(), ZoneId.systemDefault()));
        refreshTokenMapper.insert(token);
    }

    /**
     * 撤销 refreshToken 记录。
     *
     * @param token refreshToken 实体
     */
    private void revokeRefreshToken(AuthRefreshTokenPO token) {
        token.revoke();
        refreshTokenMapper.updateById(token);
    }

    /**
     * 判断用户是否仍然可用。
     *
     * @param user 用户实体
     * @return 是否可用
     */
    private boolean isUserAvailable(SysUserPO user) {
        return user != null && user.isAvailable();
    }

    /**
     * 把用户实体转换为当前用户视图对象。
     *
     * @param user 用户实体
     * @return 当前用户视图对象
     */
    private CurrentUserVO toCurrentUser(SysUserPO user) {
        return CurrentUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(Collections.emptyList())
                .permissions(Collections.emptyList())
                .build();
    }

    /**
     * 把空白字符串规范化为 null。
     *
     * @param value 原始值
     * @return 去空白后的值，空白时返回 null
     */
    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}