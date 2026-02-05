# Karate API 测试场景
# 对应 BDD 业务场景: behaviors/menu/price_update.feature

Feature: 菜单价格管理 API 测试

  Background:
    * url 'http://localhost:8080/api/v1/pricing'
    * header Content-Type = 'application/json'
    * def priceChangeRequest =
      """
      {
        "productCode": "LATTE-GRANDE",
        "productName": "大杯拿铁",
        "specification": "大杯",
        "targetRegion": "华东区",
        "adjustmentType": "INCREASE",
        "adjustmentAmount": 2.00,
        "effectiveDate": "2026-02-10T00:00:00",
        "changeReason": "市场调整",
        "createdBy": "张三",
        "affectedStoreIds": [1, 2, 3, 4, 5]
      }
      """

  @pricing @create-order
  Scenario: 创建价格变更单 API 测试
    # 对应 BDD 场景: "单一区域价格上调"

    Given path 'change-orders'
    And request priceChangeRequest
    When method post
    Then status 200
    And match response.orderNo == '#regex PCO-\\d{8}-\\d{3}'
    And match response.status == '待审批'
    And match response.affectedStoreCount == 5
    And match response.productName == '大杯拿铁'
    And match response.targetRegion == '华东区'
    And match response.originalPrice == 36.00
    And match response.newPrice == 38.00
    And match response.adjustmentAmount == 2.00

  @pricing @approve-order
  Scenario: 审批价格变更单 API 测试
    # 对应 BDD 场景: "价格变更审批通过后自动生效"

    # 1. 先创建一个价格变更单
    Given path 'change-orders'
    And request priceChangeRequest
    When method post
    Then status 200
    * def orderNo = response.orderNo

    # 2. 审批该变更单
    Given path 'change-orders', orderNo, 'approve'
    And param approver = '李四'
    When method post
    Then status 200
    And match response.orderNo == orderNo
    And match response.status == '已审批'

  @pricing @execute-order
  Scenario: 执行价格变更 API 测试
    # 对应 BDD 场景: "价格变更审批通过后自动生效"

    # 1. 创建价格变更单
    Given path 'change-orders'
    And request priceChangeRequest
    When method post
    Then status 200
    * def orderNo = response.orderNo

    # 2. 审批变更单
    Given path 'change-orders', orderNo, 'approve'
    And param approver = '李四'
    When method post
    Then status 200

    # 3. 执行价格变更
    Given path 'change-orders', orderNo, 'execute'
    When method post
    Then status 200
    And match response.success == true
    And match response.message contains '成功'

  @pricing @query-history
  Scenario: 查询价格历史 API 测试
    # 对应 BDD 场景: "价格变更历史查询"

    Given path 'history'
    And param productCode = 'LATTE-GRANDE'
    And param regionCode = '华东区'
    When method get
    Then status 200
    And match response == '#array'
    And match each response ==
      """
      {
        productName: '#string',
        regionName: '#string',
        originalPrice: '#number',
        newPrice: '#number',
        priceChangeDescription: '#string',
        effectiveTime: '#string',
        changeReason: '#string',
        approvedBy: '#string'
      }
      """

  @pricing @batch-create
  Scenario: 批量创建价格变更单 API 测试
    # 对应 BDD 场景: "多区域差异化定价"

    * def batchRequest =
      """
      {
        "products": [
          {
            "productCode": "LATTE-GRANDE",
            "productName": "大杯拿铁",
            "specification": "大杯"
          },
          {
            "productCode": "AMERICANO-GRANDE",
            "productName": "大杯美式",
            "specification": "大杯"
          }
        ],
        "regionalPrices": [
          {
            "region": "华东区",
            "price": 38.00,
            "storeIds": [1, 2, 3]
          },
          {
            "region": "华北区",
            "price": 36.00,
            "storeIds": [4, 5, 6]
          }
        ],
        "effectiveDate": "2026-02-10T00:00:00",
        "changeReason": "区域差异化定价",
        "createdBy": "张三"
      }
      """

    Given path 'change-orders/batch'
    And request batchRequest
    When method post
    Then status 200
    And match response == '#array'
    And match response == '#[4]'  # 2个产品 x 2个区域 = 4个变更单

  @pricing @validation @negative
  Scenario Outline: 价格调整金额验证 - 边界测试
    # 对应 BDD 场景: "价格调整金额验证"

    * def invalidRequest = priceChangeRequest
    * set invalidRequest.adjustmentAmount = <adjustmentAmount>

    Given path 'change-orders'
    And request invalidRequest
    When method post
    Then status <expectedStatus>
    And match response.message contains '<expectedMessage>'

    Examples:
      | adjustmentAmount | expectedStatus | expectedMessage            |
      | 15.00           | 400            | 单次调整金额不能超过10元    |
      | -15.00          | 400            | 价格下调不能超过原价30%     |
      | 2.00            | 200            | #ignore                    |

  @pricing @performance
  Scenario: 批量创建性能测试
    # 性能测试：批量创建100个价格变更单

    * def storeIds = karate.repeat(100, function(i){ return i + 1 })
    * set priceChangeRequest.affectedStoreIds = storeIds

    Given path 'change-orders'
    And request priceChangeRequest
    When method post
    Then status 200
    And match response.affectedStoreCount == 100
    And assert responseTime < 2000  # 响应时间应小于2秒
