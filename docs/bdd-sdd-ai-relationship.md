# BDD、SDD、AI 三者关系深度剖析

## 核心论点

> **BDD 到代码生成，本质上依赖沉淀的 SDD (Skills Driven Development) 方法论**

这不是三个独立的技术，而是一个完整的体系：
- **BDD**: 提供"What"（业务需求的结构化表达）
- **SDD**: 提供"How"（从需求到代码的转换规则）
- **AI**: 提供执行引擎（按 SDD 规则生成代码）

---

## 一、为什么 BDD 本身不够？

### BDD 的局限性

```gherkin
# BDD 场景（清晰的需求）
场景: 用户注册
  假如 用户填写邮箱 "test@example.com"
  当 用户点击注册按钮
  那么 注册应该成功
```

**问题**: 这个场景虽然清晰，但没有告诉 AI：
- ❓ 用什么架构模式？（MVC、DDD、Clean Architecture？）
- ❓ 邮箱验证用什么规则？（正则表达式？）
- ❓ 密码如何加密？（BCrypt、SHA256？）
- ❓ 数据如何持久化？（JPA、MyBatis、MongoDB？）
- ❓ 错误如何处理？（异常、Result 模式？）

### 没有 SDD 的后果

```
BDD 场景 → AI 直接生成
           ↓
       ❌ 代码质量参差不齐
       ❌ 架构风格不统一
       ❌ 没有最佳实践
       ❌ 缺少领域知识
       ❌ 难以维护
```

---

## 二、SDD 是什么？

### 定义

**Skills Driven Development (SDD)** 是一套**将业务场景转换为代码的规则体系**，包含：

1. **架构模式** (Architecture Patterns)
2. **代码模板** (Code Templates)
3. **领域知识** (Domain Knowledge)
4. **最佳实践** (Best Practices)
5. **约束规则** (Constraints)

### SDD 的核心：Skill 定义

```yaml
# skills/user-management/skill-definition.md

skill_name: 用户管理 (User Management)
version: 1.0.0

# 1. 架构模式
architecture:
  style: DDD (Domain-Driven Design)
  layers:
    - domain   # 领域层（业务逻辑）
    - service  # 服务层（编排）
    - controller # 接口层
    - repository # 持久层

# 2. 领域模型映射
domain_mapping:
  entities:
    - name: User
      attributes:
        - email: String (唯一)
        - username: String
        - password: String (加密)
        - status: UserStatus (枚举)
      business_methods:
        - verifyEmail(): 验证邮箱
        - activate(): 激活账号

  value_objects:
    - Email: 邮箱格式验证
    - Password: 密码强度验证

  enums:
    - UserStatus: [PENDING_VERIFICATION, ACTIVATED, DISABLED]

# 3. 业务规则
business_rules:
  - name: 邮箱格式验证
    pattern: "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    error_message: "邮箱格式不正确"

  - name: 密码强度
    pattern: "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"
    error_message: "密码必须包含字母、数字和特殊字符，至少8位"

  - name: 邮箱唯一性
    check: userRepository.existsByEmail(email)
    error_message: "该邮箱已被注册"

# 4. 代码生成规则
code_generation:
  domain_layer:
    template: |
      public class User {
          // 状态机保护
          public void verifyEmail() {
              if (this.status != UserStatus.PENDING_VERIFICATION) {
                  throw new IllegalStateException("只有待验证状态才能验证邮箱");
              }
              this.status = UserStatus.ACTIVATED;
          }
      }

  service_layer:
    template: |
      @Service
      public class UserRegistrationService {
          @Transactional
          public User registerUser(String email, String username, String password) {
              // 1. 验证邮箱格式（来源场景：邮箱格式验证）
              validateEmailFormat(email);

              // 2. 验证密码强度（来源场景：密码强度验证）
              validatePasswordStrength(password);

              // 3. 检查唯一性（来源场景：防止重复注册）
              checkEmailNotExists(email);

              // 4. 创建用户
              return createUser(email, username, password);
          }
      }

  test_layer:
    template: |
      @当("用户填写邮箱 {string}")
      public void 用户填写邮箱(String email) {
          scenarioContext.addState("email", email);
      }

# 5. 技术栈约束
tech_stack:
  language: Java 17+
  framework: Spring Boot 3.x
  persistence: Spring Data JPA
  validation: Hibernate Validator
  testing: Cucumber + JUnit 5
  password_encryption: BCrypt

# 6. 命名约定
naming_conventions:
  entity: PascalCase (User, Order)
  service: {Entity}Service (UserService)
  repository: {Entity}Repository (UserRepository)
  test_steps: {Feature}Steps (UserRegistrationSteps)

# 7. 注释规范
documentation:
  javadoc: 必须
  source_scenario: 必须标注来源场景
  example: |
    /**
     * 验证邮箱格式
     *
     * 来源场景: "邮箱格式验证"
     * behaviors/user/user_registration.feature:15
     */
```

---

## 三、BDD + SDD + AI 的协作机制

### 完整流程

```
┌─────────────────────────────────────────────────┐
│  第1步: 业务专家编写 BDD 场景                      │
│  ────────────────────────────────────────       │
│  场景: 用户注册                                    │
│    假如 用户填写邮箱 "test@example.com"            │
│    当 用户点击注册                                 │
│    那么 注册应该成功                               │
└─────────────────┬───────────────────────────────┘
                  ↓
┌─────────────────────────────────────────────────┐
│  第2步: AI 加载对应的 Skill 定义                   │
│  ────────────────────────────────────────       │
│  识别: user-management skill                    │
│  加载: 架构模式、领域模型、业务规则、代码模板        │
└─────────────────┬───────────────────────────────┘
                  ↓
┌─────────────────────────────────────────────────┐
│  第3步: AI 分析 BDD 场景 + Skill 规则             │
│  ────────────────────────────────────────       │
│  BDD: "用户填写邮箱"                              │
│    ↓                                            │
│  Skill: Email 值对象 + 格式验证规则               │
│    ↓                                            │
│  决策: 生成 validateEmailFormat() 方法           │
│        使用预定义的正则表达式                      │
└─────────────────┬───────────────────────────────┘
                  ↓
┌─────────────────────────────────────────────────┐
│  第4步: AI 按 Skill 模板生成代码                   │
│  ────────────────────────────────────────       │
│  生成:                                           │
│  - User.java (领域模型)                          │
│  - UserRegistrationService.java (服务层)        │
│  - UserRegistrationSteps.java (测试步骤)        │
│                                                 │
│  每段代码都标注: 来源场景 + Skill 规则            │
└─────────────────┬───────────────────────────────┘
                  ↓
┌─────────────────────────────────────────────────┐
│  第5步: 自动化测试验证                             │
│  ────────────────────────────────────────       │
│  运行 Cucumber 测试                              │
│  验证生成的代码是否满足 BDD 场景                   │
└─────────────────────────────────────────────────┘
```

### 关键机制

#### 机制1: 场景→实体映射
```
BDD 场景中的名词 → Skill 中的领域实体

场景: "用户填写邮箱"
  ↓
Skill: User 实体 + Email 值对象
  ↓
代码:
  public class User {
      private Email email; // 使用值对象
  }
```

#### 机制2: 场景→规则映射
```
BDD 场景中的约束 → Skill 中的业务规则

场景: "邮箱格式不正确"
  ↓
Skill: email_validation_pattern
  ↓
代码:
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@...");
```

#### 机制3: 场景→方法映射
```
BDD 场景中的动作 → Skill 中的方法模板

场景: "用户点击注册"
  ↓
Skill: registerUser() 方法模板
  ↓
代码:
  @Transactional
  public User registerUser(String email, String username, String password) {
      // 按 Skill 定义的步骤生成
  }
```

---

## 四、SDD 的价值：从"能生成"到"生成好"

### 没有 SDD（纯 AI）

```java
// ❌ AI 可能生成的代码（不一致、不规范）

// 风格1: 直接在 Controller 写业务逻辑
@PostMapping("/register")
public String register(String email, String password) {
    if (!email.contains("@")) {
        return "error";
    }
    // 业务逻辑混在一起
}

// 风格2: 没有状态机保护
public void setStatus(String status) {
    this.status = status; // 任意修改状态
}

// 风格3: 没有场景追溯
public void validateEmail(String email) {
    // 不知道这个方法来源于哪个场景
}
```

### 有 SDD（规范生成）

```java
// ✅ 按 SDD 规则生成的代码（统一、规范、可追溯）

/**
 * 用户注册服务
 *
 * 来源场景: behaviors/user/user_registration.feature
 * 遵循 Skill: user-management v1.0.0
 */
@Service
public class UserRegistrationService {

    /**
     * 验证邮箱格式
     *
     * 来源场景: "邮箱格式验证"
     * 业务规则: Skill.business_rules.email_validation
     */
    private void validateEmailFormat(String email) {
        // 使用 Skill 预定义的正则
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RegistrationException("邮箱格式不正确");
        }
    }

    /**
     * 注册用户
     *
     * 来源场景: "成功注册新用户"
     * 架构层: Service Layer (Skill.architecture.layers)
     */
    @Transactional
    public User registerUser(String email, String username, String password) {
        // 按 Skill 定义的步骤执行
        validateEmailFormat(email);
        validatePasswordStrength(password);
        checkEmailNotExists(email);
        return createUser(email, username, password);
    }
}
```

---

## 五、SDD 沉淀的内容

### 1. 架构模式库

```yaml
patterns:
  - name: DDD
    description: 领域驱动设计
    layers: [domain, service, controller, repository]
    适用场景: 复杂业务逻辑

  - name: Clean Architecture
    description: 整洁架构
    layers: [entity, usecase, interface, framework]
    适用场景: 高度解耦需求

  - name: Transaction Script
    description: 事务脚本
    layers: [service, data]
    适用场景: 简单 CRUD
```

### 2. 领域模型模板库

```yaml
entity_patterns:
  - type: 聚合根 (Aggregate Root)
    example: Order, User
    characteristics:
      - 拥有唯一标识
      - 管理内部实体
      - 维护不变量

  - type: 实体 (Entity)
    example: OrderItem, Address
    characteristics:
      - 有标识
      - 可变

  - type: 值对象 (Value Object)
    example: Email, Money
    characteristics:
      - 无标识
      - 不可变
      - 相等性基于值
```

### 3. 业务规则库

```yaml
rule_patterns:
  validation:
    - email_format: 正则验证
    - password_strength: 复杂度检查
    - unique_constraint: 唯一性验证

  state_machine:
    - order_status: NEW → PAID → SHIPPED → DELIVERED
    - user_status: PENDING → ACTIVATED → DISABLED

  business_logic:
    - discount_calculation: 折扣计算规则
    - inventory_check: 库存检查规则
```

### 4. 测试模式库

```yaml
test_patterns:
  - type: 阳光路径 (Happy Path)
    template: |
      场景: {功能}正常流程
        假如 {前置条件}
        当 {操作}
        那么 {预期结果}

  - type: 边界条件 (Edge Cases)
    template: |
      场景大纲: {功能}边界测试
        当 输入 "<值>"
        那么 结果应为 "<预期>"
        例子:
          | 值 | 预期 |

  - type: 异常处理 (Exception Handling)
    template: |
      场景: {功能}异常处理
        假如 {异常条件}
        当 {操作}
        那么 应该抛出异常 "{异常消息}"
```

### 5. 代码风格规范

```yaml
coding_standards:
  naming:
    entity: PascalCase
    method: camelCase
    constant: UPPER_SNAKE_CASE

  structure:
    package: com.company.{domain}.{layer}
    test: src/test/java/{package}/test/bdd

  documentation:
    - 必须有 Javadoc
    - 必须标注来源场景
    - 必须说明业务规则

  error_handling:
    - 使用领域异常
    - 明确错误消息
    - 包含错误码
```

---

## 六、SDD 的进化：组织知识的沉淀

### 初期（项目级）

```
单个项目的 Skill 定义
  - user-management skill
  - order-management skill
  - payment skill
```

### 中期（团队级）

```
团队共享的 Skill 库
  - 统一的架构模式
  - 共享的领域模型
  - 通用的业务规则

→ 代码风格统一
→ 知识可复用
```

### 成熟期（组织级）

```
企业级 Skill 平台
  - 跨团队共享
  - 版本管理
  - 持续优化
  - AI Fine-tuning

→ 组织知识资产
→ 最佳实践沉淀
→ 新人快速上手
```

---

## 七、总结：三者的本质关系

```
┌──────────────────────────────────────────────┐
│  BDD: 业务语言（人类→机器的接口）              │
│  ────────────────────────────                │
│  用业务术语描述"要做什么"                      │
│  可被业务专家和 AI 共同理解                    │
└──────────────┬───────────────────────────────┘
               ↓
┌──────────────────────────────────────────────┐
│  SDD: 转换规则（知识沉淀）                     │
│  ────────────────────────────                │
│  定义"如何从 BDD 生成代码"                     │
│  包含架构、模式、规则、最佳实践                │
│  这是核心竞争力和知识资产！                    │
└──────────────┬───────────────────────────────┘
               ↓
┌──────────────────────────────────────────────┐
│  AI: 执行引擎（自动化工具）                     │
│  ────────────────────────────                │
│  按 SDD 规则将 BDD 转换为代码                  │
│  提供高效、一致的生成能力                       │
└──────────────────────────────────────────────┘
```

### 核心公式

```
高质量代码 = BDD (结构化需求) × SDD (沉淀规则) × AI (生成能力)
```

**关键洞察**:
- BDD 没有 SDD = AI 生成质量参差不齐
- SDD 没有 BDD = 规则无法应用
- BDD + SDD 没有 AI = 仍需大量手工编码

**三者缺一不可，但 SDD 是核心竞争力！**

---

## 八、实践建议

### 对于团队

1. **不要急于 AI 生成代码**
   - 先建立 Skill 定义
   - 沉淀架构模式
   - 定义业务规则

2. **将 Skill 当作知识资产管理**
   - 版本控制
   - 持续优化
   - 团队评审

3. **建立反馈循环**
   ```
   BDD 场景 → AI 生成 → 代码审查 → 优化 Skill → 重新生成
   ```

### 对于组织

1. **建立 Skill 平台**
   - 集中管理 Skill 定义
   - 跨团队共享
   - 度量生成质量

2. **投资 Skill 建设**
   - 设立专职团队
   - 持续沉淀最佳实践
   - Fine-tune 组织专属的 AI 模型

3. **培养 Skill 设计能力**
   - 不仅是写代码
   - 更是设计"如何生成代码的规则"
   - 这是 AI 时代的核心竞争力

---

## 结论

> **BDD 提供了"说什么"的能力**
> **SDD 提供了"怎么做"的知识**
> **AI 提供了"自动执行"的效率**

**SDD 是连接 BDD 和 AI 的关键桥梁，是组织知识沉淀的核心载体！**

没有 SDD，BDD + AI 只是"能生成代码"；
有了 SDD，BDD + AI 才能"生成高质量、可维护、符合规范的代码"。

**这才是 AI Native 时代研发效能提升的本质。**
