package com.company.menu.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格历史记录领域模型
 *
 * 对应 BDD 场景: behaviors/menu/price_update.feature
 * - 场景: 价格变更审批通过后自动生效
 * - 场景: 价格变更历史查询
 *
 * 用途:
 * - 追踪每次价格变更记录
 * - 支持价格历史查询和审计
 * - 提供价格趋势分析数据
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {

    /**
     * 历史记录ID
     */
    private Long id;

    /**
     * 关联的价格变更单号
     */
    @NotBlank(message = "变更单号不能为空")
    private String changeOrderNo;

    /**
     * 产品编码
     */
    @NotBlank(message = "产品编码不能为空")
    private String productCode;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    private String productName;

    /**
     * 区域代码
     */
    @NotBlank(message = "区域代码不能为空")
    private String regionCode;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 门店ID (如果是门店级别的价格变更)
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 原价格
     */
    @NotNull(message = "原价格不能为空")
    @DecimalMin(value = "0.00", message = "原价格不能为负数")
    private BigDecimal originalPrice;

    /**
     * 新价格
     */
    @NotNull(message = "新价格不能为空")
    @DecimalMin(value = "0.01", message = "新价格必须大于0")
    private BigDecimal newPrice;

    /**
     * 价格差额
     */
    private BigDecimal priceDifference;

    /**
     * 调整百分比
     */
    private BigDecimal adjustmentPercentage;

    /**
     * 生效时间
     */
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间 (被下一次价格变更覆盖的时间)
     */
    private LocalDateTime expiryTime;

    /**
     * 变更原因
     */
    @NotBlank(message = "变更原因不能为空")
    private String changeReason;

    /**
     * 变更类型
     */
    @NotNull(message = "变更类型不能为空")
    private ChangeType changeType;

    /**
     * 创建人
     */
    @NotBlank(message = "创建人不能为空")
    private String createdBy;

    /**
     * 审批人
     */
    private String approvedBy;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否当前生效
     */
    private Boolean isActive;

    /**
     * 备注
     */
    private String remark;

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        MARKET_ADJUSTMENT("市场调整"),
        COST_CHANGE("成本变动"),
        PROMOTION("促销活动"),
        SEASONAL("季节性调整"),
        COMPETITIVE("竞争性定价"),
        ROLLBACK("回滚");

        private final String description;

        ChangeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 计算价格差额和调整百分比
     */
    public void calculatePriceDifference() {
        if (originalPrice != null && newPrice != null) {
            this.priceDifference = newPrice.subtract(originalPrice);

            if (originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                this.adjustmentPercentage = priceDifference
                    .divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            }
        }
    }

    /**
     * 判断该价格是否在指定时间有效
     *
     * @param queryTime 查询时间
     * @return true if active at query time
     */
    public boolean isActiveAt(LocalDateTime queryTime) {
        if (queryTime == null) {
            queryTime = LocalDateTime.now();
        }

        boolean afterEffective = !queryTime.isBefore(effectiveTime);
        boolean beforeExpiry = expiryTime == null || queryTime.isBefore(expiryTime);

        return afterEffective && beforeExpiry;
    }

    /**
     * 使该价格记录失效
     *
     * @param expiryTime 失效时间
     */
    public void expire(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
        this.isActive = false;
    }

    /**
     * 获取价格变化描述
     *
     * @return 描述文本，例如: "从 36.00 上调至 38.00 (+5.56%)"
     */
    public String getPriceChangeDescription() {
        calculatePriceDifference();

        String direction = priceDifference.compareTo(BigDecimal.ZERO) > 0 ? "上调" : "下调";

        return String.format("从 %s %s至 %s (%s%.2f%%)",
            originalPrice,
            direction,
            newPrice,
            priceDifference.compareTo(BigDecimal.ZERO) > 0 ? "+" : "",
            adjustmentPercentage
        );
    }
}
