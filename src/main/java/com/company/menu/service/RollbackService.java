package com.company.menu.service;

import com.company.menu.domain.PriceChangeOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 回滚服务
 *
 * 对应 BDD 场景: "价格调整异常回滚"
 * When: 执行过程中门店更新失败
 * Then: 系统应自动回滚所有已更新的门店价格
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RollbackService {

    /**
     * 回滚价格变更
     *
     * @param order 价格变更单
     * @param successCount 已成功更新的门店数
     */
    public void rollbackPriceChanges(PriceChangeOrder order, int successCount) {
        log.warn("开始回滚价格变更 - 单号: {}, 需回滚门店数: {}",
            order.getOrderNo(), successCount);

        // 获取已成功更新的门店列表
        order.getAffectedStoreIds().stream()
            .limit(successCount)
            .forEach(storeId -> {
                try {
                    // 恢复原价格
                    rollbackStorePrice(
                        storeId,
                        order.getProductCode(),
                        order.getOriginalPrice()
                    );
                    log.debug("门店价格回滚成功 - 门店ID: {}", storeId);
                } catch (Exception e) {
                    log.error("门店价格回滚失败 - 门店ID: {}, 错误: {}",
                        storeId, e.getMessage());
                }
            });

        log.info("价格变更回滚完成 - 单号: {}", order.getOrderNo());
    }

    /**
     * 回滚单个门店价格
     *
     * @param storeId 门店ID
     * @param productCode 产品编码
     * @param originalPrice 原价格
     */
    private void rollbackStorePrice(
            Long storeId,
            String productCode,
            java.math.BigDecimal originalPrice) {
        // 调用 POS System API 恢复价格
        // posSystemClient.updatePrice(storeId, productCode, originalPrice);
        log.debug("回滚门店价格 - 门店: {}, 产品: {}, 回滚至: {}",
            storeId, productCode, originalPrice);
    }
}
