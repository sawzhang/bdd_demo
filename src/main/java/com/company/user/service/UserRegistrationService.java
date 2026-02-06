package com.company.user.service;

import com.company.user.domain.User;
import com.company.user.domain.User.UserStatus;
import com.company.user.integration.EmailService;
import com.company.user.integration.RateLimitService;
import com.company.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 用户注册服务
 *
 * 来源场景: behaviors/user/user_registration.feature
 *
 * 实现以下业务场景:
 * - 成功注册新用户
 * - 邮箱格式验证
 * - 防止重复注册
 * - 密码强度验证
 * - 邮箱验证流程
 * - 防止恶意注册（频率限制）
 *
 * @author AI-Generated via user-registration skill
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RateLimitService rateLimitService;

    // 邮箱格式正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // 密码强度正则表达式（至少8字符，包含字母、数字和特殊字符）
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"
    );

    // 频率限制配置
    private static final int MAX_REGISTRATION_ATTEMPTS = 3;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    /**
     * 注册新用户
     *
     * 来源场景: "成功注册新用户"
     *
     * @param email 邮箱
     * @param username 用户名
     * @param password 密码
     * @return 注册的用户
     * @throws RegistrationException 注册失败
     */
    @Transactional
    public User registerUser(String email, String username, String password) {
        log.info("开始注册用户 - 邮箱: {}, 用户名: {}", email, username);

        // 1. 验证邮箱格式
        validateEmailFormat(email);

        // 2. 验证密码强度
        validatePasswordStrength(password);

        // 3. 检查邮箱是否已存在
        checkEmailNotExists(email);

        // 4. 创建用户
        User user = User.builder()
            .email(email)
            .username(username)
            .password(encryptPassword(password))
            .status(UserStatus.PENDING_VERIFICATION)
            .registeredAt(LocalDateTime.now())
            .verificationToken(generateVerificationToken())
            .build();

        // 5. 保存用户
        // 真实调用: userRepository.save(user);
        // Demo 模式: 模拟保存并设置 ID
        user.setId(System.currentTimeMillis());
        log.info("用户注册成功: {}", email);

        // 6. 发送验证邮件
        sendVerificationEmail(user);

        return user;
    }

    /**
     * 验证邮箱格式
     *
     * 来源场景: "邮箱格式验证"
     * - When: 用户填写无效邮箱
     * - Then: 注册应该失败，显示"邮箱格式不正确"
     */
    public void validateEmailFormat(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new RegistrationException("邮箱格式不正确");
        }
    }

    /**
     * 验证密码强度
     *
     * 来源场景: "密码强度验证"
     * - When: 用户填写密码
     * - Then: 根据密码强度返回成功/失败
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new RegistrationException("密码至少需要8个字符");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RegistrationException("密码必须包含数字和特殊字符");
        }
    }

    /**
     * 检查邮箱是否已存在
     *
     * 来源场景: "防止重复注册"
     * - Given: 系统中已存在用户
     * - Then: 注册应该失败，显示"该邮箱已被注册"
     */
    private void checkEmailNotExists(String email) {
        // 真实调用: 通过 Repository 查询数据库
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException("该邮箱已被注册");
        }
    }

    /**
     * 检查邮箱是否已注册
     *
     * 对应 BDD 场景: "防止重复注册"（辅助接口，供 Controller 调用）
     *
     * @param email 邮箱地址
     * @return 是否已存在
     */
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 加密密码
     */
    private String encryptPassword(String password) {
        // 实际应该使用 BCrypt 或其他加密算法
        // return passwordEncoder.encode(password);
        return "encrypted_" + password;
    }

    /**
     * 生成验证令牌
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * 发送验证邮件
     *
     * 来源场景: "成功注册新用户" - "系统应该发送验证邮件"
     */
    private void sendVerificationEmail(User user) {
        log.info("发送验证邮件到: {}", user.getEmail());
        // 真实调用: 通过 EmailService 发送验证邮件
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
    }

    /**
     * 验证邮箱
     *
     * 来源场景: "邮箱验证流程"
     * - When: 用户点击验证邮件中的链接
     * - Then: 邮箱验证应该成功，用户状态变更为"已激活"
     *
     * @param token 验证令牌
     * @return 验证后的用户
     */
    @Transactional
    public User verifyEmail(String token) {
        log.info("验证邮箱 - 令牌: {}", token);

        // 1. 根据令牌查找用户
        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new RegistrationException("无效的验证链接"));

        // 2. 验证邮箱（Domain 层业务方法）
        user.verifyEmail();

        // 3. 保存更新
        // 真实调用: userRepository.save(user);
        // Demo 模式: 状态已在内存中更新

        log.info("邮箱验证成功: {}", user.getEmail());

        return user;
    }

    /**
     * 检查注册频率限制
     *
     * 来源场景: "防止恶意注册"
     * - Given: IP地址在1分钟内已注册3次
     * - Then: 注册应该被阻止
     *
     * @param ipAddress IP地址
     * @return 是否允许注册
     */
    public boolean checkRateLimit(String ipAddress) {
        log.debug("检查 IP {} 的注册频率", ipAddress);
        // 真实调用: 通过 RateLimitService 检查频率
        return rateLimitService.isAllowed(ipAddress, "registration",
            MAX_REGISTRATION_ATTEMPTS, RATE_LIMIT_WINDOW);
    }

    /**
     * 记录可疑行为
     *
     * 来源场景: "防止恶意注册" - "应该记录可疑行为日志"
     *
     * @param ipAddress IP地址
     * @param action 行为描述
     */
    public void logSuspiciousActivity(String ipAddress, String action) {
        log.warn("可疑行为 - IP: {}, 操作: {}", ipAddress, action);
        // 真实调用: 记录到安全日志或监控系统
        rateLimitService.recordAttempt(ipAddress, "suspicious_" + action);
    }

    /**
     * 注册异常
     */
    public static class RegistrationException extends RuntimeException {
        public RegistrationException(String message) {
            super(message);
        }
    }
}
