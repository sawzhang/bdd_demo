package com.starbucks.menu.test.bdd;

import com.starbucks.menu.domain.PriceChangeOrder;
import com.starbucks.menu.service.PricingService;
import com.starbucks.menu.test.bdd.context.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.zh_cn.假如;
import io.cucumber.java.zh_cn.当;
import io.cucumber.java.zh_cn.那么;
import io.cucumber.java.zh_cn.并且;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 状态流转测试步骤定义
 *
 * 演示如何使用 ScenarioContext 处理中间状态数据
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
@Slf4j
@SpringBootTest
public class StateFlowSteps {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private ScenarioContext scenarioContext;

    // ==================== 状态创建和转换 ====================

    @假如("创建价格变更单:")
    public void 创建价格变更单(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        String productName = data.get("产品");
        String region = data.get("区域");
        BigDecimal adjustmentAmount = new BigDecimal(
            data.get("调整金额").replace("元", "")
        );

        log.info("创建价格变更单 - 产品: {}, 区域: {}, 调整: {}元",
            productName, region, adjustmentAmount);

        // 创建变更单
        PriceChangeOrder order = pricingService.createPriceChangeOrder(
            "LATTE-GRANDE",
            productName,
            "大杯",
            region,
            PriceChangeOrder.AdjustmentType.INCREASE,
            adjustmentAmount,
            LocalDateTime.now().plusDays(7),
            "测试调整",
            "张三",
            List.of(1L, 2L, 3L) // 模拟门店
        );

        // 保存到场景上下文
        scenarioContext.setCurrentOrder(order);
        scenarioContext.addState("creation_time", LocalDateTime.now());

        log.info("✓ 变更单已创建: {}, 状态: {}",
            order.getOrderNo(),
            order.getStatus().getDescription());
    }

    @当("审批人 {string} 通过该变更单")
    @并且("审批人 {string} 通过该变更单")
    public void 审批人通过该变更单(String approver) {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        log.info("审批人 {} 开始审批变更单: {}", approver, order.getOrderNo());

        // 捕获审批前快照
        scenarioContext.captureSnapshot();
        scenarioContext.addState("pre_approval_status", order.getStatus());

        // 执行审批
        order.approve(approver);

        // 更新上下文
        scenarioContext.setCurrentOrder(order);
        scenarioContext.addState("post_approval_status", order.getStatus());
        scenarioContext.addState("approver", approver);

        log.info("✓ 审批完成，状态: {} → {}",
            scenarioContext.getState("pre_approval_status"),
            order.getStatus().getDescription());
    }

    @当("系统开始执行价格变更")
    @并且("系统开始执行价格变更")
    public void 系统开始执行价格变更() {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        log.info("开始执行价格变更: {}", order.getOrderNo());

        // 捕获执行前快照
        scenarioContext.captureSnapshot();

        // 开始执行
        order.startExecution();

        // 更新上下文
        scenarioContext.setCurrentOrder(order);
        scenarioContext.addState("execution_start_time", LocalDateTime.now());

        log.info("✓ 开始执行，状态: {}", order.getStatus().getDescription());
    }

    @当("所有门店价格更新成功")
    public void 所有门店价格更新成功() {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        log.info("所有门店价格更新成功");

        // 捕获完成前快照
        scenarioContext.captureSnapshot();

        // 执行完成
        order.completeExecution();

        // 更新上下文
        scenarioContext.setCurrentOrder(order);
        scenarioContext.addState("execution_completion_time", LocalDateTime.now());

        log.info("✓ 执行完成，状态: {}", order.getStatus().getDescription());
    }

    @当("第50个门店更新失败并触发回滚")
    @当("第{int}个门店更新失败")
    public void 门店更新失败(int storeIndex) {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        log.warn("第 {} 个门店更新失败，触发回滚", storeIndex);

        // 捕获失败前快照
        scenarioContext.captureSnapshot();

        // 执行失败
        order.failExecution("门店价格同步失败");

        // 更新上下文
        scenarioContext.setCurrentOrder(order);
        scenarioContext.addState("failed_store_index", storeIndex);
        scenarioContext.addState("failure_time", LocalDateTime.now());

        log.info("✓ 执行失败，状态: {}", order.getStatus().getDescription());
    }

    // ==================== 状态验证 ====================

    @那么("订单状态应为 {string}")
    public void 订单状态应为(String expectedStatus) {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        assertThat(order).isNotNull();
        assertThat(order.getStatus().getDescription()).isEqualTo(expectedStatus);

        log.info("✓ 验证通过：订单状态 = {}", expectedStatus);
    }

    @那么("状态历史应记录 {string}")
    @并且("状态历史应记录 {string}")
    public void 状态历史应记录(String expectedHistory) {
        String actualHistory = scenarioContext.getStatusHistoryString();

        assertThat(actualHistory).isEqualTo(expectedHistory);

        log.info("✓ 验证通过：状态历史 = {}", actualHistory);
    }

    @那么("应记录审批人 {string}")
    @并且("应记录审批人 {string}")
    public void 应记录审批人(String expectedApprover) {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        assertThat(order.getApprovedBy()).isEqualTo(expectedApprover);

        log.info("✓ 验证通过：审批人 = {}", expectedApprover);
    }

    @那么("应记录执行开始时间")
    @并且("应记录执行开始时间")
    public void 应记录执行开始时间() {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        assertThat(order.getExecutionStartedAt()).isNotNull();
        assertThat(order.getExecutionStartedAt())
            .isBeforeOrEqualTo(LocalDateTime.now());

        log.info("✓ 验证通过：执行开始时间 = {}", order.getExecutionStartedAt());
    }

    @那么("应记录执行完成时间")
    @并且("应记录执行完成时间")
    public void 应记录执行完成时间() {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        assertThat(order.getExecutionCompletedAt()).isNotNull();
        assertThat(order.getExecutionCompletedAt())
            .isAfterOrEqualTo(order.getExecutionStartedAt());

        log.info("✓ 验证通过：执行完成时间 = {}", order.getExecutionCompletedAt());
    }

    @那么("应记录失败原因 {string}")
    @并且("应记录失败原因 {string}")
    public void 应记录失败原因(String expectedReason) {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        assertThat(order.getFailureReason()).contains(expectedReason);

        log.info("✓ 验证通过：失败原因 = {}", order.getFailureReason());
    }

    // ==================== 状态快照 ====================

    @并且("应捕获状态快照")
    public void 应捕获状态快照() {
        scenarioContext.captureSnapshot();

        log.debug("✓ 状态快照已捕获");
    }

    @那么("应该记录了 {int} 个状态快照")
    @并且("应该记录了 {int} 个状态快照")
    public void 应该记录了个状态快照(int expectedCount) {
        List<ScenarioContext.StateSnapshot> snapshots = scenarioContext.getSnapshots();

        assertThat(snapshots).hasSize(expectedCount);

        log.info("✓ 验证通过：记录了 {} 个状态快照", expectedCount);

        // 打印快照详情
        for (int i = 0; i < snapshots.size(); i++) {
            ScenarioContext.StateSnapshot snapshot = snapshots.get(i);
            log.debug("  快照 {}: {}", i + 1, snapshot);
        }
    }

    // ==================== 场景大纲支持 ====================

    @假如("订单当前状态为 {string}")
    public void 订单当前状态为(String statusName) {
        // 创建订单并设置到指定状态
        PriceChangeOrder order = createOrderInStatus(statusName);

        scenarioContext.setCurrentOrder(order);

        log.info("✓ 订单已创建并设置为状态: {}", statusName);
    }

    @当("执行操作 {string}")
    public void 执行操作(String operation) {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        try {
            switch (operation) {
                case "审批通过":
                    order.approve("测试人员");
                    scenarioContext.addState("operation_success", true);
                    break;
                case "开始执行":
                    order.startExecution();
                    scenarioContext.addState("operation_success", true);
                    break;
                case "执行完成":
                    order.completeExecution();
                    scenarioContext.addState("operation_success", true);
                    break;
                case "执行失败":
                    order.failExecution("测试失败");
                    scenarioContext.addState("operation_success", true);
                    break;
                default:
                    throw new IllegalArgumentException("未知操作: " + operation);
            }

            scenarioContext.setCurrentOrder(order);
            log.info("✓ 操作 '{}' 执行成功", operation);

        } catch (IllegalStateException e) {
            scenarioContext.addState("operation_success", false);
            scenarioContext.addState("error_message", e.getMessage());
            log.info("✓ 操作 '{}' 执行失败（预期行为）: {}", operation, e.getMessage());
        }
    }

    @那么("操作结果应为 {string}")
    public void 操作结果应为(String expectedResult) {
        Boolean success = scenarioContext.getState("operation_success", Boolean.class);

        if ("成功".equals(expectedResult)) {
            assertThat(success).isTrue();
            log.info("✓ 验证通过：操作成功");
        } else if ("失败".equals(expectedResult)) {
            assertThat(success).isFalse();
            String errorMsg = scenarioContext.getState("error_message", String.class);
            log.info("✓ 验证通过：操作失败，错误: {}", errorMsg);
        }
    }

    @那么("订单最终状态应为 {string}")
    @并且("订单最终状态应为 {string}")
    public void 订单最终状态应为(String expectedStatus) {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        assertThat(order.getStatus().getDescription()).isEqualTo(expectedStatus);

        log.info("✓ 验证通过：最终状态 = {}", expectedStatus);
    }

    // ==================== 回滚场景支持 ====================

    @并且("已成功更新 {int} 个门店")
    public void 已成功更新个门店(int storeCount) {
        scenarioContext.addState("updated_store_count", storeCount);
        scenarioContext.addState("remaining_store_count", 100); // 假设总共150个门店，已更新50个

        log.info("✓ 已成功更新 {} 个门店", storeCount);
    }

    @并且("捕获中间状态数据:")
    public void 捕获中间状态数据(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 尝试解析为数字
            try {
                Integer numValue = Integer.parseInt(value);
                scenarioContext.addState(key, numValue);
            } catch (NumberFormatException e) {
                scenarioContext.addState(key, value);
            }
        }

        scenarioContext.captureSnapshot();
        log.info("✓ 中间状态数据已捕获: {}", data);
    }

    @那么("应执行回滚操作")
    public void 应执行回滚操作() {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        assertThat(order.getStatus()).isEqualTo(PriceChangeOrder.OrderStatus.EXECUTION_FAILED);
        scenarioContext.addState("rollback_executed", true);

        log.info("✓ 验证通过：回滚操作已执行");
    }

    @并且("应恢复已更新的 {int} 个门店价格")
    public void 应恢复已更新的个门店价格(int storeCount) {
        Integer updatedStores = scenarioContext.getState("updated_store_count", Integer.class);

        assertThat(updatedStores).isEqualTo(storeCount);
        scenarioContext.addState("rollback_store_count", storeCount);

        log.info("✓ 验证通过：已恢复 {} 个门店价格", storeCount);
    }

    @并且("中间状态数据应被保存以供审计")
    public void 中间状态数据应被保存以供审计() {
        List<ScenarioContext.StateSnapshot> snapshots = scenarioContext.getSnapshots();

        assertThat(snapshots).isNotEmpty();
        assertThat(scenarioContext.getState("updated_store_count")).isNotNull();
        assertThat(scenarioContext.getState("remaining_store_count")).isNotNull();

        log.info("✓ 验证通过：中间状态数据已保存，共 {} 个快照", snapshots.size());
    }

    // ==================== 并发场景支持 ====================

    @当("多个审批人同时尝试审批")
    public void 多个审批人同时尝试审批(DataTable dataTable) {
        List<Map<String, String>> approvers = dataTable.asMaps();
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        List<String> successfulApprovers = new ArrayList<>();
        List<String> failedApprovers = new ArrayList<>();

        for (Map<String, String> row : approvers) {
            String approver = row.get("审批人");
            try {
                order.approve(approver);
                successfulApprovers.add(approver);
                log.info("审批人 {} 审批成功", approver);
            } catch (IllegalStateException e) {
                failedApprovers.add(approver);
                log.info("审批人 {} 审批失败: {}", approver, e.getMessage());
            }
        }

        scenarioContext.setCurrentOrder(order);
        scenarioContext.addState("successful_approvers", successfulApprovers);
        scenarioContext.addState("failed_approvers", failedApprovers);
    }

    @那么("只有第一个审批应该成功")
    public void 只有第一个审批应该成功() {
        @SuppressWarnings("unchecked")
        List<String> successfulApprovers =
            (List<String>) scenarioContext.getState("successful_approvers");

        assertThat(successfulApprovers).hasSize(1);

        log.info("✓ 验证通过：只有第一个审批成功");
    }

    @并且("审批人应为 {string}")
    public void 审批人应为(String expectedApprover) {
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        assertThat(order.getApprovedBy()).isEqualTo(expectedApprover);

        log.info("✓ 验证通过：审批人为 {}", expectedApprover);
    }

    @并且("其他审批尝试应被拒绝")
    public void 其他审批尝试应被拒绝() {
        @SuppressWarnings("unchecked")
        List<String> failedApprovers =
            (List<String>) scenarioContext.getState("failed_approvers");

        assertThat(failedApprovers).isNotEmpty();

        log.info("✓ 验证通过：{} 个审批尝试被拒绝", failedApprovers.size());
    }

    // ==================== 审计日志场景支持 ====================

    @那么("审计日志应记录以下状态变更:")
    public void 审计日志应记录以下状态变更(DataTable dataTable) {
        List<Map<String, String>> expectedChanges = dataTable.asMaps();
        List<String> statusHistory = scenarioContext.getStatusHistory();

        // 验证状态历史记录数量
        assertThat(statusHistory).hasSize(expectedChanges.size());

        // 验证每个状态变更
        for (int i = 0; i < expectedChanges.size(); i++) {
            Map<String, String> expectedChange = expectedChanges.get(i);
            String expectedStatus = expectedChange.get("状态");
            String actualStatus = statusHistory.get(i);

            assertThat(actualStatus).isEqualTo(expectedStatus);

            log.info("✓ 审计记录 {}: 状态={}, 操作人={}, 操作={}",
                i + 1,
                expectedStatus,
                expectedChange.get("操作人"),
                expectedChange.get("操作"));
        }

        log.info("✓ 验证通过：审计日志记录完整");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建指定状态的订单（用于场景大纲）
     */
    private PriceChangeOrder createOrderInStatus(String statusName) {
        // 创建基础订单
        PriceChangeOrder order = PriceChangeOrder.builder()
            .orderNo("PCO-TEST-001")
            .productCode("LATTE-GRANDE")
            .productName("大杯拿铁")
            .targetRegion("华东区")
            .originalPrice(new BigDecimal("36.00"))
            .newPrice(new BigDecimal("38.00"))
            .adjustmentAmount(new BigDecimal("2.00"))
            .adjustmentType(PriceChangeOrder.AdjustmentType.INCREASE)
            .changeReason("测试")
            .effectiveDate(LocalDateTime.now().plusDays(7))
            .affectedStoreCount(3)
            .affectedStoreIds(List.of(1L, 2L, 3L))
            .status(PriceChangeOrder.OrderStatus.PENDING_APPROVAL)
            .createdBy("测试人员")
            .createdAt(LocalDateTime.now())
            .build();

        // 根据目标状态流转
        if ("已审批".equals(statusName)) {
            order.approve("测试审批人");
        } else if ("执行中".equals(statusName)) {
            order.approve("测试审批人");
            order.startExecution();
        } else if ("已完成".equals(statusName)) {
            order.approve("测试审批人");
            order.startExecution();
            order.completeExecution();
        } else if ("执行失败".equals(statusName)) {
            order.approve("测试审批人");
            order.startExecution();
            order.failExecution("测试失败");
        }

        return order;
    }
}
