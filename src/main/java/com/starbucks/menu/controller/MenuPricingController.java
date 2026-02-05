package com.starbucks.menu.controller;

import com.starbucks.menu.domain.PriceChangeOrder;
import com.starbucks.menu.domain.PriceHistory;
import com.starbucks.menu.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单价格管理 REST API
 *
 * 对应 BDD 场景: behaviors/menu/price_update.feature
 *
 * API 端点:
 * - POST   /api/v1/pricing/change-orders          创建价格变更单
 * - POST   /api/v1/pricing/change-orders/batch    批量创建价格变更单
 * - POST   /api/v1/pricing/change-orders/{id}/approve  审批变更单
 * - POST   /api/v1/pricing/change-orders/{id}/execute  执行变更
 * - GET    /api/v1/pricing/history                查询价格历史
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
@Validated
@Tag(name = "菜单价格管理", description = "Menu Pricing Management APIs")
public class MenuPricingController {

    private final PricingService pricingService;

    /**
     * 创建价格变更单
     *
     * 对应 BDD 场景: "单一区域价格上调"
     */
    @PostMapping("/change-orders")
    @Operation(summary = "创建价格变更单", description = "提交价格调整请求，生成待审批的价格变更单")
    public ResponseEntity<PriceChangeOrderResponse> createPriceChangeOrder(
            @Valid @RequestBody CreatePriceChangeOrderRequest request) {

        log.info("接收到创建价格变更单请求 - 产品: {}, 区域: {}",
            request.getProductName(), request.getTargetRegion());

        PriceChangeOrder order = pricingService.createPriceChangeOrder(
            request.getProductCode(),
            request.getProductName(),
            request.getSpecification(),
            request.getTargetRegion(),
            request.getAdjustmentType(),
            request.getAdjustmentAmount(),
            request.getEffectiveDate(),
            request.getChangeReason(),
            request.getCreatedBy(),
            request.getAffectedStoreIds()
        );

        return ResponseEntity.ok(PriceChangeOrderResponse.from(order));
    }

    /**
     * 批量创建价格变更单
     *
     * 对应 BDD 场景: "多区域差异化定价"
     */
    @PostMapping("/change-orders/batch")
    @Operation(summary = "批量创建价格变更单", description = "支持多产品多区域的差异化定价")
    public ResponseEntity<List<PriceChangeOrderResponse>> createBatchPriceChangeOrders(
            @Valid @RequestBody PricingService.BatchPriceChangeRequest request) {

        log.info("接收到批量创建价格变更单请求 - 产品数: {}, 区域数: {}",
            request.getProducts().size(),
            request.getRegionalPrices().size());

        List<PriceChangeOrder> orders = pricingService.createBatchPriceChangeOrders(request);

        List<PriceChangeOrderResponse> responses = orders.stream()
            .map(PriceChangeOrderResponse::from)
            .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * 审批价格变更单
     *
     * 对应 BDD 场景: "价格变更审批通过后自动生效"
     */
    @PostMapping("/change-orders/{orderNo}/approve")
    @Operation(summary = "审批价格变更单", description = "审批人通过价格变更单")
    public ResponseEntity<PriceChangeOrderResponse> approvePriceChangeOrder(
            @PathVariable @Parameter(description = "变更单号") String orderNo,
            @RequestParam @Parameter(description = "审批人") String approver) {

        log.info("接收到审批请求 - 单号: {}, 审批人: {}", orderNo, approver);

        PriceChangeOrder order = pricingService.approvePriceChangeOrder(orderNo, approver);

        return ResponseEntity.ok(PriceChangeOrderResponse.from(order));
    }

    /**
     * 执行价格变更
     *
     * 对应 BDD 场景: "价格变更审批通过后自动生效"
     */
    @PostMapping("/change-orders/{orderNo}/execute")
    @Operation(summary = "执行价格变更", description = "在生效时间到达后执行价格变更")
    public ResponseEntity<ExecutionResult> executePriceChange(
            @PathVariable @Parameter(description = "变更单号") String orderNo) {

        log.info("接收到执行价格变更请求 - 单号: {}", orderNo);

        boolean success = pricingService.executePriceChange(orderNo);

        return ResponseEntity.ok(new ExecutionResult(
            success,
            success ? "价格变更执行成功" : "价格变更执行失败，已自动回滚"
        ));
    }

    /**
     * 查询价格历史
     *
     * 对应 BDD 场景: "价格变更历史查询"
     */
    @GetMapping("/history")
    @Operation(summary = "查询价格历史", description = "查询产品在特定区域的价格变更历史")
    public ResponseEntity<List<PriceHistoryResponse>> queryPriceHistory(
            @RequestParam @Parameter(description = "产品编码") String productCode,
            @RequestParam @Parameter(description = "区域代码") String regionCode) {

        log.info("接收到价格历史查询请求 - 产品: {}, 区域: {}", productCode, regionCode);

        List<PriceHistory> histories = pricingService.queryPriceHistory(productCode, regionCode);

        List<PriceHistoryResponse> responses = histories.stream()
            .map(PriceHistoryResponse::from)
            .toList();

        return ResponseEntity.ok(responses);
    }

    // ==================== Request/Response DTOs ====================

    /**
     * 创建价格变更单请求
     */
    @lombok.Data
    public static class CreatePriceChangeOrderRequest {
        private String productCode;
        private String productName;
        private String specification;
        private String targetRegion;
        private PriceChangeOrder.AdjustmentType adjustmentType;
        private BigDecimal adjustmentAmount;
        private LocalDateTime effectiveDate;
        private String changeReason;
        private String createdBy;
        private List<Long> affectedStoreIds;
    }

    /**
     * 价格变更单响应
     */
    @lombok.Data
    @lombok.Builder
    public static class PriceChangeOrderResponse {
        private String orderNo;
        private String productName;
        private String targetRegion;
        private BigDecimal originalPrice;
        private BigDecimal newPrice;
        private BigDecimal adjustmentAmount;
        private String status;
        private Integer affectedStoreCount;
        private LocalDateTime effectiveDate;
        private String createdBy;
        private LocalDateTime createdAt;

        public static PriceChangeOrderResponse from(PriceChangeOrder order) {
            return PriceChangeOrderResponse.builder()
                .orderNo(order.getOrderNo())
                .productName(order.getProductName())
                .targetRegion(order.getTargetRegion())
                .originalPrice(order.getOriginalPrice())
                .newPrice(order.getNewPrice())
                .adjustmentAmount(order.getAdjustmentAmount())
                .status(order.getStatus().getDescription())
                .affectedStoreCount(order.getAffectedStoreCount())
                .effectiveDate(order.getEffectiveDate())
                .createdBy(order.getCreatedBy())
                .createdAt(order.getCreatedAt())
                .build();
        }
    }

    /**
     * 价格历史响应
     */
    @lombok.Data
    @lombok.Builder
    public static class PriceHistoryResponse {
        private String productName;
        private String regionName;
        private BigDecimal originalPrice;
        private BigDecimal newPrice;
        private String priceChangeDescription;
        private LocalDateTime effectiveTime;
        private String changeReason;
        private String approvedBy;

        public static PriceHistoryResponse from(PriceHistory history) {
            return PriceHistoryResponse.builder()
                .productName(history.getProductName())
                .regionName(history.getRegionName())
                .originalPrice(history.getOriginalPrice())
                .newPrice(history.getNewPrice())
                .priceChangeDescription(history.getPriceChangeDescription())
                .effectiveTime(history.getEffectiveTime())
                .changeReason(history.getChangeReason())
                .approvedBy(history.getApprovedBy())
                .build();
        }
    }

    /**
     * 执行结果响应
     */
    public record ExecutionResult(boolean success, String message) {}
}
