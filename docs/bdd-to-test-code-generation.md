# BDD 到测试代码生成的思路

## 核心问题

**Q: BDD 到测试代码生成需要走 SDD 吗？**

**A: 必须！测试代码生成同样需要 SDD 规则的指导。**

---

## 一、为什么测试代码生成也需要 SDD？

### BDD 场景的双重身份

```gherkin
场景: 用户注册
  假如 用户填写邮箱 "test@example.com"
  当 用户点击注册按钮
  那么 注册应该成功
```

这个场景既是：
1. **需求规约** → 需要 SDD 生成业务代码
2. **测试用例** → 需要 SDD 生成测试代码

### 没有 SDD 的困境

```
BDD 场景 → AI 直接生成测试代码

❌ 问题:
  - 步骤定义方法命名不统一
  - 参数提取方式不一致
  - 断言风格各不相同
  - 场景上下文管理混乱
  - 数据表格处理不规范
```

---

## 二、测试代码生成的完整流程

### 流程图

```
┌─────────────────────────────────────────┐
│  BDD 场景 (Gherkin)                      │
│  ────────────────────                   │
│  场景: 用户注册                           │
│    假如 用户填写邮箱 "test@example.com"   │
│    当 用户点击注册按钮                     │
│    那么 注册应该成功                       │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│  加载 Test Skill 定义                    │
│  ────────────────────                   │
│  - 步骤定义模板                          │
│  - 参数提取规则                          │
│  - 断言模式                              │
│  - 上下文管理                            │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│  AI 解析场景                             │
│  ────────────────────                   │
│  识别: Given/When/Then                  │
│  提取: 参数、数据表格                    │
│  映射: 场景步骤 → 方法签名               │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│  按 Test Skill 规则生成测试代码          │
│  ────────────────────                   │
│  @假如("用户填写邮箱 {string}")          │
│  public void 用户填写邮箱(String email) {│
│      scenarioContext.addState("email", email);│
│  }                                      │
└─────────────────────────────────────────┘
```

---

## 三、Test Skill 定义示例

### 完整的 Test Skill

```yaml
# skills/test-automation/test-skill-definition.md

skill_name: 测试自动化 (Test Automation)
version: 1.0.0
purpose: 从 BDD 场景生成测试代码

# ════════════════════════════════════════
# 1. 测试框架选择
# ════════════════════════════════════════
framework:
  bdd_tool: Cucumber
  test_framework: JUnit 5
  assertion_library: AssertJ
  language: Java

# ════════════════════════════════════════
# 2. 步骤定义模板
# ════════════════════════════════════════
step_definition_templates:

  # Given 步骤模板
  given_template: |
    @假如("{step_text}")
    public void {method_name}({parameters}) {
        // 1. 准备测试数据
        // 2. 设置前置条件
        // 3. 保存到场景上下文
        log.info("✓ {description}");
    }

  # When 步骤模板
  when_template: |
    @当("{step_text}")
    public void {method_name}({parameters}) {
        try {
            // 1. 执行业务操作
            // 2. 捕获结果
            // 3. 保存到场景上下文
            scenarioContext.addState("operation_success", true);
        } catch (Exception e) {
            scenarioContext.addState("operation_success", false);
            scenarioContext.addState("error_message", e.getMessage());
        }
    }

  # Then 步骤模板
  then_template: |
    @那么("{step_text}")
    public void {method_name}({parameters}) {
        // 1. 从场景上下文获取实际结果
        // 2. 断言验证
        // 3. 记录验证日志
        log.info("✓ 验证通过: {description}");
    }

# ════════════════════════════════════════
# 3. 参数提取规则
# ════════════════════════════════════════
parameter_extraction:

  # 字符串参数
  string_pattern:
    regex: '"([^"]*)"'
    cucumber_type: "{string}"
    java_type: "String"
    example: |
      场景: 假如 用户填写邮箱 "test@example.com"
        ↓
      @假如("用户填写邮箱 {string}")
      public void 用户填写邮箱(String email)

  # 整数参数
  int_pattern:
    regex: '(\d+)'
    cucumber_type: "{int}"
    java_type: "int"
    example: |
      场景: 当 用户购买 3 杯咖啡
        ↓
      @当("用户购买 {int} 杯咖啡")
      public void 用户购买杯咖啡(int quantity)

  # 小数参数
  double_pattern:
    regex: '(\d+\.\d+)'
    cucumber_type: "{double}"
    java_type: "double"
    example: |
      场景: 那么 总价应为 99.99 元
        ↓
      @那么("总价应为 {double} 元")
      public void 总价应为元(double expectedPrice)

  # 数据表格参数
  datatable_pattern:
    cucumber_type: "DataTable"
    java_type: "DataTable"
    example: |
      场景:
        当 用户填写注册信息:
          | 邮箱 | 用户名 | 密码 |
          | ... | ...   | ... |
        ↓
      @当("用户填写注册信息:")
      public void 用户填写注册信息(DataTable dataTable) {
          Map<String, String> data = dataTable.asMaps().get(0);
      }

# ════════════════════════════════════════
# 4. 断言模式
# ════════════════════════════════════════
assertion_patterns:

  # 成功断言
  success_assertion:
    pattern: "{操作}应该成功"
    implementation: |
      Boolean success = scenarioContext.getState("operation_success", Boolean.class);
      assertThat(success).isTrue();

  # 失败断言
  failure_assertion:
    pattern: "{操作}应该失败"
    implementation: |
      Boolean success = scenarioContext.getState("operation_success", Boolean.class);
      assertThat(success).isFalse();

  # 相等断言
  equality_assertion:
    pattern: "{字段}应为 {string}"
    implementation: |
      String actualValue = scenarioContext.getState("{field}", String.class);
      assertThat(actualValue).isEqualTo(expectedValue);

  # 包含断言
  contains_assertion:
    pattern: "应该显示错误消息 {string}"
    implementation: |
      String errorMessage = scenarioContext.getState("error_message", String.class);
      assertThat(errorMessage).contains(expectedMessage);

  # 集合断言
  collection_assertion:
    pattern: "应该返回 {int} 个{实体}"
    implementation: |
      List<?> items = scenarioContext.getState("{entity}_list", List.class);
      assertThat(items).hasSize(expectedCount);

# ════════════════════════════════════════
# 5. 场景上下文管理
# ════════════════════════════════════════
context_management:

  # 上下文存储规则
  storage_rules:
    - key_pattern: "{entity}_current"  # 当前实体
      example: "order_current"

    - key_pattern: "{operation}_success"  # 操作结果
      example: "registration_success"

    - key_pattern: "error_message"  # 错误消息
      type: String

  # 上下文操作
  operations:
    save: |
      scenarioContext.addState(key, value);

    retrieve: |
      Type value = scenarioContext.getState(key, Type.class);

    clear: |
      scenarioContext.clear(); // 场景结束后自动清理

# ════════════════════════════════════════
# 6. 步骤定义命名规范
# ════════════════════════════════════════
naming_conventions:

  # 方法命名
  method_naming:
    rule: "使用场景文本作为方法名（去除参数占位符）"
    examples:
      - scenario: "用户填写邮箱 {string}"
        method: "用户填写邮箱"

      - scenario: "用户购买 {int} 杯咖啡"
        method: "用户购买杯咖啡"

  # 参数命名
  parameter_naming:
    rule: "根据业务含义命名，使用驼峰命名法"
    examples:
      - "{string}" → "email", "username", "orderNo"
      - "{int}" → "quantity", "count", "age"
      - "{double}" → "price", "amount", "rate"

# ════════════════════════════════════════
# 7. 数据表格处理
# ════════════════════════════════════════
datatable_handling:

  # 单行数据
  single_row:
    access: "dataTable.asMaps().get(0)"
    example: |
      Map<String, String> data = dataTable.asMaps().get(0);
      String email = data.get("邮箱");
      String username = data.get("用户名");

  # 多行数据
  multiple_rows:
    access: "dataTable.asMaps()"
    example: |
      List<Map<String, String>> rows = dataTable.asMaps();
      for (Map<String, String> row : rows) {
          process(row);
      }

  # 转换为对象
  to_object:
    example: |
      List<User> users = dataTable.asMaps().stream()
          .map(data -> User.builder()
              .email(data.get("邮箱"))
              .username(data.get("用户名"))
              .build())
          .collect(Collectors.toList());

# ════════════════════════════════════════
# 8. 日志规范
# ════════════════════════════════════════
logging_standards:

  # Given 日志
  given_log: 'log.info("✓ {前置条件已准备}");'

  # When 日志
  when_log: 'log.info("{执行操作描述}");'

  # Then 日志
  then_log: 'log.info("✓ 验证通过: {验证内容}");'

  # 失败日志
  failure_log: 'log.info("✗ {操作失败}: {错误原因}");'

# ════════════════════════════════════════
# 9. 错误处理模式
# ════════════════════════════════════════
error_handling:

  # 预期异常
  expected_exception:
    pattern: |
      try {
          // 执行可能失败的操作
      } catch (ExpectedException e) {
          scenarioContext.addState("operation_success", false);
          scenarioContext.addState("error_message", e.getMessage());
      }

  # 意外异常
  unexpected_exception:
    pattern: |
      try {
          // 执行操作
      } catch (Exception e) {
          log.error("意外异常", e);
          throw e;
      }

# ════════════════════════════════════════
# 10. 测试类结构
# ════════════════════════════════════════
test_class_structure:
  template: |
    package com.company.{domain}.test.bdd;

    import com.company.{domain}.domain.*;
    import com.company.{domain}.service.*;
    import com.company.menu.test.bdd.context.ScenarioContext;
    import io.cucumber.datatable.DataTable;
    import io.cucumber.java.zh_cn.*;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;

    import static org.assertj.core.api.Assertions.assertThat;

    /**
     * {功能名称}测试步骤定义
     *
     * 来源场景: behaviors/{domain}/{feature_name}.feature
     *
     * @author AI-Generated
     * @version 1.0.0
     */
    @Slf4j
    @SpringBootTest
    public class {FeatureName}Steps {

        @Autowired
        private ScenarioContext scenarioContext;

        @Autowired
        private {Domain}Service service;

        // ==================== Given 步骤 ====================

        // ==================== When 步骤 ====================

        // ==================== Then 步骤 ====================
    }
```

---

## 四、实际生成示例

### 输入：BDD 场景

```gherkin
场景: 用户注册
  假如 用户访问注册页面
  当 用户填写注册信息:
    | 邮箱              | 用户名 | 密码         |
    | test@example.com | 张三   | Pass123!    |
  并且 用户点击"注册"按钮
  那么 注册应该成功
  并且 用户状态应为 "待验证"
  并且 应该显示消息 "注册成功"
```

### 输出：测试代码（按 Test Skill 生成）

```java
package com.company.user.test.bdd;

import com.company.user.domain.User;
import com.company.user.service.UserRegistrationService;
import com.company.menu.test.bdd.context.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.zh_cn.假如;
import io.cucumber.java.zh_cn.当;
import io.cucumber.java.zh_cn.那么;
import io.cucumber.java.zh_cn.并且;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户注册测试步骤定义
 *
 * 来源场景: behaviors/user/user_registration.feature
 * 遵循 Skill: test-automation v1.0.0
 *
 * @author AI-Generated
 * @version 1.0.0
 */
@Slf4j
@SpringBootTest
public class UserRegistrationSteps {

    @Autowired
    private UserRegistrationService registrationService;

    @Autowired
    private ScenarioContext scenarioContext;

    // ==================== Given 步骤 ====================

    /**
     * 来源: Test Skill.step_definition_templates.given_template
     */
    @假如("用户访问注册页面")
    public void 用户访问注册页面() {
        // 1. 准备测试数据
        scenarioContext.addState("on_registration_page", true);

        // 2. 设置前置条件
        // (无需额外设置)

        // 3. 记录日志
        log.info("✓ 用户已访问注册页面");
    }

    // ==================== When 步骤 ====================

    /**
     * 来源: Test Skill.step_definition_templates.when_template
     * 参数提取: Test Skill.parameter_extraction.datatable_pattern
     */
    @当("用户填写注册信息:")
    public void 用户填写注册信息(DataTable dataTable) {
        // 1. 按 Test Skill 规则提取数据
        Map<String, String> data = dataTable.asMaps().get(0);
        String email = data.get("邮箱");
        String username = data.get("用户名");
        String password = data.get("密码");

        // 2. 保存到场景上下文
        scenarioContext.addState("registration_email", email);
        scenarioContext.addState("registration_username", username);
        scenarioContext.addState("registration_password", password);

        log.info("用户填写注册信息 - 邮箱: {}, 用户名: {}", email, username);
    }

    /**
     * 来源: Test Skill.step_definition_templates.when_template
     * 参数提取: Test Skill.parameter_extraction.string_pattern
     */
    @并且("用户点击\"{string}\"按钮")
    public void 用户点击按钮(String buttonName) {
        String email = scenarioContext.getState("registration_email", String.class);
        String username = scenarioContext.getState("registration_username", String.class);
        String password = scenarioContext.getState("registration_password", String.class);

        log.info("用户点击 {} 按钮", buttonName);

        try {
            // 1. 执行业务操作
            User user = registrationService.registerUser(email, username, password);

            // 2. 捕获结果
            scenarioContext.addState("registered_user", user);
            scenarioContext.addState("registration_success", true);

            log.info("✓ 注册成功: {}", email);

        } catch (Exception e) {
            // 3. 按 Test Skill 错误处理模式
            scenarioContext.addState("registration_success", false);
            scenarioContext.addState("error_message", e.getMessage());

            log.info("✗ 注册失败: {}", e.getMessage());
        }
    }

    // ==================== Then 步骤 ====================

    /**
     * 来源: Test Skill.step_definition_templates.then_template
     * 断言模式: Test Skill.assertion_patterns.success_assertion
     */
    @那么("注册应该成功")
    public void 注册应该成功() {
        // 1. 从场景上下文获取实际结果
        Boolean success = scenarioContext.getState("registration_success", Boolean.class);

        // 2. 按 Test Skill 断言模式验证
        assertThat(success).isTrue();

        User user = scenarioContext.getState("registered_user", User.class);
        assertThat(user).isNotNull();

        // 3. 记录验证日志
        log.info("✓ 验证通过: 注册成功");
    }

    /**
     * 来源: Test Skill.step_definition_templates.then_template
     * 断言模式: Test Skill.assertion_patterns.equality_assertion
     * 参数提取: Test Skill.parameter_extraction.string_pattern
     */
    @并且("用户状态应为 {string}")
    public void 用户状态应为(String expectedStatus) {
        // 1. 从场景上下文获取实际结果
        User user = scenarioContext.getState("registered_user", User.class);

        // 2. 按 Test Skill 断言模式验证
        assertThat(user).isNotNull();
        assertThat(user.getStatus().getDescription()).isEqualTo(expectedStatus);

        // 3. 记录验证日志
        log.info("✓ 验证通过: 用户状态 = {}", expectedStatus);
    }

    /**
     * 来源: Test Skill.step_definition_templates.then_template
     * 断言模式: Test Skill.assertion_patterns.contains_assertion
     * 参数提取: Test Skill.parameter_extraction.string_pattern
     */
    @并且("应该显示消息 {string}")
    public void 应该显示消息(String expectedMessage) {
        if ("注册成功".equals(expectedMessage)) {
            Boolean success = scenarioContext.getState("registration_success", Boolean.class);
            assertThat(success).isTrue();
        } else {
            String errorMessage = scenarioContext.getState("error_message", String.class);
            assertThat(errorMessage).contains(expectedMessage);
        }

        log.info("✓ 验证通过: 消息 = '{}'", expectedMessage);
    }
}
```

---

## 五、Test Skill 的核心价值

### 1. 统一的测试风格

```java
// ✅ 所有测试步骤定义遵循统一模板

// Given 步骤：设置前置条件 + 记录日志
@假如("...")
public void method() {
    scenarioContext.addState(...);
    log.info("✓ ...");
}

// When 步骤：执行操作 + 捕获结果 + 错误处理
@当("...")
public void method() {
    try {
        // 执行
        scenarioContext.addState("success", true);
    } catch (Exception e) {
        scenarioContext.addState("success", false);
        scenarioContext.addState("error_message", e.getMessage());
    }
}

// Then 步骤：断言验证 + 记录日志
@那么("...")
public void method() {
    assertThat(...).isTrue();
    log.info("✓ 验证通过: ...");
}
```

### 2. 可预测的代码质量

```
有 Test Skill:
  ✅ 步骤定义命名一致
  ✅ 参数提取规范
  ✅ 断言模式统一
  ✅ 日志格式标准
  ✅ 错误处理完善

没有 Test Skill:
  ❌ 命名风格各异
  ❌ 参数提取混乱
  ❌ 断言方式不同
  ❌ 日志不规范
  ❌ 错误处理缺失
```

### 3. 易于维护和扩展

```
新增场景:
  场景: 用户修改密码
    当 用户填写新密码 "NewPass123!"
    那么 修改应该成功

↓ 按 Test Skill 自动生成

@当("用户填写新密码 {string}")
public void 用户填写新密码(String newPassword) {
    // 自动遵循 Test Skill 模板
}

@那么("修改应该成功")
public void 修改应该成功() {
    // 自动使用 success_assertion 模式
}
```

---

## 六、总结

### 核心答案

**Q: BDD 到测试代码生成需要走 SDD 吗？**

**A: 必须！**

```
BDD 场景 → Test Skill (SDD) → 测试代码

Test Skill 定义了:
  ✓ 步骤定义模板
  ✓ 参数提取规则
  ✓ 断言模式
  ✓ 上下文管理
  ✓ 命名规范
  ✓ 日志规范
  ✓ 错误处理

没有 Test Skill，AI 无法生成规范、统一、可维护的测试代码！
```

### 完整链路

```
┌─────────────┐
│  BDD 场景    │ ← 业务专家编写
└──────┬──────┘
       │
       ├───────────┐
       ↓           ↓
┌─────────────┐ ┌─────────────┐
│ Domain Skill│ │ Test Skill  │ ← 架构师/团队沉淀
└──────┬──────┘ └──────┬──────┘
       │               │
       ↓               ↓
┌─────────────┐ ┌─────────────┐
│  业务代码    │ │  测试代码    │ ← AI 按 Skill 生成
└─────────────┘ └─────────────┘
```

**两个 Skill 缺一不可！**
- Domain Skill: 生成高质量业务代码
- Test Skill: 生成高质量测试代码

**这才是完整的 BDD + SDD + AI 体系！**
