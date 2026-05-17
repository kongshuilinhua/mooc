package com.elysia.mooc.common.idempotent;

import static org.assertj.core.api.Assertions.assertThat;

import com.elysia.mooc.common.idempotent.mapper.IdempotentRecordMapper;
import com.elysia.mooc.common.idempotent.service.impl.IdempotentServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** 幂等键生成测试。 */
class IdempotentServiceImplTest {

    private final IdempotentServiceImpl service =
            new IdempotentServiceImpl(Mockito.mock(IdempotentRecordMapper.class));

    @Test
    void buildRecordKeyShouldContainBizTypeAndUser() {
        String key = service.buildRecordKey("ORDER_CREATE", 3L, "abc");

        assertThat(key).isEqualTo("ORDER_CREATE:3:abc");
    }

    @Test
    void buildRecordKeyShouldHashWhenTooLong() {
        String key = service.buildRecordKey("ORDER_CREATE", 3L, "x".repeat(200));

        assertThat(key).startsWith("ORDER_CREATE:3:");
        assertThat(key.length()).isLessThanOrEqualTo(128);
    }
}
