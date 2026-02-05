# Menu Pricing Management Skill

## Skill 元数据

- **Skill ID**: `menu-pricing`
- **版本**: 1.0.0
- **所属域**: Menu Management (菜单管理)
- **依赖系统**: POS System, Store Management System
- **负责团队**: Menu Operations Team

## 功能描述

这个 Skill 负责根据 BDD 场景自动生成菜单价格管理的完整实现代码，包括：
- 价格变更单创建和审批流程
- 区域化差异定价策略
- 价格变更历史追踪
- 异常回滚机制
- 完整的测试套件

## 输入规范

### BDD 场景文件
- **路径**: `behaviors/menu/*.feature`
- **格式**: Gherkin (支持中文)
- **必需元素**:
  - 功能描述 (Feature)
  - 场景 (Scenario)
  - 步骤 (Given/When/Then)
  - 数据表格 (可选)

### 示例输入
```gherkin
功能: 菜单价格批量更新
  场景: 单一区域价格上调
    假如 运营人员登录价格管理系统
    当 提交价格调整请求
    那么 系统应生成价格变更单
```

## 输出规范

### 1. 领域模型 (Domain Models)
生成位置: `src/main/domain/`

```java
- PriceChangeOrder.java       // 价格变更单
- PriceHistory.java            // 价格历史
- RegionalPricing.java         // 区域定价
- PriceAdjustmentRule.java     // 调整规则
```

### 2. 服务层 (Services)
生成位置: `src/main/service/`

```java
- PricingService.java          // 价格管理服务
- ApprovalService.java         // 审批服务
- NotificationService.java     // 通知服务
- RollbackService.java         // 回滚服务
```

### 3. 控制器 (Controllers)
生成位置: `src/main/controller/`

```java
- MenuPricingController.java   // RESTful API
```

### 4. 数据访问层 (Repositories)
生成位置: `src/main/repository/`

```java
- PriceChangeOrderRepository.java
- PriceHistoryRepository.java
```

### 5. BDD 测试 (Cucumber)
生成位置: `src/test/bdd/`

```java
- PriceUpdateSteps.java        // Cucumber Step Definitions
- BddTestRunner.java           // 测试运行器
```

### 6. API 测试 (Karate)
生成位置: `src/test/api/`

```feature
- pricing-api-test.feature     // Karate API 测试场景
```

## 代码生成规则

### 架构模式
- **分层架构**: Controller → Service → Repository
- **DDD 领域驱动**: Rich Domain Model
- **CQRS**: 读写分离（查询历史 vs 创建变更单）

### 技术栈
- **框架**: Spring Boot 3.x
- **数据库**: PostgreSQL + Redis (缓存)
- **消息队列**: Kafka (异步通知)
- **API 规范**: OpenAPI 3.0

### 代码质量要求
- ✅ 单元测试覆盖率 > 80%
- ✅ 所有 public 方法必须有 Javadoc
- ✅ 使用 Lombok 减少样板代码
- ✅ 集成 SonarQube 质量检查
- ✅ 遵循阿里巴巴 Java 开发规范

### 安全考虑
- 🔒 价格调整操作需要审批权限
- 🔒 审计日志记录所有价格变更
- 🔒 敏感数据加密存储
- 🔒 API 接口需要 OAuth2 认证

## BDD 场景映射规则

### 场景关键词映射

| Gherkin 关键词 | 代码结构                    | 测试生成              |
|----------------|-----------------------------|-----------------------|
| 功能 (Feature) | 生成独立的 Service 类       | 生成测试类            |
| 场景 (Scenario)| 生成 Service 方法           | 生成测试方法          |
| 假如 (Given)   | Mock 数据准备 / Repository  | @Given Step           |
| 当 (When)      | 核心业务逻辑 / Service      | @When Step            |
| 那么 (Then)    | 验证逻辑 / Assertions       | @Then Step            |
| 数据表格       | DTO 类 / 批量操作逻辑       | 参数化测试            |

### 业务规则提取

从 BDD 场景中自动提取：
1. **验证规则**: "不能超过10元" → Validation Annotation
2. **业务常量**: "待审批" → Enum Status
3. **计算逻辑**: "调整金额 + 原价格" → Business Logic
4. **异常处理**: "执行失败" → Exception Handler

## 使用示例

### 方式1: Claude Code CLI

```bash
# 生成完整实现
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --output src/ \
  --generate-tests

# 仅生成领域模型
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --generate domain-only

# 生成 OpenAPI 契约
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --output-contract openapi/pricing-api.yaml
```

### 方式2: 交互式对话

```
User: 基于 behaviors/menu/price_update.feature 生成价格管理模块的代码

Claude:
我将基于 BDD 场景生成以下内容：
1. 领域模型 (PriceChangeOrder, PriceHistory)
2. 服务实现 (PricingService, ApprovalService)
3. RESTful API (MenuPricingController)
4. 完整测试套件 (Cucumber + Karate)

是否继续？[Y/n]
```

### 方式3: Git Commit Hook

```bash
# .git/hooks/pre-commit
# 检测到 .feature 文件变更时自动生成代码
if git diff --cached --name-only | grep ".feature$"; then
    claude-code --skill menu-pricing --auto-generate
fi
```

## 质量验证

### 生成代码后的自动检查

1. **编译检查**: `mvn clean compile`
2. **单元测试**: `mvn test`
3. **BDD 测试**: `mvn test -Dtest=BddTestRunner`
4. **API 测试**: `mvn test -Dkarate.options="--tags @pricing"`
5. **代码质量**: `mvn sonar:sonar`

### 持续集成流程

```yaml
# CI Pipeline
stages:
  - validate-bdd      # 验证 BDD 场景语法
  - generate-code     # AI 生成代码
  - run-tests         # 执行测试
  - quality-gate      # 质量门禁
  - deploy            # 部署
```

## 扩展能力

### 支持的变体场景
- ✅ 季节性促销定价
- ✅ 会员专属价格
- ✅ 时段差异定价（早午晚）
- ✅ 组合套餐定价
- ✅ 临期商品折扣

### 与其他 Skill 集成
- `order-management`: 订单价格计算
- `promotion-engine`: 促销价格覆盖
- `inventory-management`: 成本价关联

## 维护和演进

### 版本管理
- 遵循语义化版本 (Semantic Versioning)
- 每次 BDD 场景更新触发版本号变更
- 保持向后兼容性

### 反馈机制
- 生成代码的质量评分
- 测试覆盖率趋势
- 场景与代码一致性检查

## 团队协作

### 角色分工
- **产品经理**: 编写/维护 BDD 场景
- **开发工程师**: Review 生成的代码，补充边界逻辑
- **测试工程师**: 补充探索性测试用例
- **架构师**: 维护 Skill 模板和规则

### 知识库
- BDD 场景编写最佳实践
- 常见业务规则模式库
- 代码模板库

---

**最后更新**: 2026-02-04
**维护团队**: Menu Operations & Platform Engineering
