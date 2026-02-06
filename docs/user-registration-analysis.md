# 用户注册场景 — AI 代码生成分析

> 基于 `behaviors/user/user_registration.feature` 的 6 个 BDD 场景，
> 参考 `skills/menu-pricing/skill-definition.md` 的生成规则模板。

---

## 1. BDD 场景清单

### 场景 1: 成功注册新用户
- **标签**: `@registration @happy-path`
- **前置条件**: 系统已启动；数据库中没有用户 "zhang@example.com"
- **关键步骤**:
  - Given: 用户访问注册页面
  - When: 用户填写注册信息（邮箱/用户名/密码），点击"注册"按钮
  - Then: 注册成功；发送验证邮件；用户状态为"待验证"；自动登录并跳转到首页

### 场景 2: 邮箱格式验证
- **标签**: `@registration @validation`
- **前置条件**: 系统已启动；数据库中没有目标用户
- **关键步骤**:
  - When: 填写无效邮箱 "invalid-email"，点击"注册"按钮
  - Then: 注册失败；显示错误消息"邮箱格式不正确"

### 场景 3: 防止重复注册
- **标签**: `@registration @duplicate`
- **前置条件**: 系统中已存在用户 "existing@example.com"
- **关键步骤**:
  - When: 用户尝试用已存在邮箱注册
  - Then: 注册失败；显示错误消息"该邮箱已被注册"

### 场景 4: 密码强度验证（场景大纲）
- **标签**: `@registration @password-strength`
- **前置条件**: 系统已启动；数据库中没有目标用户
- **关键步骤**:
  - When: 填写不同强度的密码，点击"注册"按钮
  - Then: 根据密码强度返回成功/失败和对应消息
- **测试数据**:
  | 密码 | 结果 | 消息 |
  |------|------|------|
  | 123 | 失败 | 密码至少需要8个字符 |
  | abcdefgh | 失败 | 密码必须包含数字和特殊字符 |
  | Abc123!@ | 成功 | 注册成功 |
  | MyP@ss123 | 成功 | 注册成功 |

### 场景 5: 邮箱验证流程
- **标签**: `@registration @email-verification`
- **前置条件**: 用户 "zhang@example.com" 已注册但未验证
- **关键步骤**:
  - When: 用户点击验证邮件中的链接
  - Then: 邮箱验证成功；用户状态变更为"已激活"；显示欢迎页面

### 场景 6: 防止恶意注册
- **标签**: `@registration @rate-limit`
- **前置条件**: IP地址 "192.168.1.100" 在1分钟内已注册3次
- **关键步骤**:
  - When: 该IP再次尝试注册
  - Then: 注册被阻止；显示错误消息"注册过于频繁，请稍后再试"；记录可疑行为日志

---

## 2. 实体模型分析

### 已有 User.java 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| email | String | 邮箱 |
| username | String | 用户名 |
| password | String | 密码（加密后） |
| status | UserStatus | 用户状态（PENDING_VERIFICATION / ACTIVATED / DISABLED） |
| registeredAt | LocalDateTime | 注册时间 |
| emailVerifiedAt | LocalDateTime | 邮箱验证时间 |
| verificationToken | String | 验证令牌 |

### 已有业务方法

| 方法 | 说明 | 来源场景 |
|------|------|----------|
| verifyEmail() | 验证邮箱，状态变更为 ACTIVATED | 邮箱验证流程 |
| isActivated() | 检查是否已激活 | — |
| needsVerification() | 检查是否需要验证 | — |

### 需要新增的属性/方法

| 新增项 | 类型 | 说明 | 来源场景 |
|--------|------|------|----------|
| registrationIp | String | 注册IP地址 | 防止恶意注册 |
| validateEmail(email) | static 方法 | 邮箱格式校验 | 邮箱格式验证 |
| validatePassword(pwd) | static 方法 | 密码强度校验 | 密码强度验证 |

> **注意**: 当前 User.java 的验证逻辑放在 Service 层，按照项目 DDD 原则（"Business logic belongs in domain models, not services"），建议将邮箱格式校验和密码强度校验移入 Domain 层。这是一个架构增强点，但不阻塞当前生成。

---

## 3. 业务规则提取

### 3.1 邮箱验证规则
- **规则 R1**: 邮箱格式必须匹配 `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`
- **规则 R2**: 邮箱不能重复注册（唯一性约束）
- **错误消息**: "邮箱格式不正确" / "该邮箱已被注册"

### 3.2 密码强度规则
- **规则 R3**: 密码长度至少8个字符
- **规则 R4**: 密码必须包含字母、数字和特殊字符
- **错误消息**: "密码至少需要8个字符" / "密码必须包含数字和特殊字符"
- **正则**: `^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$`

### 3.3 注册频率限制规则
- **规则 R5**: 同一IP地址在1分钟内最多注册3次
- **规则 R6**: 超过限制后阻止注册并记录可疑行为日志
- **错误消息**: "注册过于频繁，请稍后再试"

### 3.4 用户状态流转规则
- **规则 R7**: 新注册用户初始状态为 `PENDING_VERIFICATION`（待验证）
- **规则 R8**: 点击验证链接后状态变更为 `ACTIVATED`（已激活）
- **规则 R9**: 只有 PENDING_VERIFICATION 状态的用户可以执行邮箱验证

### 3.5 注册成功后续操作
- **规则 R10**: 注册成功后自动发送验证邮件
- **规则 R11**: 注册成功后自动登录并跳转到首页
- **规则 R12**: 密码存储前必须加密

---

## 4. API 端点设计

### 4.1 用户注册

| 属性 | 值 |
|------|-----|
| **端点** | `POST /api/v1/users/register` |
| **场景** | 成功注册新用户、邮箱格式验证、防止重复注册、密码强度验证 |
| **请求体** | `{ "email": "string", "username": "string", "password": "string" }` |
| **成功响应 (200)** | `{ "id": 1, "email": "string", "username": "string", "status": "待验证", "registeredAt": "datetime" }` |
| **失败响应 (400)** | `{ "error": "string", "message": "邮箱格式不正确 / 该邮箱已被注册 / 密码至少需要8个字符 / 密码必须包含数字和特殊字符" }` |
| **频率限制 (429)** | `{ "error": "RATE_LIMITED", "message": "注册过于频繁，请稍后再试" }` |

### 4.2 邮箱验证

| 属性 | 值 |
|------|-----|
| **端点** | `GET /api/v1/users/verify-email?token={token}` |
| **场景** | 邮箱验证流程 |
| **请求参数** | `token` (query parameter) |
| **成功响应 (200)** | `{ "email": "string", "status": "已激活", "verifiedAt": "datetime" }` |
| **失败响应 (400)** | `{ "error": "INVALID_TOKEN", "message": "无效的验证链接" }` |

### 4.3 检查邮箱可用性（辅助接口）

| 属性 | 值 |
|------|-----|
| **端点** | `GET /api/v1/users/check-email?email={email}` |
| **场景** | 防止重复注册（前端实时检查） |
| **请求参数** | `email` (query parameter) |
| **成功响应 (200)** | `{ "available": true/false }` |

---

## 5. 缺失代码清单

### 5.1 需要新建的文件

| 文件路径 | 说明 | 来源场景 |
|----------|------|----------|
| `src/main/java/com/company/user/controller/UserRegistrationController.java` | 用户注册 REST API 控制器 | 全部场景 |
| `src/main/java/com/company/user/repository/UserRepository.java` | 用户数据访问层 | 防止重复注册、邮箱验证流程 |
| `src/test/api/user-registration-api-test.feature` | Karate API 测试 | 全部场景 |
| `src/main/java/com/company/user/integration/EmailService.java` | 邮件服务接口 | 成功注册新用户 |
| `src/main/java/com/company/user/integration/RateLimitService.java` | 频率限制服务接口 | 防止恶意注册 |

### 5.2 需要增强的已有文件

| 文件路径 | 增强内容 | 来源场景 |
|----------|----------|----------|
| `User.java` | 添加 `registrationIp` 属性；添加 `validateEmail()` / `validatePassword()` 静态方法（可选，视 DDD 纯度要求） | 邮箱格式验证、密码强度验证、防止恶意注册 |
| `UserRegistrationService.java` | 注入 `UserRepository`、`EmailService`、`RateLimitService`；`checkEmailNotExists()` 调用 Repository 实现；`sendVerificationEmail()` 调用 EmailService；`checkRateLimit()` 调用 RateLimitService | 全部场景 |
| `UserRegistrationSteps.java` | 使用 `@MockBean` 替代手动模拟；增加邮件发送验证（`verify(emailService)`） | 成功注册新用户、邮箱验证流程 |

---

## 6. BDD → 代码映射表

| 场景 | Service 方法 | API 端点 | Cucumber Step 方法 | Karate 测试场景 |
|------|-------------|----------|-------------------|----------------|
| 成功注册新用户 | `registerUser(email, username, password)` | `POST /api/v1/users/register` | `用户填写注册信息()` → `用户点击注册按钮()` → `用户注册应该成功()` | `@registration @happy-path` |
| 邮箱格式验证 | `validateEmailFormat(email)` (在 `registerUser` 内调用) | `POST /api/v1/users/register` (400 response) | `用户填写无效邮箱()` → `用户点击注册按钮()` → `注册应该失败()` | `@registration @validation` |
| 防止重复注册 | `checkEmailNotExists(email)` (在 `registerUser` 内调用) | `POST /api/v1/users/register` (400 response) | `系统中已存在用户()` → `用户尝试用邮箱注册()` → `注册应该失败()` | `@registration @duplicate` |
| 密码强度验证 | `validatePasswordStrength(password)` (在 `registerUser` 内调用) | `POST /api/v1/users/register` (400/200 response) | `用户填写密码()` → `用户点击注册按钮()` → `注册结果应为()` | `@registration @password-strength` |
| 邮箱验证流程 | `verifyEmail(token)` | `GET /api/v1/users/verify-email?token=xxx` | `用户已注册但未验证()` → `用户点击验证邮件中的链接()` → `邮箱验证应该成功()` | `@registration @email-verification` |
| 防止恶意注册 | `checkRateLimit(ipAddress)` + `logSuspiciousActivity()` | `POST /api/v1/users/register` (429 response) | `IP地址在1分钟内已注册3次()` → `该IP再次尝试注册()` → `注册应该被阻止()` | `@registration @rate-limit` |

---

## 7. 给 domain-coder 的指令

### 7.1 增强 User.java

**文件**: `src/main/java/com/company/user/domain/User.java`

**需要添加的内容**:
1. 添加属性 `registrationIp`（String 类型），用于记录注册IP地址
2. （可选）添加 `validateEmail(String email)` 和 `validatePassword(String password)` 的静态验证方法，将验证逻辑从 Service 收拢到 Domain，符合 Rich Domain Model 原则

### 7.2 新建 UserRegistrationController.java

**文件**: `src/main/java/com/company/user/controller/UserRegistrationController.java`

**参考模板**: `MenuPricingController.java`

**生成内容**:
- 类注解: `@RestController`, `@RequestMapping("/api/v1/users")`, `@Tag`, `@Slf4j`, `@RequiredArgsConstructor`, `@Validated`
- 注入 `UserRegistrationService`
- 端点方法:
  1. `POST /register` — 调用 `registrationService.registerUser()`，捕获 `RegistrationException` 返回 400，频率限制返回 429
  2. `GET /verify-email` — 调用 `registrationService.verifyEmail(token)`
  3. `GET /check-email` — 调用 `registrationService.checkEmailExists(email)` 返回可用性
- 内部 DTO 类:
  - `RegisterRequest`: email, username, password
  - `RegisterResponse`: id, email, username, status, registeredAt
  - `VerifyEmailResponse`: email, status, verifiedAt
  - `CheckEmailResponse`: available (boolean)
  - `ErrorResponse`: error, message
- 每个方法的 Javadoc 必须引用来源 BDD 场景

### 7.3 新建 UserRepository.java

**文件**: `src/main/java/com/company/user/repository/UserRepository.java`

**参考模板**: `PriceChangeOrderRepository.java`

**生成内容**:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(String token);
}
```

### 7.4 新建 EmailService.java

**文件**: `src/main/java/com/company/user/integration/EmailService.java`

**说明**: 外部依赖接口，用于发送验证邮件。测试中使用 `@MockBean` 模拟。

**生成内容**:
```java
public interface EmailService {
    void sendVerificationEmail(String email, String verificationToken);
}
```

### 7.5 新建 RateLimitService.java

**文件**: `src/main/java/com/company/user/integration/RateLimitService.java`

**说明**: 频率限制服务接口。实际实现依赖 Redis，测试中使用 `@MockBean` 模拟。

**生成内容**:
```java
public interface RateLimitService {
    boolean isAllowed(String ipAddress, String action, int maxAttempts, Duration window);
    void recordAttempt(String ipAddress, String action);
}
```

### 7.6 增强 UserRegistrationService.java

**文件**: `src/main/java/com/company/user/service/UserRegistrationService.java`

**增强要点**:
1. 注入 `UserRepository`、`EmailService`、`RateLimitService`（通过构造器注入）
2. `checkEmailNotExists()`: 替换模拟代码为 `userRepository.existsByEmail(email)` 实际查询
3. `registerUser()`: 调用 `userRepository.save(user)` 保存用户
4. `sendVerificationEmail()`: 调用 `emailService.sendVerificationEmail()` 发送邮件
5. `verifyEmail()`: 通过 `userRepository.findByVerificationToken(token)` 查找用户，验证后调用 `userRepository.save(user)` 保存
6. `checkRateLimit()`: 调用 `rateLimitService.isAllowed()` 检查频率
7. 添加 `@RequiredArgsConstructor` 注解替代手动构造器
8. 所有方法的 Javadoc 保持不变，确保 BDD 场景引用完整

---

## 8. 给 test-coder 的指令

### 8.1 增强 UserRegistrationSteps.java

**文件**: `src/test/java/com/company/user/test/bdd/UserRegistrationSteps.java`

**增强要点**:
1. 添加 `@MockBean` 注解模拟外部依赖:
   ```java
   @MockBean private UserRepository userRepository;
   @MockBean private EmailService emailService;
   @MockBean private RateLimitService rateLimitService;
   ```
2. 添加 `@Before` 钩子（Cucumber `io.cucumber.java.Before`）在每个场景前重置 Mock:
   ```java
   @Before
   public void setUp() {
       reset(userRepository, emailService, rateLimitService);
       when(rateLimitService.isAllowed(anyString(), anyString(), anyInt(), any()))
           .thenReturn(true); // 默认允许
   }
   ```
3. 在 `系统中已存在用户()` 步骤中配置 Mock:
   ```java
   when(userRepository.existsByEmail(email)).thenReturn(true);
   ```
4. 在 `IP地址在1分钟内已注册3次()` 步骤中配置 Mock:
   ```java
   when(rateLimitService.isAllowed(eq(ipAddress), eq("registration"), eq(3), any()))
       .thenReturn(false);
   ```
5. 在 `系统应该发送验证邮件到()` 步骤中添加验证:
   ```java
   verify(emailService).sendVerificationEmail(eq(email), anyString());
   ```
6. 在 `应该记录可疑行为日志()` 步骤中，验证日志记录（通过 Mock 或日志捕获）

### 8.2 新建 user-registration-api-test.feature

**文件**: `src/test/api/user-registration-api-test.feature`

**参考模板**: `pricing-api-test.feature`

**生成内容**:

```
Feature: 用户注册 API 测试

  Background:
    * url 'http://localhost:8080/api/v1/users'
    * header Content-Type = 'application/json'

  @registration @happy-path
  Scenario: 成功注册新用户 API 测试
    # 对应 BDD 场景: "成功注册新用户"
    Given path 'register'
    And request { "email": "newuser@example.com", "username": "新用户", "password": "SecurePass123!" }
    When method post
    Then status 200
    And match response.email == 'newuser@example.com'
    And match response.username == '新用户'
    And match response.status == '待验证'
    And match response.registeredAt == '#string'
    And match response.id == '#number'

  @registration @validation
  Scenario: 邮箱格式验证 API 测试
    # 对应 BDD 场景: "邮箱格式验证"
    Given path 'register'
    And request { "email": "invalid-email", "username": "测试", "password": "SecurePass123!" }
    When method post
    Then status 400
    And match response.message == '邮箱格式不正确'

  @registration @duplicate
  Scenario: 防止重复注册 API 测试
    # 对应 BDD 场景: "防止重复注册"
    # 先注册一个用户
    Given path 'register'
    And request { "email": "dup@example.com", "username": "用户1", "password": "SecurePass123!" }
    When method post
    Then status 200

    # 再用同一邮箱注册
    Given path 'register'
    And request { "email": "dup@example.com", "username": "用户2", "password": "SecurePass123!" }
    When method post
    Then status 400
    And match response.message == '该邮箱已被注册'

  @registration @password-strength
  Scenario Outline: 密码强度验证 API 测试
    # 对应 BDD 场景: "密码强度验证"
    Given path 'register'
    And request { "email": "pwd-test@example.com", "username": "测试", "password": "<password>" }
    When method post
    Then status <expectedStatus>
    And match response.message contains '<expectedMessage>'

    Examples:
      | password     | expectedStatus | expectedMessage              |
      | 123          | 400            | 密码至少需要8个字符            |
      | abcdefgh     | 400            | 密码必须包含数字和特殊字符      |
      | Abc123!@     | 200            | #ignore                      |

  @registration @email-verification
  Scenario: 邮箱验证流程 API 测试
    # 对应 BDD 场景: "邮箱验证流程"
    Given path 'verify-email'
    And param token = 'valid-token-123'
    When method get
    Then status 200
    And match response.status == '已激活'
    And match response.verifiedAt == '#string'

  @registration @rate-limit
  Scenario: 防止恶意注册 API 测试
    # 对应 BDD 场景: "防止恶意注册"
    # 模拟超过频率限制的请求（需要通过 header 传递 IP 或由服务端识别）
    * header X-Forwarded-For = '192.168.1.100'
    Given path 'register'
    And request { "email": "ratelimit@example.com", "username": "测试", "password": "SecurePass123!" }
    When method post
    Then status 429
    And match response.message == '注册过于频繁，请稍后再试'
```

### 8.3 测试执行命令

```bash
# 运行用户注册 BDD 测试
mvn test -Dtest=BddTestRunner -Dcucumber.filter.tags="@registration"

# 运行用户注册 API 测试
mvn test -Dkarate.options="--tags @registration"

# 运行所有用户注册相关测试
mvn test -Dtest=BddTestRunner,KarateRunner -Dcucumber.filter.tags="@registration"
```

---

## 附录: 文件生成优先级

| 优先级 | 文件 | 原因 |
|--------|------|------|
| P0 | UserRepository.java | Service 层依赖，其他文件都需要它 |
| P0 | EmailService.java | Service 层依赖接口 |
| P0 | RateLimitService.java | Service 层依赖接口 |
| P1 | UserRegistrationController.java | API 层，依赖 Service |
| P1 | UserRegistrationService.java (增强) | 注入真实依赖替换模拟代码 |
| P1 | User.java (增强) | 添加 registrationIp 属性 |
| P2 | UserRegistrationSteps.java (增强) | 添加 MockBean 验证 |
| P2 | user-registration-api-test.feature | Karate API 合约测试 |
