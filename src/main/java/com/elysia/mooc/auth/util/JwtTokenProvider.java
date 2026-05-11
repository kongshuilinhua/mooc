package com.elysia.mooc.auth.util;

import com.elysia.mooc.auth.security.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 和刷新令牌工具，负责令牌生成、解析和摘要计算。
 */
@Component
public class JwtTokenProvider {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${mooc.auth.jwt.secret:mooc-dev-secret-change-me-change-me-123456}")
    private String secret;

    @Value("${mooc.auth.jwt.access-token-minutes:120}")
    private long accessTokenMinutes;

    @Value("${mooc.auth.jwt.refresh-token-days:7}")
    private long refreshTokenDays;

    /**
     * 创建 accessToken。
     *
     * @param loginUser 登录用户
     * @return JWT accessToken
     */
    public String createAccessToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(Duration.ofMinutes(accessTokenMinutes));
        return Jwts.builder()
                .subject(String.valueOf(loginUser.getUserId()))
                .claim("userId", loginUser.getUserId())
                .claim("username", loginUser.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey())
                .compact();
    }

    /**
     * 解析 accessToken。
     *
     * @param token JWT accessToken
     * @return 登录用户信息
     */
    public LoginUser parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        return new LoginUser(userId, username);
    }

    /**
     * 创建 refreshToken 明文。
     *
     * @return 随机刷新令牌
     */
    public String createRefreshToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 计算令牌摘要，数据库只保存摘要。
     *
     * @param token 令牌明文
     * @return SHA-256 十六进制摘要
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前运行环境不支持SHA-256算法", ex);
        }
    }

    /**
     * 获取 accessToken 有效秒数。
     *
     * @return accessToken 有效秒数
     */
    public long accessTokenSeconds() {
        return Duration.ofMinutes(accessTokenMinutes).toSeconds();
    }

    /**
     * 获取 refreshToken 过期时间。
     *
     * @return refreshToken 过期时间
     */
    public Instant refreshTokenExpireAt() {
        return Instant.now().plus(Duration.ofDays(refreshTokenDays));
    }

    /**
     * 构建 JWT 签名密钥。
     *
     * @return HMAC 签名密钥
     */
    private SecretKey secretKey() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new JwtException("JWT密钥长度不能少于32字节");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}