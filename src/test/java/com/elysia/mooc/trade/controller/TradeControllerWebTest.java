package com.elysia.mooc.trade.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.trade.domain.dto.MockPayRequest;
import com.elysia.mooc.trade.domain.dto.OrderQuery;
import com.elysia.mooc.trade.domain.enums.OrderStatus;
import com.elysia.mooc.trade.domain.enums.PayStatus;
import com.elysia.mooc.trade.domain.vo.OrderItemVO;
import com.elysia.mooc.trade.domain.vo.OrderVO;
import com.elysia.mooc.trade.domain.vo.PayResultVO;
import com.elysia.mooc.trade.service.OrderService;
import com.elysia.mooc.trade.service.PayService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 交易接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class TradeControllerWebTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PayService payService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new OrderController(orderService),
                        new PayController(payService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createOrderShouldReturnOrderVO() throws Exception {
        when(orderService.createOrder(any())).thenReturn(orderVO(OrderStatus.UNPAID));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":3002}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(21001))
                .andExpect(jsonPath("$.data.status").value("UNPAID"))
                .andExpect(jsonPath("$.data.statusDesc").value("待支付"))
                .andExpect(jsonPath("$.data.items[0].courseId").value(3002));
    }

    @Test
    void listOrdersShouldAcceptStatusQueryAndReturnPageResult() throws Exception {
        when(orderService.listMyOrders(any())).thenReturn(PageResult.of(1L, 10, List.of(orderVO(OrderStatus.PAID))));

        mockMvc.perform(get("/api/orders?pageNo=1&pageSize=10&status=PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value("PAID"));

        ArgumentCaptor<OrderQuery> captor = ArgumentCaptor.forClass(OrderQuery.class);
        verify(orderService).listMyOrders(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void mockPayShouldAcceptMockChannel() throws Exception {
        PayResultVO result = new PayResultVO();
        result.setOrderId(21001L);
        result.setOrderNo("ORD1");
        result.setPayNo("PAY1");
        result.setPayStatus(PayStatus.SUCCESS);
        result.setOrderStatus(OrderStatus.PAID);
        result.setPayAmount(new BigDecimal("199.00"));
        result.setLearningGranted(true);
        result.setMessage("支付成功，课程已加入学习");
        when(payService.mockPay(eq(21001L), any())).thenReturn(result);

        mockMvc.perform(post("/api/orders/21001/pay/mock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payChannel\":\"MOCK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.payStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data.orderStatus").value("PAID"))
                .andExpect(jsonPath("$.data.learningGranted").value(true));

        ArgumentCaptor<MockPayRequest> captor = ArgumentCaptor.forClass(MockPayRequest.class);
        verify(payService).mockPay(eq(21001L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getPayChannel()).isEqualTo("MOCK");
    }

    private OrderVO orderVO(OrderStatus status) {
        OrderItemVO item = new OrderItemVO();
        item.setId(21101L);
        item.setCourseId(3002L);
        item.setCourseTitle("测试课程");
        item.setPrice(new BigDecimal("199.00"));
        item.setQuantity(1);

        OrderVO order = new OrderVO();
        order.setId(21001L);
        order.setOrderNo("ORD1");
        order.setUserId(3L);
        order.setTotalAmount(new BigDecimal("199.00"));
        order.setPayAmount(new BigDecimal("199.00"));
        order.setStatus(status);
        order.setStatusDesc(status.getDesc());
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        order.setItems(List.of(item));
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}
