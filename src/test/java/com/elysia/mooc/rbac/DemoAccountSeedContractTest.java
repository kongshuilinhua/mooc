package com.elysia.mooc.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** 演示账号与 RBAC 种子脚本合同测试。 */
class DemoAccountSeedContractTest {

    private static final Path DEFAULT_DOCS_ROOT = Path.of(
            System.getProperty("user.home"),
            "Desktop",
            "2026作业");

    private static final Pattern USER_ROW_PATTERN = Pattern.compile(
            "\\(\\s*\\d+\\s*,\\s*'%s'\\s*,\\s*'([^']+)'");

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void demoAccountPasswordHashesShouldMatchDocumentedPasswords() throws IOException {
        String authSql = readSql("V002__auth.sql");

        assertThat(passwordEncoder.matches("Admin@123456", passwordHashOf(authSql, "admin"))).isTrue();
        assertThat(passwordEncoder.matches("Teacher@123456", passwordHashOf(authSql, "teacher"))).isTrue();
        assertThat(passwordEncoder.matches("Student@123456", passwordHashOf(authSql, "student"))).isTrue();
    }

    @Test
    void rbacSeedShouldRepairDemoAccountRolesByUsername() throws IOException {
        String rbacSql = readSql("V003__rbac_seed.sql");

        // 演示账号允许反复重放脚本，必须清理历史错误角色，避免 teacher 被旧数据赋予 ADMIN。
        assertThat(rbacSql)
                .contains("DELETE ur")
                .contains("u.username IN ('admin', 'teacher', 'student')")
                .contains("u.username = 'admin' AND r.code = 'ADMIN'")
                .contains("u.username = 'teacher' AND r.code = 'TEACHER'")
                .contains("u.username = 'student' AND r.code = 'STUDENT'")
                .doesNotContain("(2, 3, NOW())")
                .doesNotContain("(3, 2, NOW())");
    }

    /**
     * 从文档 SQL 目录读取脚本；其他机器没有该目录时跳过，避免普通单元测试被本机路径强绑定。
     *
     * @param fileName SQL 文件名
     * @return SQL 文本
     */
    private String readSql(String fileName) throws IOException {
        Path sqlPath = docsRoot().resolve("每日SQL脚本").resolve(fileName);
        assumeTrue(Files.exists(sqlPath), "未找到文档 SQL 脚本：" + sqlPath);
        return Files.readString(sqlPath, StandardCharsets.UTF_8);
    }

    /**
     * 允许测试环境通过系统属性覆盖文档根目录，默认使用本项目约定位置。
     *
     * @return 文档根目录
     */
    private Path docsRoot() {
        String configured = System.getProperty("mooc.docs.root");
        return configured == null || configured.isBlank() ? DEFAULT_DOCS_ROOT : Path.of(configured);
    }

    /**
     * 从演示账号初始化 SQL 中提取指定用户的 BCrypt 密文。
     *
     * @param sql      SQL 文本
     * @param username 用户名
     * @return BCrypt 密文
     */
    private String passwordHashOf(String sql, String username) {
        Matcher matcher = Pattern.compile(USER_ROW_PATTERN.pattern().formatted(Pattern.quote(username)))
                .matcher(sql);
        assertThat(matcher.find()).as("应存在演示账号：" + username).isTrue();
        return matcher.group(1);
    }
}
