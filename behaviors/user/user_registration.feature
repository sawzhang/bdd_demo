# language: zh-CN

功能: 用户注册
  作为 新用户
  我想要 注册账号
  以便 使用系统功能

  背景:
    假如 系统已启动
    并且 数据库中没有用户 "zhang@example.com"

  @registration @happy-path
  场景: 成功注册新用户
    假如 用户访问注册页面
    当 用户填写注册信息:
      | 字段   | 值                  |
      | 邮箱   | zhang@example.com   |
      | 用户名 | 张三                 |
      | 密码   | SecurePass123!      |
    并且 用户点击"注册"按钮
    那么 用户注册应该成功
    并且 系统应该发送验证邮件到 "zhang@example.com"
    并且 用户状态应为 "待验证"
    并且 应该自动登录并跳转到首页

  @registration @validation
  场景: 邮箱格式验证
    假如 用户访问注册页面
    当 用户填写无效邮箱 "invalid-email"
    并且 用户点击"注册"按钮
    那么 注册应该失败
    并且 应该显示错误消息 "邮箱格式不正确"

  @registration @duplicate
  场景: 防止重复注册
    假如 系统中已存在用户 "existing@example.com"
    当 用户尝试用邮箱 "existing@example.com" 注册
    那么 注册应该失败
    并且 应该显示错误消息 "该邮箱已被注册"

  @registration @password-strength
  场景大纲: 密码强度验证
    假如 用户访问注册页面
    当 用户填写密码 "<密码>"
    并且 用户点击"注册"按钮
    那么 注册结果应为 "<结果>"
    并且 应该显示消息 "<消息>"

    例子: 各种密码强度
      | 密码          | 结果 | 消息                           |
      | 123          | 失败 | 密码至少需要8个字符             |
      | abcdefgh     | 失败 | 密码必须包含数字和特殊字符       |
      | Abc123!@     | 成功 | 注册成功                       |
      | MyP@ss123    | 成功 | 注册成功                       |

  @registration @email-verification
  场景: 邮箱验证流程
    假如 用户 "zhang@example.com" 已注册但未验证
    当 用户点击验证邮件中的链接
    那么 邮箱验证应该成功
    并且 用户状态应变更为 "已激活"
    并且 应该显示欢迎页面

  @registration @rate-limit
  场景: 防止恶意注册
    假如 IP地址 "192.168.1.100" 在1分钟内已注册3次
    当 该IP再次尝试注册
    那么 注册应该被阻止
    并且 应该显示错误消息 "注册过于频繁，请稍后再试"
    并且 应该记录可疑行为日志
