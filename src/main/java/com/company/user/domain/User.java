package com.company.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户领域模型
 *
 * 来源场景: behaviors/user/user_registration.feature
 * 场景: 用户注册
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密后）
     */
    private String password;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 注册时间
     */
    private LocalDateTime registeredAt;

    /**
     * 邮箱验证时间
     */
    private LocalDateTime emailVerifiedAt;

    /**
     * 验证令牌
     */
    private String verificationToken;

    /**
     * 注册IP地址
     *
     * 对应 BDD 场景: "防止恶意注册"
     * - Given: IP地址在1分钟内已注册3次
     * - Then: 注册应该被阻止
     */
    private String registrationIp;

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        /**
         * 待验证
         */
        PENDING_VERIFICATION("待验证"),

        /**
         * 已激活
         */
        ACTIVATED("已激活"),

        /**
         * 已禁用
         */
        DISABLED("已禁用");

        private final String description;

        UserStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 业务方法: 验证邮箱
     *
     * 来源场景: "邮箱验证流程"
     */
    public void verifyEmail() {
        if (this.status != UserStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("只有待验证状态的用户才能验证邮箱");
        }

        this.status = UserStatus.ACTIVATED;
        this.emailVerifiedAt = LocalDateTime.now();
        this.verificationToken = null; // 清除验证令牌
    }

    /**
     * 业务方法: 检查是否已激活
     */
    public boolean isActivated() {
        return this.status == UserStatus.ACTIVATED;
    }

    /**
     * 业务方法: 检查是否需要验证
     */
    public boolean needsVerification() {
        return this.status == UserStatus.PENDING_VERIFICATION;
    }
}
