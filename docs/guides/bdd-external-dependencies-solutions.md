# BDD 测试中的外部依赖处理方案

## 问题场景

在 BDD 测试中，业务流程常常依赖外部服务：
- **第三方 API**（支付网关、短信服务、地图服务）
- **内部微服务**（用户服务、库存服务、订单服务）
- **外部系统**（ERP、CRM、数据仓库）
- **消息队列**（Kafka、RabbitMQ）

这些依赖带来的挑战：
- 网络不稳定导致测试不可靠
- 外部服务维护期间测试中断
- 测试数据污染生产环境
- 测试执行缓慢
- 难以模拟异常场景

---

## 解决方案对比

| 方案 | 适用场景 | 优点 | 缺点 | 推荐度 |
|------|---------|------|------|--------|
| **Mock/Stub** | 单元/集成测试 | 快速、可控、离线 | 不测试真实集成 | ⭐⭐⭐⭐⭐ |
| **WireMock** | HTTP API 依赖 | 真实 HTTP 调用、录制回放 | 需要维护映射 | ⭐⭐⭐⭐⭐ |
| **Test Containers** | 需要真实服务 | 接近生产环境 | 启动慢、资源消耗大 | ⭐⭐⭐⭐ |
| **契约测试** | 微服务架构 | 保证接口兼容性 | 需要额外工具 | ⭐⭐⭐⭐ |
| **测试环境** | E2E 测试 | 完全真实 | 维护成本高、不稳定 | ⭐⭐⭐ |

---

## 方案一：Mockito Mock（推荐用于内部服务）

### 适用场景
- 内部微服务调用
- 业务逻辑测试
- 快速反馈循环

### 实现步骤

#### 1. 定义外部服务接口

```java
public interface InventoryService {
    /**
     * 检查库存是否充足
     */
    InventoryCheckResult checkInventory(String productCode, int quantity);

    /**
     * 锁定库存
     */
    boolean lockInventory(String productCode, int quantity, String orderId);

    /**
     * 释放库存
     */
    void releaseInventory(String orderId);
}

public interface PaymentGateway {
    /**
     * 创建支付订单
     */
    PaymentOrder createPayment(BigDecimal amount, String orderNo);

    /**
     * 查询支付状态
     */
    PaymentStatus queryPaymentStatus(String paymentId);
}
```

#### 2. 在 BDD 步骤中使用 Mock

```java
@Slf4j
@SpringBootTest
public class OrderFlowSteps {

    @Autowired
    private OrderService orderService;

    @MockBean  // Spring Boot 自动注入 Mock
    private InventoryService inventoryService;

    @MockBean
    private PaymentGateway paymentGateway;

    @Autowired
    private ScenarioContext scenarioContext;

    @假如("库存系统显示 {string} 库存充足")
    public void 库存系统显示库存充足(String productCode) {
        // Mock 库存检查返回成功
        when(inventoryService.checkInventory(eq(productCode), anyInt()))
            .thenReturn(InventoryCheckResult.sufficient());

        // Mock 库存锁定成功
        when(inventoryService.lockInventory(eq(productCode), anyInt(), anyString()))
            .thenReturn(true);

        log.info("✓ 已配置库存服务 Mock: {} 库存充足", productCode);
    }

    @假如("库存系统显示 {string} 库存不足")
    public void 库存系统显示库存不足(String productCode) {
        when(inventoryService.checkInventory(eq(productCode), anyInt()))
            .thenReturn(InventoryCheckResult.insufficient());

        log.info("✓ 已配置库存服务 Mock: {} 库存不足", productCode);
    }

    @假如("支付网关工作正常")
    public void 支付网关工作正常() {
        when(paymentGateway.createPayment(any(BigDecimal.class), anyString()))
            .thenAnswer(invocation -> {
                String orderNo = invocation.getArgument(1);
                return PaymentOrder.builder()
                    .paymentId("PAY-" + orderNo)
                    .status(PaymentStatus.PENDING)
                    .build();
            });

        when(paymentGateway.queryPaymentStatus(anyString()))
            .thenReturn(PaymentStatus.SUCCESS);

        log.info("✓ 已配置支付网关 Mock: 正常工作");
    }

    @假如("支付网关响应超时")
    public void 支付网关响应超时() {
        when(paymentGateway.createPayment(any(BigDecimal.class), anyString()))
            .thenThrow(new TimeoutException("支付网关超时"));

        log.info("✓ 已配置支付网关 Mock: 超时异常");
    }

    @当("用户下单购买 {int} 杯 {string}")
    public void 用户下单购买(int quantity, String productName) {
        try {
            Order order = orderService.createOrder(productName, quantity);
            scenarioContext.setCurrentOrder(order);
            scenarioContext.addState("order_created", true);
        } catch (Exception e) {
            scenarioContext.addState("order_created", false);
            scenarioContext.addState("error_message", e.getMessage());
        }
    }

    @那么("应该调用库存服务锁定库存")
    public void 应该调用库存服务锁定库存() {
        verify(inventoryService, times(1))
            .lockInventory(anyString(), anyInt(), anyString());

        log.info("✓ 验证通过：已调用库存锁定");
    }

    @那么("应该调用支付网关创建支付")
    public void 应该调用支付网关创建支付() {
        verify(paymentGateway, times(1))
            .createPayment(any(BigDecimal.class), anyString());

        log.info("✓ 验证通过：已调用支付网关");
    }
}
```

---

## 方案二：WireMock（推荐用于 HTTP API）

### 适用场景
- 第三方 HTTP API
- RESTful 服务
- 需要真实 HTTP 调用的场景

### 实现步骤

#### 1. 添加依赖

```xml
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>2.35.0</version>
    <scope>test</scope>
</dependency>
```

#### 2. 配置 WireMock

```java
@Slf4j
@SpringBootTest
public class ExternalApiSteps {

    private WireMockServer wireMockServer;

    @Autowired
    private ScenarioContext scenarioContext;

    @Before
    public void setupWireMock() {
        wireMockServer = new WireMockServer(options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @After
    public void teardownWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @假如("短信服务 API 工作正常")
    public void 短信服务API工作正常() {
        // 配置 WireMock 响应
        stubFor(post(urlEqualTo("/api/sms/send"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"code\": 0,\n" +
                    "  \"message\": \"success\",\n" +
                    "  \"data\": {\n" +
                    "    \"messageId\": \"SMS-123456\"\n" +
                    "  }\n" +
                    "}")));

        log.info("✓ WireMock 已配置: 短信服务正常响应");
    }

    @假如("短信服务 API 返回 {int} 错误")
    public void 短信服务API返回错误(int statusCode) {
        stubFor(post(urlEqualTo("/api/sms/send"))
            .willReturn(aResponse()
                .withStatus(statusCode)
                .withBody("{\n" +
                    "  \"code\": " + statusCode + ",\n" +
                    "  \"message\": \"Service unavailable\"\n" +
                    "}")));

        log.info("✓ WireMock 已配置: 短信服务返回 {} 错误", statusCode);
    }

    @假如("短信服务响应延迟 {int} 毫秒")
    public void 短信服务响应延迟(int delayMs) {
        stubFor(post(urlEqualTo("/api/sms/send"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(delayMs)
                .withBody("{\"code\": 0, \"message\": \"success\"}")));

        log.info("✓ WireMock 已配置: 延迟 {} 毫秒响应", delayMs);
    }

    @那么("应该向短信服务发送了 {int} 次请求")
    public void 应该向短信服务发送了次请求(int expectedCount) {
        verify(expectedCount, postRequestedFor(urlEqualTo("/api/sms/send")));

        log.info("✓ 验证通过：短信服务收到 {} 次请求", expectedCount);
    }
}
```

---

## 方案三：Test Containers（接近真实环境）

### 适用场景
- 需要真实数据库
- 需要真实消息队列
- E2E 测试

### 实现步骤

```java
@SpringBootTest
@Testcontainers
public class DatabaseDependencySteps {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    // 测试步骤使用真实的数据库和 Kafka
}
```

---

## 方案四：契约测试（推荐用于微服务）

### 使用 Pact 进行契约测试

```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "InventoryService")
public class InventoryServiceContractTest {

    @Pact(consumer = "OrderService")
    public RequestResponsePact checkInventoryPact(PactDslWithProvider builder) {
        return builder
            .given("产品 LATTE-001 有充足库存")
            .uponReceiving("检查库存请求")
            .path("/api/inventory/check")
            .method("POST")
            .body(new PactDslJsonBody()
                .stringValue("productCode", "LATTE-001")
                .integerType("quantity", 5))
            .willRespondWith()
            .status(200)
            .body(new PactDslJsonBody()
                .booleanValue("sufficient", true)
                .integerType("available", 100))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "checkInventoryPact")
    public void testCheckInventory(MockServer mockServer) {
        // 使用生成的契约进行测试
    }
}
```

---

## 最佳实践

### 1. 分层测试策略

```
┌─────────────────────────────────────┐
│   E2E 测试 (真实环境)                │  5%
│   - 关键业务流程                     │
│   - 使用测试环境                     │
└─────────────────────────────────────┘
           ▲
┌─────────────────────────────────────┐
│   集成测试 (Test Containers)         │  15%
│   - 跨服务交互                       │
│   - 真实数据库/消息队列              │
└─────────────────────────────────────┘
           ▲
┌─────────────────────────────────────┐
│   BDD 集成测试 (WireMock/Mock)       │  30%
│   - 业务场景验证                     │
│   - Mock 外部依赖                    │
└─────────────────────────────────────┘
           ▲
┌─────────────────────────────────────┐
│   单元测试 (Mockito)                 │  50%
│   - 业务逻辑                         │
│   - 完全隔离                         │
└─────────────────────────────────────┘
```

### 2. Mock 数据管理

创建 **Mock 数据工厂**：

```java
public class MockDataFactory {

    public static InventoryCheckResult sufficientInventory(String productCode) {
        return InventoryCheckResult.builder()
            .productCode(productCode)
            .sufficient(true)
            .available(100)
            .build();
    }

    public static PaymentOrder successfulPayment(String orderNo) {
        return PaymentOrder.builder()
            .paymentId("PAY-" + orderNo)
            .orderNo(orderNo)
            .status(PaymentStatus.SUCCESS)
            .amount(new BigDecimal("100.00"))
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static SmsResponse successfulSms() {
        return SmsResponse.builder()
            .code(0)
            .message("success")
            .messageId("SMS-" + UUID.randomUUID())
            .build();
    }
}
```

### 3. 环境配置

使用 **Spring Profile** 区分环境：

```yaml
# application-test.yml
spring:
  profiles: test

external:
  inventory-service:
    url: http://localhost:8089  # WireMock
    timeout: 1000

  payment-gateway:
    url: http://localhost:8089  # WireMock
    mock-enabled: true

# application-prod.yml
spring:
  profiles: prod

external:
  inventory-service:
    url: https://inventory.prod.example.com
    timeout: 5000

  payment-gateway:
    url: https://payment.gateway.com
    mock-enabled: false
```

### 4. 测试数据隔离

```java
@假如("准备测试数据")
public void 准备测试数据() {
    // 使用唯一标识避免冲突
    String testId = "TEST-" + UUID.randomUUID().toString().substring(0, 8);

    scenarioContext.addState("test_id", testId);
    scenarioContext.addState("test_user", "user-" + testId);
    scenarioContext.addState("test_order", "order-" + testId);
}
```

---

## 决策树

```
外部依赖处理方案选择
│
├─ 是 HTTP API？
│  ├─ 是 → 使用 WireMock
│  └─ 否 → 继续
│
├─ 是微服务架构？
│  ├─ 是 → 使用 契约测试 (Pact)
│  └─ 否 → 继续
│
├─ 需要真实环境？
│  ├─ 是 → 使用 Test Containers
│  └─ 否 → 使用 Mock/Stub
│
└─ 是关键业务流程？
   ├─ 是 → 添加 E2E 测试（测试环境）
   └─ 否 → 仅 Mock 即可
```

---

## 总结

| 需求 | 推荐方案 | 理由 |
|------|---------|------|
| 快速反馈 | Mock/Stub | 最快、最稳定 |
| HTTP API | WireMock | 真实 HTTP、易维护 |
| 真实数据库 | Test Containers | 接近生产 |
| 微服务 | 契约测试 | 保证兼容性 |
| E2E | 测试环境 | 完整验证 |

**核心原则**：
1. **金字塔原则**：大量 Mock 单测 + 少量真实集成测试
2. **稳定性优先**：避免依赖不稳定的外部服务
3. **快速反馈**：测试应在秒级完成
4. **可重复性**：同样的测试应该得到同样的结果
5. **数据隔离**：测试数据不污染生产环境
