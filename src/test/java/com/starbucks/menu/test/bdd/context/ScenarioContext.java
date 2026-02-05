package com.starbucks.menu.test.bdd.context;

import com.starbucks.menu.domain.PriceChangeOrder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BDD 场景上下文
 *
 * 用于在测试步骤之间共享数据和状态
 * 解决中间状态数据传递问题
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
@Component
@Scope("cucumber-glue") // Cucumber 专用作用域，每个场景一个实例
public class ScenarioContext {

    // 当前场景的价格变更单
    private PriceChangeOrder currentOrder;

    // 状态历史记录
    private List<String> statusHistory = new ArrayList<>();

    // 中间状态数据存储
    private Map<String, Object> stateData = new HashMap<>();

    // 状态快照列表
    private List<StateSnapshot> snapshots = new ArrayList<>();

    /**
     * 保存当前订单并记录状态
     */
    public void setCurrentOrder(PriceChangeOrder order) {
        this.currentOrder = order;

        // 记录状态到历史
        if (order != null && order.getStatus() != null) {
            addStatusToHistory(order.getStatus().getDescription());
        }
    }

    public PriceChangeOrder getCurrentOrder() {
        return this.currentOrder;
    }

    /**
     * 添加状态到历史记录
     */
    public void addStatusToHistory(String status) {
        if (!statusHistory.contains(status)) {
            statusHistory.add(status);
        }
    }

    /**
     * 获取状态历史
     */
    public List<String> getStatusHistory() {
        return new ArrayList<>(statusHistory);
    }

    /**
     * 获取状态历史字符串（用箭头连接）
     */
    public String getStatusHistoryString() {
        return String.join(" → ", statusHistory);
    }

    /**
     * 保存任意键值对数据
     */
    public void addState(String key, Object value) {
        stateData.put(key, value);
    }

    /**
     * 获取保存的状态数据
     */
    public Object getState(String key) {
        return stateData.get(key);
    }

    /**
     * 获取保存的状态数据（带类型转换）
     */
    @SuppressWarnings("unchecked")
    public <T> T getState(String key, Class<T> type) {
        Object value = stateData.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * 捕获当前订单的状态快照
     */
    public void captureSnapshot() {
        if (currentOrder != null) {
            snapshots.add(StateSnapshot.capture(currentOrder));
        }
    }

    /**
     * 获取所有快照
     */
    public List<StateSnapshot> getSnapshots() {
        return new ArrayList<>(snapshots);
    }

    /**
     * 获取指定索引的快照
     */
    public StateSnapshot getSnapshotAt(int index) {
        if (index < 0 || index >= snapshots.size()) {
            return null;
        }
        return snapshots.get(index);
    }

    /**
     * 获取最后一个快照
     */
    public StateSnapshot getLastSnapshot() {
        if (snapshots.isEmpty()) {
            return null;
        }
        return snapshots.get(snapshots.size() - 1);
    }

    /**
     * 清理场景数据
     * 每个场景执行后调用
     */
    public void clear() {
        this.currentOrder = null;
        this.statusHistory.clear();
        this.stateData.clear();
        this.snapshots.clear();
    }

    /**
     * 状态快照内部类
     */
    public static class StateSnapshot {
        private final String orderNo;
        private final PriceChangeOrder.OrderStatus status;
        private final String statusDescription;
        private final java.time.LocalDateTime timestamp;
        private final Map<String, Object> metadata;

        private StateSnapshot(
                String orderNo,
                PriceChangeOrder.OrderStatus status,
                String statusDescription,
                java.time.LocalDateTime timestamp,
                Map<String, Object> metadata) {
            this.orderNo = orderNo;
            this.status = status;
            this.statusDescription = statusDescription;
            this.timestamp = timestamp;
            this.metadata = metadata;
        }

        public static StateSnapshot capture(PriceChangeOrder order) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("approvedBy", order.getApprovedBy());
            metadata.put("approvedAt", order.getApprovedAt());
            metadata.put("executionStartedAt", order.getExecutionStartedAt());
            metadata.put("executionCompletedAt", order.getExecutionCompletedAt());
            metadata.put("failureReason", order.getFailureReason());

            return new StateSnapshot(
                order.getOrderNo(),
                order.getStatus(),
                order.getStatus().getDescription(),
                java.time.LocalDateTime.now(),
                metadata
            );
        }

        // Getters
        public String getOrderNo() { return orderNo; }
        public PriceChangeOrder.OrderStatus getStatus() { return status; }
        public String getStatusDescription() { return statusDescription; }
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, Object> getMetadata() { return metadata; }

        @Override
        public String toString() {
            return String.format("Snapshot[%s, %s, %s]",
                orderNo, statusDescription, timestamp);
        }
    }
}
