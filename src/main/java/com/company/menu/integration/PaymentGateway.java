package com.company.menu.integration;

import com.company.menu.integration.dto.PaymentOrder;
import com.company.menu.integration.dto.PaymentStatus;

import java.math.BigDecimal;

/**
 * 支付网关接口（外部依赖）
 *
 * 模拟与第三方支付系统的集成
 * 在 BDD 测试中使用 @MockBean 进行 Mock
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
public interface PaymentGateway {

    /**
     * 创建支付订单
     *
     * @param amount 支付金额
     * @param orderNo 订单号
     * @return 支付订单
     */
    PaymentOrder createPayment(BigDecimal amount, String orderNo);

    /**
     * 查询支付状态
     *
     * @param paymentId 支付ID
     * @return 支付状态
     */
    PaymentStatus queryPaymentStatus(String paymentId);

    /**
     * 退款
     *
     * @param paymentId 支付ID
     * @return 是否退款成功
     */
    boolean refund(String paymentId);
}
