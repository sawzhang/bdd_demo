# BDD + Claude Code 集成指南

## 概述

本指南详细说明了 BDD (Behavior Driven Development) 的完整执行机制，以及如何与 Claude Code 研发工具深度集成。

## BDD 完整执行机制

### 1. BDD 工作流程

```
┌──────────────────────────────────────────────────────────────┐
│                    BDD 生命周期                               │
└──────────────────────────────────────────────────────────────┘

第一阶段: 发现 (Discovery)
┌─────────────────────────────────────────┐
│ 1. 三方协作会议 (Three Amigos)          │
│    - 产品经理 (What)                    │
│    - 开发工程师 (How)                   │
│    - 测试工程师 (What if)               │
│                                         │
│ 2. 示例映射 (Example Mapping)           │
│    - 用户故事卡片                       │
│    - 规则卡片                           │
│    - 示例卡片                           │
│    - 疑问卡片                           │
└─────────────────────────────────────────┘
                    ↓
第二阶段: 形式化 (Formulation)
┌─────────────────────────────────────────┐
│ 3. 编写 Gherkin 场景                    │
│    Feature: 业务功能描述                │
│      Scenario: 具体场景                 │
│        Given: 前置条件                  │
│        When: 触发动作                   │
│        Then: 预期结果                   │
│        And: 补充条件                    │
└─────────────────────────────────────────┘
                    ↓
第三阶段: 自动化 (Automation)
┌─────────────────────────────────────────┐
│ 4. 生成 Step Definitions                │
│    - @Given 步骤实现                    │
│    - @When 步骤实现                     │
│    - @Then 步骤实现                     │
│                                         │
│ 5. 实现业务逻辑                         │
│    - Domain Models                      │
│    - Services                           │
│    - Controllers                        │
└─────────────────────────────────────────┘
                    ↓
第四阶段: 验证 (Validation)
┌─────────────────────────────────────────┐
│ 6. 执行 BDD 测试                        │
│    - Cucumber 运行场景                  │
│    - 生成可读报告                       │
│    - 验证业务行为                       │
└─────────────────────────────────────────┘
                    ↓
第五阶段: 演进 (Evolution)
┌─────────────────────────────────────────┐
│ 7. 持续维护                             │
│    - 场景即文档                         │
│    - 回归测试                           │
│    - 活文档 (Living Documentation)      │
└─────────────────────────────────────────┘
```

### 2. Gherkin 语法详解

#### 基本结构

```gherkin
# language: zh-CN

功能: 功能的简短描述
  作为 [角色]
  我想要 [功能]
  以便 [商业价值]

  背景:
    假如 所有场景共享的前置条件

  场景: 场景的简短描述
    假如 前置条件1
    并且 前置条件2
    当 执行某个动作
    那么 应该得到预期结果1
    并且 应该得到预期结果2

  场景大纲: 参数化场景
    假如 用户输入 <输入>
    那么 系统应返回 <输出>

    例子:
      | 输入 | 输出 |
      | A    | B    |
      | C    | D    |
```

#### 关键词映射

| Gherkin | 含义 | 代码映射 |
|---------|------|----------|
| Feature | 功能 | 独立的服务类 |
| Scenario | 场景 | 测试方法 |
| Given | 假如 | 测试数据准备 |
| When | 当 | 执行业务逻辑 |
| Then | 那么 | 断言验证 |
| And/But | 并且/但是 | 补充步骤 |
| Background | 背景 | @Before 钩子 |
| Scenario Outline | 场景大纲 | 参数化测试 |
| Examples | 例子 | 测试数据集 |

## 与 Claude Code 集成

### 集成架构

```
┌─────────────────────────────────────────────────────────────┐
│              Claude Code 集成层                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐          ┌──────────────┐                │
│  │ BDD 场景文件 │  ──────→ │ Claude Code  │                │
│  │ .feature     │          │ Skill Engine │                │
│  └──────────────┘          └──────┬───────┘                │
│                                    │                         │
│                    ┌───────────────┼───────────────┐        │
│                    │               │               │        │
│                    ▼               ▼               ▼        │
│            ┌──────────┐    ┌──────────┐    ┌──────────┐   │
│            │ 代码生成 │    │ 测试生成 │    │ 文档生成 │   │
│            └──────────┘    └──────────┘    └──────────┘   │
│                    │               │               │        │
│                    └───────────────┼───────────────┘        │
│                                    ▼                         │
│                          ┌──────────────────┐               │
│                          │ 完整的可执行代码 │               │
│                          └──────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

### 集成方式

#### 方式1: CLI 命令行集成

```bash
# 基础用法
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --output src/

# 仅生成特定层
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --generate domain,service

# 生成并立即测试
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --output src/ \
  --run-tests

# 生成 OpenAPI 契约
claude-code --skill menu-pricing \
  --input behaviors/menu/price_update.feature \
  --output-contract openapi/pricing-api.yaml
```

#### 方式2: 交互式对话集成

```
User: 基于 behaviors/menu/price_update.feature 生成菜单价格管理模块

Claude Code:
我将分析 BDD 场景并生成以下内容：

📋 BDD 场景分析:
  ✓ 6 个场景
  ✓ 识别出 4 个领域对象
  ✓ 提取了 15 条业务规则

🎯 即将生成:
  1. Domain Models (2 个类)
     - PriceChangeOrder
     - PriceHistory

  2. Services (3 个类)
     - PricingService
     - NotificationService
     - RollbackService

  3. Controllers (1 个类)
     - MenuPricingController

  4. Repositories (2 个接口)
     - PriceChangeOrderRepository
     - PriceHistoryRepository

  5. Tests
     - Cucumber Step Definitions
     - Karate API Tests

是否继续生成? [Y/n]

User: Y

Claude Code:
✅ 代码生成完成!

📊 统计:
  - 生成代码行数: 1,847 行
  - 测试覆盖率: 85%
  - 生成时间: 2.3 秒

🧪 下一步建议:
  1. Review 生成的代码
  2. 运行测试: mvn test
  3. 启动应用: mvn spring-boot:run
```

#### 方式3: Git Hook 集成

```bash
# .git/hooks/pre-commit

#!/bin/bash

# 检测 .feature 文件变更
CHANGED_FEATURES=$(git diff --cached --name-only --diff-filter=ACM | grep ".feature$")

if [ -n "$CHANGED_FEATURES" ]; then
    echo "检测到 BDD 场景文件变更，开始生成代码..."

    for feature in $CHANGED_FEATURES; do
        # 提取 skill 名称 (从文件路径)
        skill=$(echo $feature | cut -d'/' -f2)

        echo "处理: $feature (skill: $skill)"

        # 调用 Claude Code 生成代码
        claude-code --skill $skill \
          --input $feature \
          --output src/ \
          --auto-stage

        if [ $? -ne 0 ]; then
            echo "❌ 代码生成失败: $feature"
            exit 1
        fi
    done

    echo "✅ 代码生成完成"

    # 运行测试
    mvn test -q
    if [ $? -ne 0 ]; then
        echo "❌ 测试失败，请修复后重新提交"
        exit 1
    fi

    echo "✅ 所有测试通过"
fi
```

#### 方式4: CI/CD 集成

```yaml
# .github/workflows/bdd-validation.yml

name: BDD Validation & Code Generation

on:
  push:
    paths:
      - 'behaviors/**/*.feature'
  pull_request:
    paths:
      - 'behaviors/**/*.feature'

jobs:
  validate-and-generate:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Setup Claude Code
        run: |
          npm install -g @anthropics/claude-code

      - name: Detect changed features
        id: changes
        run: |
          CHANGED_FEATURES=$(git diff --name-only ${{ github.event.before }} ${{ github.sha }} | grep ".feature$" || true)
          echo "features=$CHANGED_FEATURES" >> $GITHUB_OUTPUT

      - name: Generate code from BDD scenarios
        if: steps.changes.outputs.features != ''
        run: |
          for feature in ${{ steps.changes.outputs.features }}; do
            skill=$(echo $feature | cut -d'/' -f2)
            echo "Generating code for $feature (skill: $skill)"

            claude-code --skill $skill \
              --input $feature \
              --output src/ \
              --generate-tests
          done

      - name: Run tests
        run: |
          mvn clean test

      - name: Generate test reports
        if: always()
        run: |
          mvn surefire-report:report

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: target/surefire-reports/

      - name: Comment PR with results
        if: github.event_name == 'pull_request'
        uses: actions/github-script@v6
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: '✅ BDD 场景验证通过，代码已自动生成'
            })
```

### 工作流示例

#### 完整开发流程

```
Day 1: 需求分析
┌──────────────────────────────────┐
│ 1. Three Amigos Meeting         │
│    - 讨论用户故事                │
│    - 绘制示例映射                │
│    - 识别疑问和风险              │
│                                  │
│ 2. 编写 BDD 场景                 │
│    - 产品经理编写 .feature 文件  │
│    - 团队 Review 并完善          │
│    - 提交到 Git                  │
└──────────────────────────────────┘
                ↓
Day 2: 代码生成与实现
┌──────────────────────────────────┐
│ 3. Claude Code 生成代码          │
│    $ claude-code --skill xxx     │
│    - 自动生成 Domain, Service    │
│    - 自动生成 Tests              │
│                                  │
│ 4. 开发人员 Review               │
│    - 检查业务逻辑                │
│    - 补充边界处理                │
│    - 优化性能                    │
│                                  │
│ 5. 运行 BDD 测试                 │
│    $ mvn test -Dtest=BddRunner   │
│    - 验证所有场景通过            │
└──────────────────────────────────┘
                ↓
Day 3: 集成与部署
┌──────────────────────────────────┐
│ 6. 集成测试                      │
│    - API 契约测试 (Karate)       │
│    - 端到端测试                  │
│                                  │
│ 7. 提交代码                      │
│    - Git Commit 触发 CI/CD       │
│    - 自动测试 + 质量门禁         │
│    - 自动部署                    │
└──────────────────────────────────┘
```

## 最佳实践

### BDD 场景编写

```gherkin
✅ Good Example:

场景: 用户下单大杯拿铁
  假如 用户已登录
  并且 购物车中有 1 杯"大杯拿铁"
  当 用户点击"提交订单"按钮
  那么 订单应创建成功
  并且 订单状态为"待支付"
  并且 订单金额应为 38.00 元

❌ Bad Example:

场景: 测试订单创建API
  假如 调用 POST /api/orders
  并且 请求体包含 {"product": "latte", "size": "grande"}
  那么 返回 HTTP 200
  并且 响应包含 order_id
```

**编写原则**:
1. ✅ 使用业务语言，不要暴露技术细节
2. ✅ 场景应该独立，不依赖其他场景
3. ✅ 一个场景只测试一个业务行为
4. ✅ 使用数据表格提高可读性
5. ✅ 包含正常流程和异常流程

### Claude Code Skill 配置

```markdown
# skill-definition.md 最佳实践

## 明确的输入输出规范
- 输入: 明确支持的 BDD 场景格式
- 输出: 详细的代码结构说明

## 清晰的映射规则
| BDD 元素 | 代码结构 |
|----------|----------|
| Feature  | Service  |
| Scenario | Method   |
| Given    | Setup    |
| When     | Action   |
| Then     | Assert   |

## 代码质量要求
- 覆盖率: > 80%
- 文档: 所有 public 方法有 Javadoc
- 规范: 遵循团队编码规范
```

### 测试执行策略

```bash
# 本地开发
mvn test -Dtest=*Test        # 快速单元测试
mvn test -Dtest=BddTestRunner  # BDD 场景测试

# CI/CD
mvn clean verify              # 完整测试套件
mvn test -P integration-test  # 集成测试
```

## 团队协作

### 角色分工

| 角色 | 职责 | 工具 |
|------|------|------|
| 产品经理 | 编写 BDD 场景 | Gherkin 编辑器 |
| 开发工程师 | Review 生成代码，补充逻辑 | Claude Code + IDE |
| 测试工程师 | 补充探索性测试 | Karate + JMeter |
| 架构师 | 维护 Skill 定义 | Claude Code Skill |

### 沟通机制

```
每日站会:
- 昨天完成的 BDD 场景
- 今天计划实现的场景
- 遇到的阻碍

每周 Review:
- BDD 场景覆盖率
- 测试通过率
- 代码质量趋势
```

## 常见问题

### Q1: BDD 场景应该写多详细?

**A**: 遵循"Just Enough"原则
- ✅ 详细到能让团队理解业务行为
- ❌ 不要详细到暴露实现细节

### Q2: 如何处理复杂的业务规则?

**A**: 使用场景大纲和数据表格
```gherkin
场景大纲: 价格计算
  当 购买 <数量> 杯咖啡
  那么 总价应为 <总价> 元

  例子:
    | 数量 | 总价  |
    | 1    | 38.00 |
    | 2    | 72.00 |  # 第二杯半价
    | 3    | 110.00|
```

### Q3: 生成的代码需要手动修改吗?

**A**: 是的，生成代码是基础
- Claude Code 生成 80% 的样板代码
- 开发人员补充 20% 的业务细节和优化

### Q4: 如何保证 BDD 场景与代码同步?

**A**: 活文档机制
- BDD 测试失败 → CI/CD 阻断
- 强制场景和代码保持一致
- 定期 Review 场景有效性

---

**维护**: Platform Engineering Team
**最后更新**: 2026-02-04
