package com.company.menu.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单领域模型
 *
 * 演示如何在业务流程中依赖外部服务
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 数量
     */
    private int quantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 积分抵扣金额
     */
    private BigDecimal pointsDiscount;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 支付ID
     */
    private String paymentId;

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        /**
         * 待支付
         */
        PENDING_PAYMENT("待支付"),

        /**
         * 已支付
         */
        PAID("已支付"),

        /**
         * 支付处理中
         */
        PAYMENT_PROCESSING("支付处理中"),

        /**
         * 已取消
         */
        CANCELLED("已取消"),

        /**
         * 已完成
         */
        COMPLETED("已完成");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 业务方法：完成支付
     */
    public void completePay(String paymentId) {
        if (this.status != OrderStatus.PENDING_PAYMENT &&
            this.status != OrderStatus.PAYMENT_PROCESSING) {
            throw new IllegalStateException("当前订单状态不允许支付");
        }

        this.paymentId = paymentId;
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 业务方法：取消订单
     */
    public void cancel() {
        if (this.status == OrderStatus.PAID || this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("已支付或已完成的订单不能取消");
        }

        this.status = OrderStatus.CANCELLED;
    }

    /**
     * 业务方法：设置支付处理中
     */
    public void setPaymentProcessing() {
        this.status = OrderStatus.PAYMENT_PROCESSING;
    }
}
