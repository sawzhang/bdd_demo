package com.company.user.integration;

/**
 * 邮件服务接口（外部依赖）
 *
 * 对应 BDD 场景: behaviors/user/user_registration.feature
 * - 场景: 成功注册新用户 — "系统应该发送验证邮件到"
 *
 * 实际实现依赖邮件服务（如 SMTP / SendGrid 等），
 * 在 BDD 测试中使用 @MockBean 进行 Mock。
 *
 * @author AI-Generated via user-registration skill
 * @version 1.0.0
 * @since 2026-02-06
 */
public interface EmailService {

    /**
     * 发送验证邮件
     *
     * 对应 BDD 场景: "成功注册新用户"
     * - Then: 系统应该发送验证邮件到 "zhang@example.com"
     *
     * @param email 目标邮箱地址
     * @param verificationToken 验证令牌
     */
    void sendVerificationEmail(String email, String verificationToken);
}
