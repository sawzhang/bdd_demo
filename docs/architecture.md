# SDD + BDD 融合架构设计

## 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                      业务需求 (Business Requirements)            │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│              BDD 场景编写 (Gherkin Feature Files)                │
│  - 产品经理/业务分析师编写业务场景                               │
│  - 使用中文描述业务行为                                          │
│  - 包含验收条件和数据表格                                        │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│              Claude Code Skill 分析与生成                        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ 1. BDD 场景解析                                           │  │
│  │    - 提取业务规则                                         │  │
│  │    - 识别领域对象                                         │  │
│  │    - 分析数据流                                           │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │ 2. 代码生成                                               │  │
│  │    - Domain Models (领域模型)                            │  │
│  │    - Services (服务层)                                   │  │
│  │    - Controllers (控制器)                                │  │
│  │    - Repositories (数据访问层)                           │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │ 3. 测试生成                                               │  │
│  │    - Cucumber Step Definitions                           │  │
│  │    - Karate API Tests                                    │  │
│  │    - Unit Tests                                          │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   生成的代码和测试                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ 业务代码      │  │ 测试代码      │  │ API 契约     │          │
│  │ (src/main)   │  │ (src/test)   │  │ (openapi)    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   CI/CD 自动化流程                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ 编译检查  │→│ 单元测试  │→│ BDD测试  │→│ API测试  │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│                          │                                       │
│                          ▼                                       │
│  ┌──────────────────────────────────────┐                       │
│  │ 质量门禁 (Coverage > 80%)            │                       │
│  └──────────────────────────────────────┘                       │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                        部署                                      │
└─────────────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. BDD 场景库 (Behavior Specifications)

**位置**: `behaviors/`

**职责**:
- 存储所有业务场景的 Gherkin 描述
- 作为团队协作的统一语言
- 驱动代码生成和测试

**示例结构**:
```
behaviors/
├── menu/
│   ├── price_update.feature
│   ├── menu_creation.feature
│   └── seasonal_menu.feature
├── order/
│   ├── create_order.feature
│   └── cancel_order.feature
└── customization/
    └── validate_modifier.feature
```

### 2. Skills 定义 (AI Code Generation Skills)

**位置**: `skills/`

**职责**:
- 定义代码生成规则
- 映射 BDD 场景到代码结构
- 提供代码模板

**关键配置**:
```yaml
skill-definition.md:
  - 输入规范 (BDD 场景格式)
  - 输出规范 (代码结构)
  - 代码生成规则
  - 架构模式
  - 技术栈
```

### 3. 生成代码 (Generated Code)

**位置**: `src/main/`

**分层架构**:

```
src/main/
├── domain/          # 领域模型 (Rich Domain Model)
│   ├── PriceChangeOrder.java
│   └── PriceHistory.java
├── service/         # 业务服务层
│   ├── PricingService.java
│   ├── NotificationService.java
│   └── RollbackService.java
├── controller/      # API 控制器
│   └── MenuPricingController.java
└── repository/      # 数据访问层
    ├── PriceChangeOrderRepository.java
    └── PriceHistoryRepository.java
```

**设计原则**:
- ✅ DDD (Domain-Driven Design)
- ✅ SOLID 原则
- ✅ 分层架构
- ✅ 依赖注入

### 4. 测试套件 (Test Suite)

**位置**: `src/test/`

**测试金字塔**:

```
src/test/
├── bdd/                    # BDD 验收测试 (Cucumber)
│   ├── PriceUpdateSteps.java
│   └── BddTestRunner.java
├── api/                    # API 契约测试 (Karate)
│   └── pricing-api-test.feature
└── unit/                   # 单元测试 (JUnit)
    ├── PricingServiceTest.java
    └── PriceChangeOrderTest.java
```

## 工作流程

### 开发流程

```
1. 业务需求 → BDD 场景
   ├─ 产品经理编写业务场景
   ├─ 团队 Review 场景描述
   └─ 提交到 behaviors/ 目录

2. AI 生成代码
   ├─ 执行: claude-code --skill menu-pricing --input behaviors/menu/price_update.feature
   ├─ 生成: Domain, Service, Controller, Repository
   └─ 生成: Tests (Cucumber + Karate)

3. 开发人员 Review
   ├─ 检查生成的代码逻辑
   ├─ 补充边界条件处理
   └─ 优化性能和安全

4. 测试执行
   ├─ mvn test (运行所有测试)
   ├─ BDD 测试验证业务场景
   └─ API 测试验证接口契约

5. CI/CD 部署
   ├─ Git Commit → CI Pipeline
   ├─ 自动测试 + 质量门禁
   └─ 自动部署到环境
```

### 并行开发模式

```
┌─────────────────┐
│ BDD 场景完成    │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│ 前端   │ │ 后端   │
│ 开发   │ │ AI生成 │
│        │ │ 代码   │
│ Mock   │ │        │
│ 数据   │ │ 实现   │
└────────┘ └────────┘
    │         │
    └────┬────┘
         ▼
   ┌─────────┐
   │ 集成测试│
   └─────────┘
```

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.x
- **语言**: Java 17+
- **数据库**: PostgreSQL + Redis
- **消息队列**: Kafka
- **API**: RESTful + OpenAPI 3.0

### 测试技术
- **BDD**: Cucumber (支持中文)
- **API 测试**: Karate
- **单元测试**: JUnit 5 + AssertJ
- **Mock**: Mockito

### AI 工具
- **Claude Code**: 代码生成引擎
- **Skills**: 自定义代码生成规则

## 质量保障

### 测试覆盖率目标
- 单元测试覆盖率: **> 80%**
- BDD 场景覆盖率: **100%** (所有场景都有对应实现)
- API 契约覆盖率: **100%** (所有端点都有测试)

### CI/CD 质量门禁
```yaml
quality-gates:
  - code-compilation: ✅ 必须编译通过
  - unit-tests: ✅ 必须全部通过
  - bdd-tests: ✅ 必须全部通过
  - api-tests: ✅ 必须全部通过
  - code-coverage: ✅ 必须 > 80%
  - sonar-quality: ✅ 无阻断性问题
```

## 与现有系统集成

### OMS (订单系统)
```
BDD: behaviors/order/*.feature
Skill: order-management
```

### VIA (履约系统)
```
BDD: behaviors/fulfillment/*.feature
Skill: fulfillment
```

### Customization (定制系统)
```
BDD: behaviors/customization/*.feature
Skill: modifier-validation
```

## 效率提升

### 传统开发 vs SDD+BDD

| 指标               | 传统开发 | SDD+BDD | 提升   |
|--------------------|---------|---------|--------|
| 需求理解一致性     | 60%     | 95%     | +58%   |
| 代码编写时间       | 10天    | 3天     | -70%   |
| 测试编写时间       | 5天     | 1天     | -80%   |
| 缺陷发现阶段       | 测试期  | 开发期  | 前移60%|
| 回归测试时间       | 2天     | 2小时   | -92%   |

## 最佳实践

### BDD 场景编写
1. ✅ 使用业务语言，避免技术术语
2. ✅ 一个场景只测试一个业务行为
3. ✅ 使用数据表格提高可读性
4. ✅ 包含正常流程和异常流程
5. ✅ 场景应该独立，不依赖其他场景

### Skill 使用
1. ✅ 先 Review 生成的代码
2. ✅ 补充边界条件和异常处理
3. ✅ 优化性能敏感的代码
4. ✅ 添加详细的代码注释
5. ✅ 保持代码风格一致

### 测试执行
1. ✅ 本地开发时先运行单元测试
2. ✅ 提交前运行 BDD 测试
3. ✅ CI/CD 运行完整测试套件
4. ✅ 定期执行性能测试
5. ✅ 监控测试覆盖率趋势

---

**文档维护**: Platform Engineering Team
**最后更新**: 2026-02-04
