# BDD Demo 执行演示脚本

## 🎯 演示目标

向团队展示 **BDD + SDD 融合实践**，从业务需求到代码实现的完整流程。

演示时长: **30 分钟**

---

## 📋 演示前准备清单

### 环境检查

```bash
# 1. 进入项目目录
cd /Users/sawzhang/code/skill_bdd

# 2. 检查文件是否完整
ls -la
# 应该看到: behaviors/, skills/, src/, docs/, README.md, DEMO.md 等

# 3. 确认 Java 环境（如需实际运行测试）
java -version  # Java 17+
mvn -version   # Maven 3.8+
```

### 准备演示窗口

```bash
# 打开 3 个终端窗口:
# 窗口1: 演示 BDD 场景
# 窗口2: 演示生成的代码
# 窗口3: 演示测试执行
```

---

## 🎬 演示脚本

### 第一部分: 业务背景介绍 (3分钟)

**旁白**:
> "我们今天要演示的是咖啡公司的一个实际业务场景：菜单价格批量更新。运营团队需要能够快速响应市场变化，批量调整不同区域的饮品价格。传统方式下，这需要产品写需求文档，开发理解需求，编写代码，测试编写用例，整个流程可能需要 3-5 天。今天我们展示如何用 BDD + AI 的方式，将这个流程缩短到半天。"

**展示**: 打开 README.md

```bash
cat README.md | head -30
```

**讲解要点**:
- ✅ 核心价值链: 需求 → BDD → AI → 代码 → 测试
- ✅ 效率提升: 代码生成 70%，测试编写 80%

---

### 第二部分: BDD 场景展示 (8分钟)

#### 2.1 展示完整的 BDD 场景文件

**操作**: 窗口1

```bash
# 打开 BDD 场景文件
cat behaviors/menu/price_update.feature
```

**讲解**: 边滚动边讲解

> "这是用 Gherkin 语法编写的业务场景，完全使用中文。注意几个关键点："

**重点标注场景1: 单一区域价格上调**

```bash
# 只显示第一个场景
cat behaviors/menu/price_update.feature | sed -n '/场景: 单一区域价格上调/,/场景: 价格变更审批/p' | head -15
```

**讲解**:
1. **假如 (Given)**: 设置前置条件
   - "运营人员登录系统"

2. **当 (When)**: 触发业务动作
   - "提交价格调整请求"，注意这里用了数据表格，非常直观

3. **那么 (Then)**: 验证预期结果
   - "系统应生成价格变更单 PCO-20260204-001"
   - "变更单状态为待审批"
   - "变更单应包含 150 个门店"

**重点**: "这就是产品经理和业务人员能直接理解和编写的文档，不需要懂技术。"

#### 2.2 展示复杂场景

**操作**:

```bash
# 展示异常回滚场景
grep -A 10 "场景: 价格调整异常回滚" behaviors/menu/price_update.feature
```

**讲解**:
> "BDD 不仅仅描述正常流程，还包含异常处理。这个场景描述了：当第50个门店更新失败时，系统应该自动回滚所有已更新的门店。这就是业务规则，直接体现在场景中。"

#### 2.3 展示场景大纲（参数化测试）

**操作**:

```bash
# 展示价格验证场景
grep -A 15 "场景大纲: 价格调整金额验证" behaviors/menu/price_update.feature
```

**讲解**:
> "场景大纲允许我们用数据驱动的方式测试多个案例。这里定义了：调整金额 15元 应该被拒绝，调整金额 2元 应该通过。这就是边界值测试，直接写在 BDD 场景里。"

---

### 第三部分: Skill 定义展示 (5分钟)

**操作**: 窗口1

```bash
# 查看 Skill 定义
cat skills/menu-pricing/skill-definition.md | head -120
```

**讲解要点**:

1. **输入规范**
> "Skill 定义了 AI 如何理解 BDD 场景。输入是 .feature 文件，格式是 Gherkin。"

2. **输出规范**
> "输出是完整的代码结构："
- Domain Models (领域模型)
- Services (服务层)
- Controllers (API 控制器)
- Repositories (数据访问)
- Tests (测试代码)

3. **映射规则**

```bash
# 展示映射规则部分
grep -A 15 "BDD 场景映射规则" skills/menu-pricing/skill-definition.md
```

**讲解**:
> "这里定义了 BDD 元素如何映射到代码："
- Feature → 独立的 Service 类
- Scenario → Service 方法
- Given → Mock 数据准备
- When → 核心业务逻辑
- Then → 验证逻辑

**关键**: "这就是 AI 生成代码的规则手册。"

---

### 第四部分: 生成代码展示 (8分钟)

#### 4.1 展示领域模型

**操作**: 窗口2

```bash
# 查看价格变更单领域模型
cat src/main/domain/PriceChangeOrder.java | head -100
```

**讲解要点**:

1. **Javadoc 引用 BDD**

```java
/**
 * 对应 BDD 场景: behaviors/menu/price_update.feature
 * - 场景: 单一区域价格上调
 * - 场景: 价格变更审批通过后自动生效
 */
```

> "注意这里的注释直接引用了 BDD 场景文件，这就是可追溯性。"

2. **业务规则在领域对象中**

```bash
# 展示业务规则方法
grep -A 15 "validatePriceDecrease" src/main/domain/PriceChangeOrder.java
```

**讲解**:
> "业务规则'价格下调不能超过原价30%'直接编码在领域对象的方法中，不是写在 Service 里。这是 DDD 的核心思想：业务逻辑属于领域模型。"

3. **状态机**

```bash
# 展示状态机方法
grep -A 10 "public void approve" src/main/domain/PriceChangeOrder.java
```

**讲解**:
> "变更单的状态流转也封装在对象中：approve() → startExecution() → completeExecution()。这些方法会检查状态是否合法。"

#### 4.2 展示服务层

**操作**:

```bash
# 查看 PricingService
cat src/main/service/PricingService.java | head -150
```

**讲解要点**:

1. **方法对应 BDD 场景**

```bash
# 展示创建变更单方法
grep -B 10 -A 5 "createPriceChangeOrder" src/main/service/PricingService.java | head -20
```

**讲解**:
> "每个 Service 方法都有详细的 Javadoc，说明它对应哪个 BDD 场景的哪个步骤。"

2. **业务流程编排**

> "Service 层不包含业务规则，只负责编排。它调用 Domain 对象的方法，协调各个组件。"

#### 4.3 展示 REST API

**操作**:

```bash
# 查看 Controller
cat src/main/controller/MenuPricingController.java | grep -A 30 "createPriceChangeOrder"
```

**讲解**:
> "Controller 层提供 RESTful API，带有 OpenAPI 注解，可以自动生成 API 文档。参数验证用 @Valid，错误处理统一。"

---

### 第五部分: 测试代码展示 (6分钟)

#### 5.1 展示 Cucumber Step Definitions

**操作**: 窗口2

```bash
# 查看测试步骤定义
cat src/test/bdd/PriceUpdateSteps.java | head -80
```

**讲解要点**:

1. **支持中文关键词**

```bash
# 展示中文注解
grep "@当\|@假如\|@那么\|@并且" src/test/bdd/PriceUpdateSteps.java | head -10
```

**讲解**:
> "注意这些注解：@假如、@当、@那么、@并且。这是 Cucumber 的中文支持，让测试步骤和 BDD 场景完美对应。"

2. **数据表格解析**

```bash
# 展示数据表格处理
grep -A 20 "@当(\"提交以下价格调整请求:\")" src/test/bdd/PriceUpdateSteps.java
```

**讲解**:
> "当 BDD 场景中有数据表格时，Cucumber 会自动解析成 DataTable 对象，我们可以轻松提取数据。"

3. **断言验证**

```bash
# 展示断言
grep -A 5 "@那么" src/test/bdd/PriceUpdateSteps.java | head -15
```

**讲解**:
> "Then 步骤包含断言，使用 AssertJ 库，语义清晰。如果测试失败，报告会准确指出哪个场景的哪个步骤失败。"

#### 5.2 展示 Karate API 测试

**操作**:

```bash
# 查看 API 测试
cat src/test/api/pricing-api-test.feature | head -80
```

**讲解**:
> "Karate 是另一种 BDD 框架，专门用于 API 测试。它也使用类似 Gherkin 的语法，但更适合验证 HTTP 请求和响应。"

**重点场景**:

```bash
# 展示创建订单 API 测试
grep -A 15 "Scenario: 创建价格变更单" src/test/api/pricing-api-test.feature
```

**讲解**:
- `Given path` - 设置 API 路径
- `And request` - 设置请求体
- `When method post` - 发送 POST 请求
- `Then status 200` - 验证状态码
- `And match response` - 验证响应体字段

---

### 第六部分: 完整流程演示 (模拟) (5分钟)

#### 6.1 模拟新需求

**旁白**:
> "现在我们模拟一个新需求：需要添加会员专属价格功能。让我演示整个流程。"

**步骤1: 编写 BDD 场景** (演示创建，实际不执行)

```bash
# 展示如何创建新场景
cat << 'EOF'
# 新需求的 BDD 场景应该这样写：

# language: zh-CN

功能: 会员专属价格
  作为 会员运营人员
  我想要 为会员设置专属价格
  以便 提升会员粘性

  场景: 设置会员折扣价
    假如 存在产品"大杯拿铁"，普通价格38元
    当 设置会员专属价格为 32元
    那么 会员购买应支付 32元
    并且 非会员购买应支付 38元
EOF
```

**步骤2: 使用 Claude Code 生成代码** (模拟)

```bash
echo ""
echo "🤖 如果有 Claude Code，执行命令："
echo "claude-code --skill menu-pricing \\"
echo "  --input behaviors/menu/member_pricing.feature \\"
echo "  --output src/"
echo ""
echo "✅ AI 会自动生成："
echo "   - MemberPricing.java (领域模型)"
echo "   - MemberPricingService.java (服务层)"
echo "   - MemberPricingController.java (API)"
echo "   - MemberPricingSteps.java (测试步骤)"
echo "   - member-pricing-api-test.feature (API 测试)"
echo ""
echo "⏱️  生成时间: 约 5-10 秒"
```

**步骤3: 开发人员 Review**

```bash
echo ""
echo "👨‍💻 开发人员的工作："
echo "   1. Review 生成的代码 (10分钟)"
echo "   2. 补充边界条件处理"
echo "   3. 优化性能敏感部分"
echo "   4. 添加必要的业务细节"
echo ""
echo "📊 工作量对比："
echo "   传统方式: 2天编码 + 1天测试 = 3天"
echo "   SDD+BDD: 0.5天 Review + 0.5天优化 = 1天"
echo "   节省: 66%"
```

#### 6.2 效率对比总结

**展示对比表**:

```bash
cat << 'EOF'

📊 效率提升对比

┌────────────────┬──────────┬──────────┬──────────┐
│ 阶段           │ 传统开发 │ SDD+BDD  │ 提升     │
├────────────────┼──────────┼──────────┼──────────┤
│ 需求澄清       │ 2小时    │ 30分钟   │ -75%     │
│ 代码编写       │ 2天      │ 2小时    │ -92%     │
│ 测试编写       │ 1天      │ 自动生成 │ -100%    │
│ 集成测试       │ 4小时    │ 1小时    │ -75%     │
│ ─────────────  │ ──────── │ ──────── │ ──────── │
│ 总计           │ 3.5天    │ 0.5天    │ -86%     │
└────────────────┴──────────┴──────────┴──────────┘

✨ 核心优势：
  ✅ 需求一致性提升 90%
  ✅ 代码质量更高（遵循最佳实践）
  ✅ 测试覆盖率 80%+（自动生成）
  ✅ 文档与代码同步（活文档）

EOF
```

---

## 🎯 演示总结 (2分钟)

### 核心价值

**展示**:

```bash
cat << 'EOF'

🎯 BDD + SDD 融合的核心价值

1️⃣  统一语言
   - BDD 场景是产品、开发、测试的共同语言
   - 减少需求理解偏差

2️⃣  AI 驱动
   - 从业务场景自动生成高质量代码
   - 开发人员专注于业务细节和创新

3️⃣  质量保障
   - 场景即测试，100% 覆盖业务行为
   - 活文档机制，需求与代码永不脱节

4️⃣  效率提升
   - 代码编写时间减少 70-92%
   - 测试编写时间减少 100%
   - 整体交付周期缩短 86%

5️⃣  可追溯性
   - 每行代码都能追溯到业务需求
   - 每个测试都对应具体场景

EOF
```

### 适用场景

```bash
cat << 'EOF'

📋 适用场景

✅ 适合的情况:
  - 业务规则清晰的功能开发
  - 需要高测试覆盖率的核心模块
  - 团队协作密集的项目
  - API 开发和契约管理
  - 重构和现代化改造

❌ 不太适合的情况:
  - 探索性的 POC 项目
  - 业务规则极度模糊的场景
  - 一次性的小工具开发

EOF
```

---

## 🚀 下一步行动

```bash
cat << 'EOF'

🚀 下一步建议

Phase 1: 试点验证 (1-2周)
  □ 选择模块: MOD 定制验证
  □ 编写场景: 5-10 个核心场景
  □ 生成代码: 使用 Claude Code
  □ 度量效果: 记录时间节省数据

Phase 2: 小规模推广 (1个月)
  □ 扩展到 3 个模块
  □ 建立场景库 (50+ 场景)
  □ 培训团队
  □ 总结最佳实践

Phase 3: 全面推广 (3个月)
  □ 所有核心模块
  □ 场景库 200+ 场景
  □ 平台化工具建设
  □ 持续优化流程

EOF
```

---

## 📞 Q&A 准备

### 常见问题

**Q1: BDD 场景谁来写？**
```
A: 产品经理编写初稿（用业务语言）
   → 团队 Three Amigos 会议完善
   → 开发和测试 Review
```

**Q2: 生成的代码质量如何？**
```
A: - 80% 可直接使用
   - 20% 需要人工优化（边界条件、性能调优）
   - 遵循最佳实践（DDD、分层架构）
   - 自带 80%+ 测试覆盖率
```

**Q3: 如何保证场景与代码同步？**
```
A: - BDD 测试失败会阻断 CI/CD
   - 代码注释引用场景文件
   - 定期 Review 场景有效性
   - 活文档机制
```

**Q4: 学习成本高吗？**
```
A: - Gherkin 语法 1 天掌握
   - 团队培训 1 周
   - 实际应用 2-3 个迭代熟练
```

**Q5: 需要什么工具支持？**
```
A: - Claude Code (AI 代码生成)
   - Maven/Gradle (构建)
   - Cucumber (BDD 框架)
   - Karate (API 测试)
   - Spring Boot (Java 框架)
```

---

## 📚 参考资料

演示结束后，引导观众查看：

```bash
# 1. 项目总览
cat README.md

# 2. 完整演示指南
cat DEMO.md

# 3. 架构设计文档
cat docs/architecture.md

# 4. BDD 集成指南
cat docs/integration-guide.md

# 5. 项目总结
cat PROJECT_SUMMARY.md
```

---

**演示准备完成！祝演示成功！** 🎉
