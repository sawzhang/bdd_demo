# Karate API 测试场景
# 对应 BDD 业务场景: behaviors/user/user_registration.feature

Feature: 用户注册 API 测试

  Background:
    * url 'http://localhost:8080/api/v1/users'
    * header Content-Type = 'application/json'
    * def registerRequest =
      """
      {
        "email": "newuser@example.com",
        "username": "新用户",
        "password": "SecurePass123!"
      }
      """

  @registration @happy-path
  Scenario: 成功注册新用户 API 测试
    # 对应 BDD 场景: behaviors/user/user_registration.feature - "成功注册新用户"

    Given path 'register'
    And request registerRequest
    When method post
    Then status 200
    And match response.id == '#number'
    And match response.email == 'newuser@example.com'
    And match response.username == '新用户'
    And match response.status == '待验证'
    And match response.registeredAt == '#string'
    And match response ==
      """
      {
        id: '#number',
        email: '#string',
        username: '#string',
        status: '待验证',
        registeredAt: '#string'
      }
      """

  @registration @validation
  Scenario: 邮箱格式验证 API 测试
    # 对应 BDD 场景: behaviors/user/user_registration.feature - "邮箱格式验证"

    * def invalidRequest = registerRequest
    * set invalidRequest.email = 'invalid-email'

    Given path 'register'
    And request invalidRequest
    When method post
    Then status 400
    And match response.error == '#string'
    And match response.message == '邮箱格式不正确'

  @registration @duplicate
  Scenario: 防止重复注册 API 测试
    # 对应 BDD 场景: behaviors/user/user_registration.feature - "防止重复注册"

    # 第一步: 先注册一个用户
    * def firstRequest = registerRequest
    * set firstRequest.email = 'dup@example.com'
    * set firstRequest.username = '用户1'

    Given path 'register'
    And request firstRequest
    When method post
    Then status 200

    # 第二步: 用同一邮箱再次注册
    * def duplicateRequest = registerRequest
    * set duplicateRequest.email = 'dup@example.com'
    * set duplicateRequest.username = '用户2'

    Given path 'register'
    And request duplicateRequest
    When method post
    Then status 400
    And match response.error == '#string'
    And match response.message == '该邮箱已被注册'

  @registration @password-strength
  Scenario Outline: 密码强度验证 API 测试 - <description>
    # 对应 BDD 场景: behaviors/user/user_registration.feature - "密码强度验证"

    * def pwdRequest = registerRequest
    * set pwdRequest.email = 'pwd-test-' + Math.random() + '@example.com'
    * set pwdRequest.password = '<password>'

    Given path 'register'
    And request pwdRequest
    When method post
    Then status <expectedStatus>
    And match response.message contains '<expectedMessage>'

    Examples:
      | password     | expectedStatus | expectedMessage              | description          |
      | 123          | 400            | 密码至少需要8个字符            | 密码过短              |
      | abcdefgh     | 400            | 密码必须包含数字和特殊字符      | 缺少数字和特殊字符     |
      | Abc123!@     | 200            | #ignore                      | 有效密码(含字母数字特殊) |
      | MyP@ss123    | 200            | #ignore                      | 有效密码(混合字符)     |

  @registration @email-verification
  Scenario: 邮箱验证流程 API 测试
    # 对应 BDD 场景: behaviors/user/user_registration.feature - "邮箱验证流程"

    # 第一步: 注册用户（获取验证令牌）
    Given path 'register'
    And request registerRequest
    When method post
    Then status 200
    And match response.status == '待验证'

    # 第二步: 使用验证令牌验证邮箱
    Given path 'verify-email'
    And param token = 'valid-token-123'
    When method get
    Then status 200
    And match response.email == '#string'
    And match response.status == '已激活'
    And match response.verifiedAt == '#string'
    And match response ==
      """
      {
        email: '#string',
        status: '已激活',
        verifiedAt: '#string'
      }
      """

  @registration @rate-limit
  Scenario: 防止恶意注册 API 测试
    # 对应 BDD 场景: behaviors/user/user_registration.feature - "防止恶意注册"
    # 模拟超过频率限制的请求（通过 X-Forwarded-For 传递 IP 地址）

    * header X-Forwarded-For = '192.168.1.100'

    * def rateLimitRequest = registerRequest
    * set rateLimitRequest.email = 'ratelimit@example.com'

    Given path 'register'
    And request rateLimitRequest
    When method post
    Then status 429
    And match response.error == 'RATE_LIMITED'
    And match response.message == '注册过于频繁，请稍后再试'

  @registration @check-email
  Scenario: 检查邮箱可用性 API 测试
    # 对应辅助接口: 防止重复注册的前端实时检查

    # 检查不存在的邮箱 — 应该可用
    Given path 'check-email'
    And param email = 'available@example.com'
    When method get
    Then status 200
    And match response.available == true

  @registration @verify-email @negative
  Scenario: 无效验证令牌 API 测试
    # 对应 BDD 场景: behaviors/user/user_registration.feature - "邮箱验证流程"（失败路径）

    Given path 'verify-email'
    And param token = 'invalid-token-xyz'
    When method get
    Then status 400
    And match response.error == 'INVALID_TOKEN'
    And match response.message == '无效的验证链接'
