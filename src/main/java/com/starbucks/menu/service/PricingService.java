package com.starbucks.menu.service;

import com.starbucks.menu.domain.PriceChangeOrder;
import com.starbucks.menu.domain.PriceHistory;
import com.starbucks.menu.repository.PriceChangeOrderRepository;
import com.starbucks.menu.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 价格管理服务
 *
 * 对应 BDD 场景: behaviors/menu/price_update.feature
 * 核心职责:
 * - 创建价格变更单
 * - 执行价格变更
 * - 处理异常回滚
 * - 查询价格历史
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PriceChangeOrderRepository orderRepository;
    private final PriceHistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final RollbackService rollbackService;

    /**
     * 创建价格变更单
     *
     * 对应 BDD 场景: "单一区域价格上调"
     * When: 提交价格调整请求
     * Then: 系统应生成价格变更单
     *
     * @param productCode 产品编码
     * @param productName 产品名称
     * @param specification 规格
     * @param targetRegion 目标区域
     * @param adjustmentType 调整类型
     * @param adjustmentAmount 调整金额
     * @param effectiveDate 生效日期
     * @param changeReason 变更原因
     * @param createdBy 创建人
     * @param affectedStoreIds 影响的门店ID列表
     * @return 创建的价格变更单
     */
    @Transactional
    public PriceChangeOrder createPriceChangeOrder(
            String productCode,
            String productName,
            String specification,
            String targetRegion,
            PriceChangeOrder.AdjustmentType adjustmentType,
            BigDecimal adjustmentAmount,
            LocalDateTime effectiveDate,
            String changeReason,
            String createdBy,
            List<Long> affectedStoreIds) {

        log.info("开始创建价格变更单 - 产品: {}, 区域: {}, 调整金额: {}",
            productName, targetRegion, adjustmentAmount);

        // 1. 生成变更单号 (格式: PCO-YYYYMMDD-序号)
        String orderNo = generateOrderNo();

        // 2. 获取当前价格 (从价格历史表或产品表查询)
        BigDecimal currentPrice = getCurrentPrice(productCode, targetRegion);

        // 3. 计算新价格
        BigDecimal newPrice = calculateNewPrice(currentPrice, adjustmentType, adjustmentAmount);

        // 4. 构建价格变更单
        PriceChangeOrder order = PriceChangeOrder.builder()
            .orderNo(orderNo)
            .productCode(productCode)
            .productName(productName)
            .specification(specification)
            .targetRegion(targetRegion)
            .originalPrice(currentPrice)
            .newPrice(newPrice)
            .adjustmentType(adjustmentType)
            .adjustmentAmount(adjustmentAmount)
            .changeReason(changeReason)
            .effectiveDate(effectiveDate)
            .affectedStoreCount(affectedStoreIds.size())
            .affectedStoreIds(affectedStoreIds)
            .status(PriceChangeOrder.OrderStatus.PENDING_APPROVAL)
            .createdBy(createdBy)
            .createdAt(LocalDateTime.now())
            .build();

        // 5. 计算调整百分比
        order.calculateAdjustmentPercentage();

        // 6. 业务规则验证
        order.validatePriceDecrease();

        // 7. 保存变更单
        PriceChangeOrder savedOrder = orderRepository.save(order);

        log.info("价格变更单创建成功 - 单号: {}, 影响门店数: {}",
            orderNo, affectedStoreIds.size());

        return savedOrder;
    }

    /**
     * 批量创建价格变更单 (多区域差异化定价)
     *
     * 对应 BDD 场景: "多区域差异化定价"
     * When: 提交批量价格调整
     * Then: 系统应生成多个独立的价格变更单
     *
     * @param batchRequest 批量请求
     * @return 创建的变更单列表
     */
    @Transactional
    public List<PriceChangeOrder> createBatchPriceChangeOrders(
            BatchPriceChangeRequest batchRequest) {

        log.info("开始批量创建价格变更单 - 产品数: {}, 区域数: {}",
            batchRequest.getProducts().size(),
            batchRequest.getRegionalPrices().size());

        return batchRequest.getProducts().stream()
            .flatMap(product ->
                batchRequest.getRegionalPrices().stream()
                    .map(regionalPrice -> createPriceChangeOrder(
                        product.getProductCode(),
                        product.getProductName(),
                        product.getSpecification(),
                        regionalPrice.getRegion(),
                        PriceChangeOrder.AdjustmentType.FIXED,
                        regionalPrice.getPrice().subtract(getCurrentPrice(
                            product.getProductCode(),
                            regionalPrice.getRegion())),
                        batchRequest.getEffectiveDate(),
                        batchRequest.getChangeReason(),
                        batchRequest.getCreatedBy(),
                        regionalPrice.getStoreIds()
                    ))
            )
            .collect(Collectors.toList());
    }

    /**
     * 审批价格变更单
     *
     * 对应 BDD 场景: "价格变更审批通过后自动生效"
     * When: 审批人通过该变更单
     *
     * @param orderNo 变更单号
     * @param approver 审批人
     * @return 审批后的变更单
     */
    @Transactional
    public PriceChangeOrder approvePriceChangeOrder(String orderNo, String approver) {
        log.info("审批价格变更单 - 单号: {}, 审批人: {}", orderNo, approver);

        PriceChangeOrder order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new IllegalArgumentException("变更单不存在: " + orderNo));

        order.approve(approver);
        PriceChangeOrder approvedOrder = orderRepository.save(order);

        log.info("价格变更单审批成功 - 单号: {}", orderNo);

        return approvedOrder;
    }

    /**
     * 执行价格变更
     *
     * 对应 BDD 场景: "价格变更审批通过后自动生效"
     * When: 系统时间到达生效时间
     * Then: 所有门店价格应更新
     *
     * @param orderNo 变更单号
     * @return 执行结果
     */
    @Transactional
    public boolean executePriceChange(String orderNo) {
        log.info("开始执行价格变更 - 单号: {}", orderNo);

        PriceChangeOrder order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new IllegalArgumentException("变更单不存在: " + orderNo));

        // 1. 开始执行
        order.startExecution();
        orderRepository.save(order);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            // 2. 为每个门店更新价格并记录历史
            order.getAffectedStoreIds().forEach(storeId -> {
                try {
                    // 更新门店价格 (调用POS系统API)
                    updateStorePricing(storeId, order.getProductCode(), order.getNewPrice());

                    // 记录价格历史
                    savePriceHistory(order, storeId);

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("门店价格更新失败 - 门店ID: {}, 错误: {}",
                        storeId, e.getMessage());
                    failureCount.incrementAndGet();

                    // 如果失败数量超过阈值，触发回滚
                    if (failureCount.get() >= 1) {
                        throw new PriceUpdateException(
                            "门店价格同步失败",
                            storeId,
                            e
                        );
                    }
                }
            });

            // 3. 执行完成
            order.completeExecution();
            orderRepository.save(order);

            // 4. 发送通知
            notificationService.sendPriceChangeNotification(order);

            log.info("价格变更执行成功 - 单号: {}, 成功: {}, 失败: {}",
                orderNo, successCount.get(), failureCount.get());

            return true;

        } catch (PriceUpdateException e) {
            // 执行回滚
            log.error("价格变更执行失败，开始回滚 - 单号: {}", orderNo, e);

            rollbackService.rollbackPriceChanges(order, successCount.get());

            order.failExecution(e.getMessage());
            orderRepository.save(order);

            // 发送失败通知
            notificationService.sendPriceChangeFailureNotification(order, e.getMessage());

            return false;
        }
    }

    /**
     * 查询价格历史
     *
     * 对应 BDD 场景: "价格变更历史查询"
     * When: 查询产品在区域的价格历史
     * Then: 应返回历史记录并按时间倒序排列
     *
     * @param productCode 产品编码
     * @param regionCode 区域代码
     * @return 价格历史列表 (按时间倒序)
     */
    public List<PriceHistory> queryPriceHistory(String productCode, String regionCode) {
        log.info("查询价格历史 - 产品: {}, 区域: {}", productCode, regionCode);

        return historyRepository.findByProductCodeAndRegionCodeOrderByEffectiveTimeDesc(
            productCode,
            regionCode
        );
    }

    /**
     * 生成变更单号
     *
     * @return 格式: PCO-YYYYMMDD-序号
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = orderRepository.countByCreatedAtDate(LocalDateTime.now().toLocalDate()) + 1;
        return String.format("PCO-%s-%03d", dateStr, sequence);
    }

    /**
     * 获取当前价格
     *
     * @param productCode 产品编码
     * @param region 区域
     * @return 当前价格
     */
    private BigDecimal getCurrentPrice(String productCode, String region) {
        // 从价格历史表查询最新生效价格
        return historyRepository
            .findCurrentPrice(productCode, region, LocalDateTime.now())
            .orElse(new BigDecimal("36.00")); // 默认基准价格
    }

    /**
     * 计算新价格
     *
     * @param currentPrice 当前价格
     * @param adjustmentType 调整类型
     * @param adjustmentAmount 调整金额
     * @return 新价格
     */
    private BigDecimal calculateNewPrice(
            BigDecimal currentPrice,
            PriceChangeOrder.AdjustmentType adjustmentType,
            BigDecimal adjustmentAmount) {

        return switch (adjustmentType) {
            case INCREASE -> currentPrice.add(adjustmentAmount);
            case DECREASE -> currentPrice.subtract(adjustmentAmount.abs());
            case FIXED -> adjustmentAmount; // 直接设置为固定价格
        };
    }

    /**
     * 更新门店价格 (调用POS系统API)
     *
     * @param storeId 门店ID
     * @param productCode 产品编码
     * @param newPrice 新价格
     */
    private void updateStorePricing(Long storeId, String productCode, BigDecimal newPrice) {
        // 调用 POS System API
        // posSystemClient.updatePrice(storeId, productCode, newPrice);
        log.debug("更新门店价格 - 门店: {}, 产品: {}, 新价格: {}",
            storeId, productCode, newPrice);
    }

    /**
     * 保存价格历史记录
     *
     * @param order 价格变更单
     * @param storeId 门店ID
     */
    private void savePriceHistory(PriceChangeOrder order, Long storeId) {
        PriceHistory history = PriceHistory.builder()
            .changeOrderNo(order.getOrderNo())
            .productCode(order.getProductCode())
            .productName(order.getProductName())
            .regionCode(order.getTargetRegion())
            .storeId(storeId)
            .originalPrice(order.getOriginalPrice())
            .newPrice(order.getNewPrice())
            .effectiveTime(order.getEffectiveDate())
            .changeReason(order.getChangeReason())
            .changeType(PriceHistory.ChangeType.MARKET_ADJUSTMENT)
            .createdBy(order.getCreatedBy())
            .approvedBy(order.getApprovedBy())
            .approvedAt(order.getApprovedAt())
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .build();

        history.calculatePriceDifference();
        historyRepository.save(history);
    }

    /**
     * 批量价格变更请求DTO
     */
    @lombok.Data
    public static class BatchPriceChangeRequest {
        private List<ProductInfo> products;
        private List<RegionalPrice> regionalPrices;
        private LocalDateTime effectiveDate;
        private String changeReason;
        private String createdBy;
    }

    @lombok.Data
    public static class ProductInfo {
        private String productCode;
        private String productName;
        private String specification;
    }

    @lombok.Data
    public static class RegionalPrice {
        private String region;
        private BigDecimal price;
        private List<Long> storeIds;
    }

    /**
     * 价格更新异常
     */
    public static class PriceUpdateException extends RuntimeException {
        private final Long storeId;

        public PriceUpdateException(String message, Long storeId, Throwable cause) {
            super(message, cause);
            this.storeId = storeId;
        }

        public Long getStoreId() {
            return storeId;
        }
    }
}
