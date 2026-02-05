# BDD 测试执行指南

## 🎯 测试执行方式

### 方式1: Maven 命令行

```bash
# 运行所有 BDD 测试
mvn test -Dtest=BddTestRunner

# 运行所有 Karate API 测试
mvn test -Dkarate.options="--tags @pricing"

# 运行特定标签的测试
mvn test -Dcucumber.filter.tags="@smoke"

# 生成详细报告
mvn clean test site
```

### 方式2: IDE 直接运行

在 IntelliJ IDEA 或 Eclipse 中：
1. 打开 `src/test/bdd/BddTestRunner.java`
2. 右键 → Run 'BddTestRunner'
3. 查看控制台输出

### 方式3: Gradle (如果使用 Gradle)

```bash
# 运行 Cucumber 测试
./gradlew cucumber

# 运行 Karate 测试
./gradlew karateTest
```

## 📊 测试报告

### Cucumber 报告

执行测试后会生成多种格式的报告：

```
target/cucumber-reports/
├── cucumber.html      # HTML 可视化报告
├── cucumber.json      # JSON 格式（可用于 CI/CD）
└── cucumber.xml       # JUnit XML 格式
```

**查看 HTML 报告**：
```bash
# Mac/Linux
open target/cucumber-reports/cucumber.html

# Windows
start target/cucumber-reports/cucumber.html

# 或在浏览器中打开
file:///path/to/project/target/cucumber-reports/cucumber.html
```

### 报告内容

HTML 报告包含：
- ✅ 场景通过/失败统计
- ✅ 步骤执行详情
- ✅ 执行时间
- ✅ 失败截图（如有）
- ✅ 错误堆栈跟踪

## 🏃 测试执行流程

### 完整执行流程

```bash
# 1. 清理之前的构建
mvn clean

# 2. 编译项目
mvn compile

# 3. 编译测试代码
mvn test-compile

# 4. 运行测试
mvn test

# 5. 查看报告
open target/cucumber-reports/cucumber.html
```

### 快速执行

```bash
# 一键完成所有步骤
mvn clean test && open target/cucumber-reports/cucumber.html
```

## 🎨 测试输出示例

### 控制台输出

```
功能: 菜单价格批量更新

  场景: 单一区域价格上调                   # behaviors/menu/price_update.feature:16
    假如 运营人员登录价格管理系统           # PriceUpdateSteps.运营人员登录价格管理系统()
    当 提交以下价格调整请求:               # PriceUpdateSteps.提交以下价格调整请求(DataTable)
    那么 系统应生成价格变更单              # PriceUpdateSteps.系统应生成价格变更单(String)
    并且 变更单状态为 "待审批"             # PriceUpdateSteps.变更单状态为(String)
    并且 变更单应包含 150 个门店           # PriceUpdateSteps.变更单应包含个门店(int)

6 Scenarios (6 passed)
25 Steps (25 passed)
0m2.347s
```

### 失败场景示例

如果测试失败，输出会显示：

```
场景: 单一区域价格上调                     # behaviors/menu/price_update.feature:16
  假如 运营人员登录价格管理系统             # PriceUpdateSteps.运营人员登录价格管理系统()
  当 提交以下价格调整请求:                 # PriceUpdateSteps.提交以下价格调整请求(DataTable)
  那么 系统应生成价格变更单                 # PriceUpdateSteps.系统应生成价格变更单(String)
    ❌ 断言失败:
    Expected: "PCO-20260204-001"
    Actual:   "PCO-20260204-002"
    at PriceUpdateSteps.系统应生成价格变更单(PriceUpdateSteps.java:123)

6 Scenarios (5 passed, 1 failed)
25 Steps (24 passed, 1 failed)
```

## 🏷️ 使用标签过滤

### 在 Feature 文件中添加标签

```gherkin
@pricing @smoke
场景: 单一区域价格上调
  假如 运营人员登录价格管理系统
  ...

@pricing @regression
场景: 价格调整异常回滚
  假如 价格变更单已审批通过
  ...
```

### 运行特定标签的测试

```bash
# 只运行 @smoke 标签的测试
mvn test -Dcucumber.filter.tags="@smoke"

# 运行 @pricing 和 @smoke 标签的测试（AND）
mvn test -Dcucumber.filter.tags="@pricing and @smoke"

# 运行 @pricing 或 @smoke 标签的测试（OR）
mvn test -Dcucumber.filter.tags="@pricing or @smoke"

# 排除 @skip 标签的测试
mvn test -Dcucumber.filter.tags="not @skip"
```

## 🔍 调试测试

### 启用详细日志

```bash
# Maven
mvn test -X -Dtest=BddTestRunner

# 在测试中添加日志
log.info("当前订单状态: {}", order.getStatus());
```

### 在 IDE 中调试

1. 在测试步骤中设置断点
2. Debug 模式运行 BddTestRunner
3. 逐步调试代码执行

## 📈 持续集成

### GitHub Actions 示例

```yaml
name: BDD Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run BDD Tests
        run: mvn clean test

      - name: Upload Test Reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: cucumber-reports
          path: target/cucumber-reports/
```

### Jenkins Pipeline 示例

```groovy
pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Publish Reports') {
            steps {
                cucumber buildStatus: 'UNSTABLE',
                    reportTitle: 'BDD Test Report',
                    fileIncludePattern: '**/*.json',
                    trendsLimit: 10
            }
        }
    }
}
```

## 💡 最佳实践

### 1. 测试数据管理

```gherkin
# 使用背景（Background）设置共享数据
背景:
  假如 系统中存在以下区域配置:
    | 区域   | 门店数量 |
    | 华东区 | 150     |
    | 华北区 | 120     |
```

### 2. 场景独立性

- ✅ 每个场景应该独立运行
- ✅ 不依赖其他场景的执行顺序
- ✅ 使用 @Before 和 @After 钩子清理数据

### 3. 命名规范

```gherkin
# 场景名称应该清晰描述业务行为
✅ 场景: 价格变更审批通过后自动生效
❌ 场景: 测试场景1

# 步骤应该使用业务语言
✅ 假如 运营人员登录价格管理系统
❌ 假如 用户点击登录按钮
```

### 4. 数据表格

```gherkin
# 使用数据表格提高可读性
当 提交以下价格调整请求:
  | 产品名称 | 规格 | 目标区域 | 调整金额 |
  | 大杯拿铁 | 大杯 | 华东区   | 2元      |
```

## 🚀 性能优化

### 并行执行

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

### 跳过不必要的测试

```bash
# 只运行 smoke 测试（快速验证）
mvn test -Dcucumber.filter.tags="@smoke"

# 跳过集成测试
mvn test -DskipITs
```

## 📚 相关资源

- **Cucumber 文档**: https://cucumber.io/docs/cucumber/
- **Karate 文档**: https://github.com/karatelabs/karate
- **AssertJ 文档**: https://assertj.github.io/doc/
- **项目示例**: `behaviors/menu/price_update.feature`

## ❓ 常见问题

### Q1: 测试报告在哪里？
A: `target/cucumber-reports/cucumber.html`

### Q2: 如何运行单个场景？
A: 使用行号：`mvn test -Dcucumber.features="src/test/resources/features/pricing.feature:16"`

### Q3: 如何查看详细日志？
A: 添加 `-X` 参数：`mvn test -X`

### Q4: 测试失败如何调试？
A:
1. 查看控制台输出的错误信息
2. 在 IDE 中 Debug 模式运行
3. 在关键步骤添加日志

### Q5: 如何生成覆盖率报告？
A: 使用 JaCoCo：
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

---

**最后更新**: 2026-02-04
**维护**: Platform Engineering Team
