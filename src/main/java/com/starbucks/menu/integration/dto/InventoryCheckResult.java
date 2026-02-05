package com.starbucks.menu.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存检查结果
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckResult {

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 是否充足
     */
    private boolean sufficient;

    /**
     * 可用库存数量
     */
    private int available;

    /**
     * 已锁定数量
     */
    private int locked;

    /**
     * 库存仓库
     */
    private String warehouse;
}
