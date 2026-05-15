package com.elysia.mooc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
class MoocApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void executeSql() throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get("V003.sql")), StandardCharsets.UTF_8);
        String[] statements = sql.split(";");
        for (String statement : statements) {
            String q = statement.trim();
            if (q.isEmpty()) continue;
            jdbcTemplate.execute(q);
        }
    }
}
