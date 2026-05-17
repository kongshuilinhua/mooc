package com.elysia.mooc.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.trade.constants.TradeConstants;
import com.elysia.mooc.trade.constants.TradeErrorCode;
import com.elysia.mooc.trade.domain.dto.CancelOrderRequest;
import com.elysia.mooc.trade.domain.dto.CreateOrderRequest;
import com.elysia.mooc.trade.domain.dto.OrderQuery;
import com.elysia.mooc.trade.domain.enums.OrderStatus;
import com.elysia.mooc.trade.domain.po.TradeOrderItemPO;
import com.elysia.mooc.trade.domain.po.TradeOrderPO;
import com.elysia.mooc.trade.domain.vo.OrderItemVO;
import com.elysia.mooc.trade.domain.vo.OrderVO;
import com.elysia.mooc.trade.mapper.TradeOrderItemMapper;
import com.elysia.mooc.trade.mapper.TradeOrderMapper;
import com.elysia.mooc.trade.service.OrderNoGenerator;
import com.elysia.mooc.trade.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 订单服务实现。 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserContextService userContextService;
    private final CourseMapper courseMapper;
    private final LearningCourseMapper learningCourseMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderItemMapper tradeOrderItemMapper;
    private final OrderNoGenerator orderNoGenerator;

    /**
     * 创建订单。
     *
     * @param request 创建订单请求
     * @return 订单详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderRequest request) {
        Long userId = userContextService.currentUserId();
        CoursePO course = requirePublishedCourse(request.getCourseId());
        if (isFreeCourse(course)) {
            throw new BizException(TradeErrorCode.TRADE_FREE_COURSE, "免费课程不需要创建付费订单，请直接加入学习");
        }
        if (hasJoinedCourse(userId, course.getId())) {
            throw new BizException(TradeErrorCode.TRADE_COURSE_ALREADY_JOINED);
        }

        // 同一用户同一课程已有待支付订单时直接复用，避免前端重复点击生成多条待支付订单。
        TradeOrderPO existedUnpaidOrder = findUnpaidOrder(userId, course.getId());
        if (existedUnpaidOrder != null) {
            return toOrderVO(existedUnpaidOrder);
        }

        TradeOrderPO order = new TradeOrderPO();
        order.setOrderNo(orderNoGenerator.nextOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(course.getPrice());
        order.setPayAmount(course.getPrice());
        order.setStatus(OrderStatus.UNPAID);
        order.setExpireTime(LocalDateTime.now().plusMinutes(TradeConstants.DEFAULT_ORDER_EXPIRE_MINUTES));
        order.setDeleted(0);
        tradeOrderMapper.insert(order);

        TradeOrderItemPO item = new TradeOrderItemPO();
        item.setOrderId(order.getId());
        item.setCourseId(course.getId());
        item.setCourseTitle(course.getTitle());
        item.setCourseCover(course.getCoverUrl());
        item.setPrice(course.getPrice());
        item.setQuantity(1);
        tradeOrderItemMapper.insert(item);

        return toOrderVO(order, List.of(item));
    }

    /**
     * 分页查询我的订单。
     *
     * @param query 查询参数
     * @return 订单分页
     */
    @Override
    public PageResult<OrderVO> listMyOrders(OrderQuery query) {
        OrderQuery safeQuery = query == null ? new OrderQuery() : query;
        Long userId = userContextService.currentUserId();
        Set<Long> orderIdsByKeyword = collectOrderIdsByKeyword(safeQuery.getKeyword());
        if (orderIdsByKeyword != null && orderIdsByKeyword.isEmpty()) {
            return PageResult.empty(0L, 0);
        }

        LambdaQueryWrapper<TradeOrderPO> wrapper = Wrappers.<TradeOrderPO>lambdaQuery()
                .eq(TradeOrderPO::getUserId, userId);
        if (safeQuery.getStatus() != null) {
            wrapper.eq(TradeOrderPO::getStatus, safeQuery.getStatus());
        }
        if (orderIdsByKeyword != null) {
            wrapper.in(TradeOrderPO::getId, orderIdsByKeyword);
        }
        applySort(wrapper, safeQuery.getSortBy(), safeQuery.getIsAsc());

        Page<TradeOrderPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<TradeOrderPO> result = tradeOrderMapper.selectPage(page, wrapper);
        return PageResult.of(result, toOrderVOList(result.getRecords()));
    }

    /**
     * 查询订单详情。
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long orderId) {
        Long userId = userContextService.currentUserId();
        TradeOrderPO order = requireOrder(orderId);
        checkOwner(order, userId);
        return toOrderVO(order);
    }

    /**
     * 取消订单。
     *
     * @param orderId 订单 ID
     * @param request 取消请求
     * @return 取消后的订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancelOrder(Long orderId, CancelOrderRequest request) {
        Long userId = userContextService.currentUserId();
        TradeOrderPO order = requireOrder(orderId);
        checkOwner(order, userId);
        if (order.getStatus() != OrderStatus.UNPAID) {
            throw new BizException(TradeErrorCode.TRADE_ORDER_STATUS_INVALID, "只有待支付订单可以取消");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
        return toOrderVO(order);
    }

    private CoursePO requirePublishedCourse(Long courseId) {
        CoursePO course = courseId == null ? null : courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BizException(TradeErrorCode.TRADE_COURSE_NOT_AVAILABLE);
        }
        return course;
    }

    private boolean hasJoinedCourse(Long userId, Long courseId) {
        return learningCourseMapper.selectCount(Wrappers.<LearningCoursePO>lambdaQuery()
                .eq(LearningCoursePO::getUserId, userId)
                .eq(LearningCoursePO::getCourseId, courseId)) > 0;
    }

    private TradeOrderPO findUnpaidOrder(Long userId, Long courseId) {
        List<TradeOrderItemPO> items = tradeOrderItemMapper.selectList(Wrappers.<TradeOrderItemPO>lambdaQuery()
                .eq(TradeOrderItemPO::getCourseId, courseId));
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        List<Long> orderIds = items.stream().map(TradeOrderItemPO::getOrderId).distinct().toList();
        return tradeOrderMapper.selectList(Wrappers.<TradeOrderPO>lambdaQuery()
                        .eq(TradeOrderPO::getUserId, userId)
                        .eq(TradeOrderPO::getStatus, OrderStatus.UNPAID)
                        .in(TradeOrderPO::getId, orderIds)
                        .orderByDesc(TradeOrderPO::getCreateTime)
                        .orderByDesc(TradeOrderPO::getId))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private TradeOrderPO requireOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BizException(TradeErrorCode.TRADE_ORDER_NOT_FOUND);
        }
        TradeOrderPO order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(TradeErrorCode.TRADE_ORDER_NOT_FOUND);
        }
        return order;
    }

    private void checkOwner(TradeOrderPO order, Long userId) {
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BizException(TradeErrorCode.TRADE_ORDER_FORBIDDEN);
        }
    }

    private boolean isFreeCourse(CoursePO course) {
        return course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0;
    }

    private Set<Long> collectOrderIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String trimmed = keyword.trim();
        Set<Long> orderIds = tradeOrderMapper.selectList(Wrappers.<TradeOrderPO>lambdaQuery()
                        .like(TradeOrderPO::getOrderNo, trimmed))
                .stream()
                .map(TradeOrderPO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        tradeOrderItemMapper.selectList(Wrappers.<TradeOrderItemPO>lambdaQuery()
                        .like(TradeOrderItemPO::getCourseTitle, trimmed))
                .stream()
                .map(TradeOrderItemPO::getOrderId)
                .forEach(orderIds::add);
        return orderIds;
    }

    private void applySort(LambdaQueryWrapper<TradeOrderPO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("payTime".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, TradeOrderPO::getPayTime);
        } else if ("payAmount".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, TradeOrderPO::getPayAmount);
        } else {
            wrapper.orderBy(true, asc, TradeOrderPO::getCreateTime);
        }
        wrapper.orderByDesc(TradeOrderPO::getId);
    }

    private List<OrderVO> toOrderVOList(List<TradeOrderPO> orders) {
        if (CollectionUtils.isEmpty(orders)) {
            return Collections.emptyList();
        }
        Map<Long, List<TradeOrderItemPO>> itemMap = mapItems(orders.stream()
                .map(TradeOrderPO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return orders.stream()
                .map(order -> toOrderVO(order, itemMap.getOrDefault(order.getId(), Collections.emptyList())))
                .toList();
    }

    private OrderVO toOrderVO(TradeOrderPO order) {
        List<TradeOrderItemPO> items = tradeOrderItemMapper.selectList(Wrappers.<TradeOrderItemPO>lambdaQuery()
                .eq(TradeOrderItemPO::getOrderId, order.getId()));
        return toOrderVO(order, items);
    }

    private OrderVO toOrderVO(TradeOrderPO order, List<TradeOrderItemPO> items) {
        return BeanCopyUtils.copyBean(order, OrderVO.class, (source, target) -> {
            target.setStatusDesc(source.getStatus() == null ? null : source.getStatus().getDesc());
            target.setItems(BeanCopyUtils.copyList(items, OrderItemVO.class));
        });
    }

    private Map<Long, List<TradeOrderItemPO>> mapItems(Set<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return Collections.emptyMap();
        }
        return tradeOrderItemMapper.selectList(Wrappers.<TradeOrderItemPO>lambdaQuery()
                        .in(TradeOrderItemPO::getOrderId, orderIds))
                .stream()
                .collect(Collectors.groupingBy(TradeOrderItemPO::getOrderId));
    }
}
