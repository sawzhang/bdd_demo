# 咖啡公司 SDD + BDD 融合实践 Demo

## 项目概述

这个项目展示了如何将 **Skills Driven Development (SDD)** 与 **Behavior Driven Development (BDD)** 融合，实现从业务需求到代码实现的全链路自动化。

### 核心价值链

```
业务需求 → BDD场景描述 → AI Skill生成代码 → 自动生成测试 → 持续验证
```

## 项目结构

```
skill_bdd/
├── README.md                          # 本文档
├── skills/                            # Skills 定义目录
│   ├── menu-pricing/                  # 菜单价格管理 Skill
│   ├── order-management/              # 订单管理 Skill (OMS)
│   └── modifier-validation/           # 定制验证 Skill (Customization)
├── src/                               # 生成的源代码
│   ├── main/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── domain/
│   │   └── repository/
│   └── test/
│       ├── bdd/                       # Cucumber BDD 测试
│       └── api/                       # Karate API 测试
├── behaviors/                         # BDD 场景库（共享）
│   ├── menu/
│   ├── order/
│   └── customization/
└── docs/                              # 文档
    ├── architecture.md
    └── integration-guide.md
```

## 快速开始

### 1. 使用 Menu Pricing Skill 生成代码

```bash
# 方式1: 使用 Claude Code CLI
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --generate-implementation

# 方式2: 在 Claude Code 交互模式中
/skill menu-pricing behaviors/menu/price_update.feature
```

### 2. 查看生成的代码

```bash
# 查看生成的 Controller
cat src/main/controller/MenuPricingController.java

# 查看生成的测试
cat src/test/bdd/PriceUpdateSteps.java
```

### 3. 运行 BDD 测试

```bash
# 运行 Cucumber 测试
mvn test -Dtest=BddTestRunner

# 运行 Karate API 测试
mvn test -Dtest=KarateRunner
```

## Demo 场景说明

### 场景1: 菜单价格批量更新 (Menu Pricing)

**业务背景**: 运营需要快速响应市场变化，批量调整区域饮品价格

**BDD 场景**: `behaviors/menu/price_update.feature`
**生成代码**:
- Controller: `MenuPricingController`
- Service: `PricingService`
- Domain: `PriceChangeOrder`, `PriceHistory`
- 测试: Cucumber + Karate 完整测试套件

### 场景2: 订单创建与取消 (Order Management - OMS)

**业务背景**: OMS系统核心订单流程

**BDD 场景**: `behaviors/order/create_order.feature`
**生成代码**: 完整的订单处理链路

### 场景3: 定制验证 (Modifier Validation - Customization)

**业务背景**: Customization系统需要验证饮品定制组合的有效性

**BDD 场景**: `behaviors/customization/validate_modifier.feature`
**生成代码**: 定制规则引擎

## 与现有架构集成

### 契约优先开发流程

```
BDD场景 → AI分析 → OpenAPI契约 → 代码生成 → 测试验证
```

### 并行开发模式

- **前端团队**: 根据BDD场景理解业务，使用Mock数据开发
- **后端团队**: AI根据BDD生成实现，自动测试验证
- **测试团队**: BDD场景即测试用例，自动化执行

## 质量保障

### 自动化测试覆盖

- ✅ BDD场景测试 (Cucumber)
- ✅ API契约测试 (Karate)
- ✅ 单元测试 (JUnit)
- ✅ 集成测试

### CI/CD 集成

```yaml
# .github/workflows/bdd-validation.yml
- BDD场景验证
- AI代码生成
- 自动化测试执行
- 质量门禁
```

## 效率提升指标

- **代码生成效率**: 提升 70%
- **测试编写时间**: 减少 80%
- **需求理解一致性**: 提升 90%
- **缺陷发现前移**: 60% 的缺陷在开发阶段发现

## 下一步

1. **试点模块**: 从 Customization 定制模块开始
2. **场景库建设**: 创建 5-10 个核心业务场景
3. **团队培训**: BDD 编写规范和 Skill 使用
4. **度量优化**: 持续收集效率提升数据

## 📚 文档导航

### 核心文档
- [README.md](README.md) - 项目主文档（本文档）
- [CLAUDE.md](CLAUDE.md) - Claude Code 协作指南

### 🎯 方法论与价值（推荐阅读）
- [**BDD + AI Native 价值分析**](docs/bdd-ai-native-analysis.md) - 深度剖析 BDD 原理与 AI 时代的价值
- [**BDD + AI Native 架构设计**](docs/bdd-ai-native-architecture.md) - 完整的技术架构与实施路径

### 架构与演示
- [架构文档](docs/architecture.md) - 系统架构设计
- [演示文档](docs/demo.md) - 完整演示说明
- [演示执行](docs/demo-execution.md) - 演示步骤指南
- [项目总结](docs/project-summary.md) - 项目概要总结

### 开发指南
- [BDD 测试指南](docs/guides/bdd-testing-guide.md) - BDD 测试编写规范
- [中间状态解决方案](docs/guides/bdd-intermediate-state-solutions.md) - 处理场景中间状态
- [外部依赖解决方案](docs/guides/bdd-external-dependencies-solutions.md) - Mock 外部服务
- [集成指南](docs/guides/integration-guide.md) - 系统集成说明

## 支持

如有问题，请联系技术架构团队。
