# BDD 场景中间状态解决方案

## 问题描述

在 BDD 测试中，业务数据经常需要经历多个中间状态，如何优雅地处理这些状态转换是一个关键挑战。

### 典型场景

**价格变更单的状态流转**：
```
待审批 → 已审批 → 执行中 → 已完成
   ↓                  ↓
 已取消            执行失败
```

## 解决方案

### 方案1: 使用场景上下文 (Scenario Context)

**原理**: 在测试步骤之间共享数据，保持状态连续性

#### 实现步骤

**1. 创建场景上下文类**

```java
package com.company.menu.test.bdd.context;

import com.company.menu.domain.PriceChangeOrder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

/**
 * BDD 场景上下文
 * 用于在测试步骤之间共享数据和状态
 */
@Component
@Scope("cucumber-glue") // Cucumber 专用作用域
public class ScenarioContext {

    // 当前场景的价格变更单
    private PriceChangeOrder currentOrder;

    // 中间状态数据
    private Map<String, Object> stateData = new HashMap<>();

    // 保存当前订单
    public void setCurrentOrder(PriceChangeOrder order) {
        this.currentOrder = order;
        // 同时保存状态历史
        addState("order_" + order.getOrderNo(), order.getStatus());
    }

    public PriceChangeOrder getCurrentOrder() {
        return this.currentOrder;
    }

    // 保存任意状态数据
    public void addState(String key, Object value) {
        stateData.put(key, value);
    }

    public Object getState(String key) {
        return stateData.get(key);
    }

    // 清理场景数据（每个场景后执行）
    public void clear() {
        this.currentOrder = null;
        this.stateData.clear();
    }
}
```

**2. 在测试步骤中使用上下文**

```java
@Slf4j
@SpringBootTest
public class PriceUpdateSteps {

    @Autowired
    private ScenarioContext scenarioContext;

    @Autowired
    private PricingService pricingService;

    @当("提交价格调整请求并记录状态")
    public void 提交价格调整请求并记录状态(DataTable dataTable) {
        // 创建订单
        PriceChangeOrder order = pricingService.createPriceChangeOrder(...);

        // 保存到场景上下文
        scenarioContext.setCurrentOrder(order);
        scenarioContext.addState("initial_status", order.getStatus());

        log.info("当前订单状态: {}", order.getStatus());
    }

    @当("审批人通过该变更单")
    public void 审批人通过该变更单() {
        // 从上下文获取订单
        PriceChangeOrder order = scenarioContext.getCurrentOrder();

        // 记录审批前状态
        scenarioContext.addState("pre_approval_status", order.getStatus());

        // 执行审批
        order = pricingService.approvePriceChangeOrder(
            order.getOrderNo(),
            "审批人"
        );

        // 更新上下文
        scenarioContext.setCurrentOrder(order);
        scenarioContext.addState("post_approval_status", order.getStatus());

        log.info("审批后状态: {} → {}",
            scenarioContext.getState("pre_approval_status"),
            order.getStatus());
    }

    @那么("订单状态应从{string}变为{string}")
    public void 订单状态应从变为(String fromStatus, String toStatus) {
        String actualFromStatus = (String) scenarioContext.getState("pre_approval_status");
        String actualToStatus = scenarioContext.getCurrentOrder().getStatus().name();

        assertThat(actualFromStatus).isEqualTo(fromStatus);
        assertThat(actualToStatus).isEqualTo(toStatus);
    }
}
```

**3. 使用 Hooks 清理状态**

```java
package com.company.menu.test.bdd.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class CucumberHooks {

    @Autowired
    private ScenarioContext scenarioContext;

    @Before
    public void beforeScenario() {
        // 每个场景开始前清理
        scenarioContext.clear();
    }

    @After
    public void afterScenario() {
        // 每个场景结束后清理
        scenarioContext.clear();
    }
}
```

---

### 方案2: 使用背景 + 状态构建器

**原理**: 使用 Background 设置初始状态，用 Builder 模式构建中间状态

#### BDD 场景示例

```gherkin
# language: zh-CN

功能: 价格变更单状态流转测试

  背景: 初始化测试数据
    假如 系统中存在以下基础数据:
      | 区域   | 门店数量 |
      | 华东区 | 150     |
    并且 当前"大杯拿铁"的基准价格为36元

  场景: 完整的状态流转测试
    # 状态1: 待审批
    假如 创建价格变更单:
      | 产品     | 区域   | 调整金额 |
      | 大杯拿铁 | 华东区 | 2元      |
    那么 订单状态应为 "待审批"
    并且 状态历史应记录 "待审批"

    # 状态2: 已审批
    当 审批人 "李四" 通过该变更单
    那么 订单状态应为 "已审批"
    并且 状态历史应记录 "待审批 → 已审批"
    并且 应记录审批人 "李四"

    # 状态3: 执行中
    当 系统开始执行价格变更
    那么 订单状态应为 "执行中"
    并且 状态历史应记录 "待审批 → 已审批 → 执行中"
    并且 应记录执行开始时间

    # 状态4: 已完成
    当 所有门店价格更新成功
    那么 订单状态应为 "已完成"
    并且 状态历史应记录 "待审批 → 已审批 → 执行中 → 已完成"
    并且 应记录执行完成时间

  场景: 异常状态流转测试
    # 正常流转到执行中
    假如 存在 "执行中" 状态的价格变更单

    # 异常导致失败
    当 第50个门店更新失败
    那么 订单状态应为 "执行失败"
    并且 状态历史应记录 "待审批 → 已审批 → 执行中 → 执行失败"
    并且 应触发回滚流程
```

#### 测试步骤实现

```java
@假如("存在 {string} 状态的价格变更单")
public void 存在指定状态的价格变更单(String targetStatus) {
    // 使用状态构建器创建指定状态的订单
    PriceChangeOrder order = PriceChangeOrderBuilder
        .newOrder()
        .withProduct("大杯拿铁")
        .withRegion("华东区")
        .toStatus(OrderStatus.valueOf(targetStatus))  // 自动流转到目标状态
        .build();

    scenarioContext.setCurrentOrder(order);
}

@那么("状态历史应记录 {string}")
public void 状态历史应记录(String expectedHistory) {
    List<String> actualHistory = scenarioContext.getStatusHistory();
    String actualHistoryStr = String.join(" → ", actualHistory);

    assertThat(actualHistoryStr).isEqualTo(expectedHistory);
}
```

#### 状态构建器实现

```java
package com.company.menu.test.bdd.builders;

/**
 * 价格变更单测试构建器
 * 用于快速创建各种状态的测试数据
 */
public class PriceChangeOrderBuilder {

    private PriceChangeOrder order;
    private List<String> statusHistory = new ArrayList<>();

    public static PriceChangeOrderBuilder newOrder() {
        return new PriceChangeOrderBuilder();
    }

    public PriceChangeOrderBuilder withProduct(String productName) {
        // 设置产品信息
        return this;
    }

    public PriceChangeOrderBuilder withRegion(String region) {
        // 设置区域信息
        return this;
    }

    /**
     * 自动流转到目标状态
     */
    public PriceChangeOrderBuilder toStatus(OrderStatus targetStatus) {
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        statusHistory.add("待审批");

        if (targetStatus == OrderStatus.APPROVED ||
            targetStatus.ordinal() > OrderStatus.APPROVED.ordinal()) {
            order.approve("测试审批人");
            statusHistory.add("已审批");
        }

        if (targetStatus == OrderStatus.EXECUTING ||
            targetStatus.ordinal() > OrderStatus.EXECUTING.ordinal()) {
            order.startExecution();
            statusHistory.add("执行中");
        }

        if (targetStatus == OrderStatus.COMPLETED) {
            order.completeExecution();
            statusHistory.add("已完成");
        }

        if (targetStatus == OrderStatus.FAILED) {
            order.failExecution("测试失败");
            statusHistory.add("执行失败");
        }

        return this;
    }

    public PriceChangeOrder build() {
        // 保存状态历史到场景上下文
        scenarioContext.addState("status_history", statusHistory);
        return order;
    }
}
```

---

### 方案3: 使用场景大纲测试多状态

**原理**: 用场景大纲参数化测试不同的状态转换路径

#### BDD 场景

```gherkin
# language: zh-CN

场景大纲: 价格变更单状态转换验证
  假如 订单当前状态为 "<当前状态>"
  当 执行操作 "<操作>"
  那么 订单状态应变为 "<目标状态>"
  并且 状态转换应 "<是否成功>"

  例子: 正常状态转换
    | 当前状态 | 操作     | 目标状态 | 是否成功 |
    | 待审批   | 审批通过 | 已审批   | 成功     |
    | 已审批   | 开始执行 | 执行中   | 成功     |
    | 执行中   | 执行完成 | 已完成   | 成功     |

  例子: 异常状态转换
    | 当前状态 | 操作     | 目标状态 | 是否成功 |
    | 待审批   | 开始执行 | 待审批   | 失败     |
    | 执行中   | 审批通过 | 执行中   | 失败     |
    | 已完成   | 审批通过 | 已完成   | 失败     |
```

#### 测试步骤实现

```java
@假如("订单当前状态为 {string}")
public void 订单当前状态为(String statusName) {
    PriceChangeOrder order = PriceChangeOrderBuilder
        .newOrder()
        .toStatus(OrderStatus.fromName(statusName))
        .build();

    scenarioContext.setCurrentOrder(order);
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
        }
    } catch (IllegalStateException e) {
        // 状态转换失败
        scenarioContext.addState("operation_success", false);
        scenarioContext.addState("error_message", e.getMessage());
    }
}

@那么("状态转换应 {string}")
public void 状态转换应(String expectedResult) {
    boolean success = (boolean) scenarioContext.getState("operation_success");

    if ("成功".equals(expectedResult)) {
        assertThat(success).isTrue();
    } else {
        assertThat(success).isFalse();
    }
}
```

---

### 方案4: 使用状态快照

**原理**: 在关键状态点保存快照，便于回溯和验证

#### 实现

```java
public class StateSnapshot {
    private final String orderNo;
    private final OrderStatus status;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;

    public static StateSnapshot capture(PriceChangeOrder order) {
        return new StateSnapshot(
            order.getOrderNo(),
            order.getStatus(),
            LocalDateTime.now(),
            captureMetadata(order)
        );
    }

    private static Map<String, Object> captureMetadata(PriceChangeOrder order) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("approvedBy", order.getApprovedBy());
        metadata.put("executionStartedAt", order.getExecutionStartedAt());
        // ... 其他关键信息
        return metadata;
    }
}

// 在场景上下文中使用
public class ScenarioContext {
    private List<StateSnapshot> snapshots = new ArrayList<>();

    public void captureSnapshot(PriceChangeOrder order) {
        snapshots.add(StateSnapshot.capture(order));
    }

    public List<StateSnapshot> getSnapshots() {
        return snapshots;
    }

    public StateSnapshot getSnapshotAt(int index) {
        return snapshots.get(index);
    }
}
```

#### 使用示例

```java
@当("审批人通过该变更单")
public void 审批人通过该变更单() {
    PriceChangeOrder order = scenarioContext.getCurrentOrder();

    // 捕获审批前快照
    scenarioContext.captureSnapshot(order);

    // 执行审批
    order.approve("审批人");

    // 捕获审批后快照
    scenarioContext.captureSnapshot(order);

    scenarioContext.setCurrentOrder(order);
}

@那么("应记录状态变更历史")
public void 应记录状态变更历史() {
    List<StateSnapshot> snapshots = scenarioContext.getSnapshots();

    // 验证快照序列
    assertThat(snapshots).hasSize(2);
    assertThat(snapshots.get(0).getStatus()).isEqualTo(OrderStatus.PENDING_APPROVAL);
    assertThat(snapshots.get(1).getStatus()).isEqualTo(OrderStatus.APPROVED);
}
```

---

## 最佳实践

### 1. 选择合适的方案

| 场景 | 推荐方案 |
|------|---------|
| 简单的状态流转 | 方案1: 场景上下文 |
| 复杂的多步骤流程 | 方案2: 背景 + 构建器 |
| 验证状态转换规则 | 方案3: 场景大纲 |
| 需要状态回溯 | 方案4: 状态快照 |

### 2. 命名规范

```gherkin
# 清晰描述状态和操作
✅ 假如 存在 "执行中" 状态的价格变更单
✅ 当 订单从 "待审批" 变为 "已审批"
✅ 那么 状态历史应记录 "待审批 → 已审批 → 执行中"

# 避免技术细节
❌ 假如 order.status = EXECUTING
❌ 当 调用 approve() 方法
```

### 3. 状态验证

```java
// 验证状态本身
assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);

// 验证状态转换
assertThat(order.getStatus()).isNotEqualTo(previousStatus);

// 验证状态历史
assertThat(statusHistory).containsExactly(
    "待审批", "已审批", "执行中", "已完成"
);

// 验证状态相关数据
assertThat(order.getApprovedBy()).isNotNull();
assertThat(order.getApprovedAt()).isBefore(LocalDateTime.now());
```

### 4. 错误处理

```gherkin
场景: 非法状态转换
  假如 订单状态为 "已完成"
  当 尝试审批该订单
  那么 应抛出异常 "只有待审批状态的变更单才能审批"
  并且 订单状态保持 "已完成"
```

```java
@当("尝试审批该订单")
public void 尝试审批该订单() {
    PriceChangeOrder order = scenarioContext.getCurrentOrder();

    try {
        order.approve("测试人员");
        scenarioContext.addState("exception_thrown", false);
    } catch (IllegalStateException e) {
        scenarioContext.addState("exception_thrown", true);
        scenarioContext.addState("exception_message", e.getMessage());
    }
}
```

---

## 完整示例

结合所有方案的完整示例见：
- `behaviors/menu/price_state_flow.feature` - BDD 场景
- `src/test/bdd/StateFlowSteps.java` - 测试步骤
- `src/test/bdd/context/ScenarioContext.java` - 场景上下文
- `src/test/bdd/builders/PriceChangeOrderBuilder.java` - 状态构建器

---

**最后更新**: 2026-02-04
**作者**: Platform Engineering Team
