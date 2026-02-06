package com.company.user.integration;

import java.time.Duration;

/**
 * 频率限制服务接口（外部依赖）
 *
 * 对应 BDD 场景: behaviors/user/user_registration.feature
 * - 场景: 防止恶意注册 — IP 频率限制
 *
 * 实际实现依赖 Redis 或内存缓存，
 * 在 BDD 测试中使用 @MockBean 进行 Mock。
 *
 * @author AI-Generated via user-registration skill
 * @version 1.0.0
 * @since 2026-02-06
 */
public interface RateLimitService {

    /**
     * 检查是否允许操作
     *
     * 对应 BDD 场景: "防止恶意注册"
     * - Given: IP地址在1分钟内已注册3次
     * - Then: 注册应该被阻止
     *
     * @param ipAddress  IP地址
     * @param action     操作类型（如 "registration"）
     * @param maxAttempts 最大尝试次数
     * @param window     时间窗口
     * @return 是否允许
     */
    boolean isAllowed(String ipAddress, String action, int maxAttempts, Duration window);

    /**
     * 记录一次尝试
     *
     * 对应 BDD 场景: "防止恶意注册"
     *
     * @param ipAddress IP地址
     * @param action    操作类型
     */
    void recordAttempt(String ipAddress, String action);
}
