package com.company.menu.integration.dto;

/**
 * 支付状态
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
public enum PaymentStatus {

    /**
     * 待支付
     */
    PENDING("待支付"),

    /**
     * 支付中
     */
    PROCESSING("支付中"),

    /**
     * 支付成功
     */
    SUCCESS("支付成功"),

    /**
     * 支付失败
     */
    FAILED("支付失败"),

    /**
     * 已退款
     */
    REFUNDED("已退款");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
