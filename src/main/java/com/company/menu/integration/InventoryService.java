package com.company.menu.integration;

import com.company.menu.integration.dto.InventoryCheckResult;

/**
 * 库存服务接口（外部依赖）
 *
 * 模拟与库存管理系统的集成
 * 在 BDD 测试中使用 @MockBean 进行 Mock
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
public interface InventoryService {

    /**
     * 检查库存是否充足
     *
     * @param productCode 产品编码
     * @param quantity 需要数量
     * @return 库存检查结果
     */
    InventoryCheckResult checkInventory(String productCode, int quantity);

    /**
     * 锁定库存
     *
     * @param productCode 产品编码
     * @param quantity 锁定数量
     * @param orderId 订单ID
     * @return 是否锁定成功
     */
    boolean lockInventory(String productCode, int quantity, String orderId);

    /**
     * 释放库存
     *
     * @param orderId 订单ID
     */
    void releaseInventory(String orderId);

    /**
     * 扣减库存（订单完成后）
     *
     * @param orderId 订单ID
     */
    void deductInventory(String orderId);
}
