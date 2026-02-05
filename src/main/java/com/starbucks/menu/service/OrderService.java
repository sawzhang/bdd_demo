package com.starbucks.menu.service;

import com.starbucks.menu.domain.Order;
import com.starbucks.menu.integration.*;
import com.starbucks.menu.integration.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单服务
 *
 * 演示如何在业务服务中集成外部依赖
 * 在测试中，外部依赖会被 Mock
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private SmsService smsService;

    @Autowired
    private MemberService memberService;

    /**
     * 创建订单
     *
     * 依赖外部服务：库存系统、支付网关
     */
    @Transactional
    public Order createOrder(String userName, String productName, int quantity) {
        log.info("创建订单 - 用户: {}, 产品: {}, 数量: {}", userName, productName, quantity);

        // 1. 检查库存（外部依赖）
        String productCode = "LATTE-GRANDE"; // 简化处理
        InventoryCheckResult inventoryCheck = inventoryService.checkInventory(productCode, quantity);

        if (!inventoryCheck.isSufficient()) {
            throw new RuntimeException("库存不足");
        }

        // 2. 创建订单
        String orderNo = generateOrderNo();
        BigDecimal unitPrice = new BigDecimal("36.00");
        BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));

        Order order = Order.builder()
            .orderNo(orderNo)
            .userName(userName)
            .productName(productName)
            .productCode(productCode)
            .quantity(quantity)
            .totalAmount(totalAmount)
            .actualAmount(totalAmount)
            .status(Order.OrderStatus.PENDING_PAYMENT)
            .createdAt(LocalDateTime.now())
            .build();

        // 3. 锁定库存（外部依赖）
        boolean locked = inventoryService.lockInventory(productCode, quantity, orderNo);
        if (!locked) {
            throw new RuntimeException("库存锁定失败");
        }

        // 4. 创建支付订单（外部依赖）
        try {
            PaymentOrder paymentOrder = paymentGateway.createPayment(totalAmount, orderNo);
            order.setPaymentId(paymentOrder.getPaymentId());
        } catch (Exception e) {
            log.error("支付网关调用失败", e);
            // 回滚库存
            inventoryService.releaseInventory(orderNo);
            throw new RuntimeException("支付网关超时", e);
        }

        log.info("✓ 订单创建成功: {}", orderNo);
        return order;
    }

    /**
     * 创建多产品订单
     */
    @Transactional
    public Order createOrderWithMultipleItems(String userName, List<Map<String, String>> items) {
        // 简化实现，实际应该支持多产品
        String orderNo = generateOrderNo();

        Order order = Order.builder()
            .orderNo(orderNo)
            .userName(userName)
            .totalAmount(new BigDecimal("150.00"))
            .actualAmount(new BigDecimal("150.00"))
            .status(Order.OrderStatus.PENDING_PAYMENT)
            .createdAt(LocalDateTime.now())
            .build();

        return order;
    }

    /**
     * 应用积分抵扣
     */
    public void applyPointsDeduction(Order order, int points) {
        // 100 积分 = 1 元
        BigDecimal discount = new BigDecimal(points).divide(new BigDecimal("100"));

        order.setPointsDiscount(discount);
        order.setActualAmount(order.getTotalAmount().subtract(discount));
    }

    /**
     * 完成支付
     *
     * 依赖外部服务：支付网关、短信服务
     */
    @Transactional
    public void completePayment(Order order) {
        log.info("完成支付 - 订单: {}", order.getOrderNo());

        try {
            // 1. 查询支付状态（外部依赖）
            PaymentStatus status = paymentGateway.queryPaymentStatus(order.getPaymentId());

            if (status == PaymentStatus.SUCCESS) {
                // 2. 更新订单状态
                order.completePay(order.getPaymentId());

                // 3. 扣减库存（外部依赖）
                inventoryService.deductInventory(order.getOrderNo());

                // 4. 发送短信通知（外部依赖，可降级）
                try {
                    String message = "订单支付成功，订单号: " + order.getOrderNo();
                    smsService.sendSms("13800138000", message);
                } catch (Exception e) {
                    log.warn("短信发送失败，降级处理", e);
                    // 短信失败不影响主流程
                }

                log.info("✓ 支付完成: {}", order.getOrderNo());
            } else {
                throw new RuntimeException("支付失败");
            }

        } catch (Exception e) {
            // 支付超时，设置为处理中
            order.setPaymentProcessing();
            throw new RuntimeException("支付超时", e);
        }
    }

    /**
     * 取消订单
     *
     * 依赖外部服务：支付网关、库存系统
     */
    @Transactional
    public void cancelOrder(Order order) {
        log.info("取消订单: {}", order.getOrderNo());

        // 1. 取消订单
        order.cancel();

        // 2. 退款（如果已支付）
        if (order.getPaymentId() != null) {
            paymentGateway.refund(order.getPaymentId());
        }

        // 3. 释放库存（可能失败，需要补偿）
        try {
            inventoryService.releaseInventory(order.getOrderNo());
        } catch (Exception e) {
            log.error("库存释放失败，需要创建补偿任务", e);
            // 创建补偿任务（简化处理）
            throw e;
        }

        log.info("✓ 订单取消成功");
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD-" + System.currentTimeMillis() + "-" +
            UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
