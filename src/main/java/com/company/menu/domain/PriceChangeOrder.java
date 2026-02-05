package com.company.menu.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格变更单领域模型
 *
 * 业务规则:
 * - 单次调整金额不能超过10元
 * - 价格下调不能超过原价30%
 * - 变更单生效前必须经过审批
 *
 * 对应 BDD 场景: behaviors/menu/price_update.feature
 * - 场景: 单一区域价格上调
 * - 场景: 价格变更审批通过后自动生效
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceChangeOrder {

    /**
     * 变更单号 (格式: PCO-YYYYMMDD-序号)
     * 例如: PCO-20260204-001
     */
    @NotBlank(message = "变更单号不能为空")
    @Pattern(regexp = "^PCO-\\d{8}-\\d{3}$", message = "变更单号格式不正确")
    private String orderNo;

    /**
     * 产品编码
     * 例如: LATTE-GRANDE (大杯拿铁)
     */
    @NotBlank(message = "产品编码不能为空")
    private String productCode;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    private String productName;

    /**
     * 产品规格
     * 例如: 大杯、中杯、小杯
     */
    private String specification;

    /**
     * 目标区域
     * 例如: 华东区、华北区、华南区
     */
    @NotBlank(message = "目标区域不能为空")
    private String targetRegion;

    /**
     * 原价格
     */
    @NotNull(message = "原价格不能为空")
    @DecimalMin(value = "0.01", message = "原价格必须大于0")
    private BigDecimal originalPrice;

    /**
     * 新价格
     */
    @NotNull(message = "新价格不能为空")
    @DecimalMin(value = "0.01", message = "新价格必须大于0")
    private BigDecimal newPrice;

    /**
     * 调整类型
     */
    @NotNull(message = "调整类型不能为空")
    private AdjustmentType adjustmentType;

    /**
     * 调整金额 (新价格 - 原价格)
     */
    @DecimalMax(value = "10.00", message = "单次调整金额不能超过10元")
    private BigDecimal adjustmentAmount;

    /**
     * 调整百分比
     */
    private BigDecimal adjustmentPercentage;

    /**
     * 变更原因
     */
    @NotBlank(message = "变更原因不能为空")
    private String changeReason;

    /**
     * 生效日期
     */
    @NotNull(message = "生效日期不能为空")
    @Future(message = "生效日期必须是未来时间")
    private LocalDateTime effectiveDate;

    /**
     * 影响的门店数量
     */
    @Min(value = 1, message = "至少影响1个门店")
    private Integer affectedStoreCount;

    /**
     * 影响的门店ID列表
     */
    private List<Long> affectedStoreIds;

    /**
     * 变更单状态
     */
    @NotNull(message = "状态不能为空")
    private OrderStatus status;

    /**
     * 创建人
     */
    @NotBlank(message = "创建人不能为空")
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 审批人
     */
    private String approvedBy;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 执行开始时间
     */
    private LocalDateTime executionStartedAt;

    /**
     * 执行完成时间
     */
    private LocalDateTime executionCompletedAt;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 调整类型枚举
     */
    public enum AdjustmentType {
        INCREASE("上调"),
        DECREASE("下调"),
        FIXED("固定价格");

        private final String description;

        AdjustmentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 变更单状态枚举
     *
     * 状态流转:
     * PENDING_APPROVAL → APPROVED → EXECUTING → COMPLETED
     *                             ↘ FAILED
     */
    public enum OrderStatus {
        PENDING_APPROVAL("待审批"),
        APPROVED("已审批"),
        EXECUTING("执行中"),
        COMPLETED("已完成"),
        FAILED("执行失败"),
        CANCELLED("已取消");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 业务规则验证: 价格下调不能超过原价30%
     *
     * @return true if valid
     * @throws IllegalArgumentException if validation fails
     */
    public boolean validatePriceDecrease() {
        if (adjustmentType == AdjustmentType.DECREASE) {
            BigDecimal maxDecreaseAmount = originalPrice.multiply(new BigDecimal("0.30"));
            if (adjustmentAmount.abs().compareTo(maxDecreaseAmount) > 0) {
                throw new IllegalArgumentException("价格下调不能超过原价30%");
            }
        }
        return true;
    }

    /**
     * 计算调整百分比
     */
    public void calculateAdjustmentPercentage() {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.adjustmentPercentage = adjustmentAmount
                .divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        }
    }

    /**
     * 审批通过
     *
     * @param approver 审批人
     */
    public void approve(String approver) {
        if (this.status != OrderStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("只有待审批状态的变更单才能审批");
        }
        this.status = OrderStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 开始执行
     */
    public void startExecution() {
        if (this.status != OrderStatus.APPROVED) {
            throw new IllegalStateException("只有已审批的变更单才能执行");
        }
        this.status = OrderStatus.EXECUTING;
        this.executionStartedAt = LocalDateTime.now();
    }

    /**
     * 执行完成
     */
    public void completeExecution() {
        if (this.status != OrderStatus.EXECUTING) {
            throw new IllegalStateException("只有执行中的变更单才能标记为完成");
        }
        this.status = OrderStatus.COMPLETED;
        this.executionCompletedAt = LocalDateTime.now();
    }

    /**
     * 执行失败
     *
     * @param reason 失败原因
     */
    public void failExecution(String reason) {
        if (this.status != OrderStatus.EXECUTING) {
            throw new IllegalStateException("只有执行中的变更单才能标记为失败");
        }
        this.status = OrderStatus.FAILED;
        this.failureReason = reason;
        this.executionCompletedAt = LocalDateTime.now();
    }
}
