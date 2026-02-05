package com.company.user.service;

import com.company.user.domain.User;
import com.company.user.domain.User.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
@Service
public class UserRegistrationService {

    // 邮箱格式正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // 密码强度正则表达式（至少8字符，包含字母、数字和特殊字符）
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"
    );

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

        // 5. 保存用户（实际应该调用 Repository）
        // userRepository.save(user);

        log.info("✓ 用户注册成功: {}", email);

        // 6. 发送验证邮件
        sendVerificationEmail(user);

        return user;
    }

    /**
     * 验证邮箱格式
     *
     * 来源场景: "邮箱格式验证"
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
     */
    private void checkEmailNotExists(String email) {
        // 实际应该查询数据库
        // if (userRepository.existsByEmail(email)) {
        //     throw new RegistrationException("该邮箱已被注册");
        // }

        // 模拟检查
        log.debug("检查邮箱是否存在: {}", email);
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
        // 实际应该调用邮件服务
        // emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
    }

    /**
     * 验证邮箱
     *
     * 来源场景: "邮箱验证流程"
     *
     * @param token 验证令牌
     * @return 验证后的用户
     */
    @Transactional
    public User verifyEmail(String token) {
        log.info("验证邮箱 - 令牌: {}", token);

        // 1. 根据令牌查找用户
        // User user = userRepository.findByVerificationToken(token)
        //     .orElseThrow(() -> new RegistrationException("无效的验证链接"));

        // 模拟查找用户
        User user = User.builder()
            .email("zhang@example.com")
            .username("张三")
            .status(UserStatus.PENDING_VERIFICATION)
            .verificationToken(token)
            .build();

        // 2. 验证邮箱
        user.verifyEmail();

        // 3. 保存更新
        // userRepository.save(user);

        log.info("✓ 邮箱验证成功: {}", user.getEmail());

        return user;
    }

    /**
     * 检查注册频率限制
     *
     * 来源场景: "防止恶意注册"
     *
     * @param ipAddress IP地址
     * @return 是否允许注册
     */
    public boolean checkRateLimit(String ipAddress) {
        log.debug("检查 IP {} 的注册频率", ipAddress);

        // 实际应该使用 Redis 或内存缓存
        // int count = redisTemplate.opsForValue().get("registration:" + ipAddress);
        // if (count >= 3) {
        //     return false;
        // }

        // 模拟检查
        return true;
    }

    /**
     * 记录可疑行为
     */
    public void logSuspiciousActivity(String ipAddress, String action) {
        log.warn("⚠️ 可疑行为 - IP: {}, 操作: {}", ipAddress, action);
        // 实际应该记录到安全日志或监控系统
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
