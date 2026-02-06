package com.company.user.controller;

import com.company.user.domain.User;
import com.company.user.service.UserRegistrationService;
import com.company.user.service.UserRegistrationService.RegistrationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 用户注册 REST API 控制器
 *
 * 对应 BDD 场景: behaviors/user/user_registration.feature
 *
 * API 端点:
 * - POST /api/v1/users/register       用户注册
 * - GET  /api/v1/users/verify-email    邮箱验证
 * - GET  /api/v1/users/check-email     检查邮箱可用性
 *
 * @author AI-Generated via user-registration skill
 * @version 1.0.0
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户注册管理", description = "User Registration Management APIs")
public class UserRegistrationController {

    private final UserRegistrationService registrationService;

    /**
     * 用户注册
     *
     * 对应 BDD 场景:
     * - "成功注册新用户"
     * - "邮箱格式验证"
     * - "防止重复注册"
     * - "密码强度验证"
     * - "防止恶意注册"
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "提交注册信息，创建新用户并发送验证邮件")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false)
            @Parameter(description = "客户端IP地址") String clientIp) {

        log.info("接收到注册请求 - 邮箱: {}, 用户名: {}", request.getEmail(), request.getUsername());

        try {
            // 检查频率限制
            String ipAddress = clientIp != null ? clientIp : "unknown";
            if (!registrationService.checkRateLimit(ipAddress)) {
                log.warn("注册频率超限 - IP: {}", ipAddress);
                registrationService.logSuspiciousActivity(ipAddress, "注册频率超限");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse("RATE_LIMITED", "注册过于频繁，请稍后再试"));
            }

            User user = registrationService.registerUser(
                request.getEmail(),
                request.getUsername(),
                request.getPassword()
            );

            RegisterResponse response = RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .status(user.getStatus().getDescription())
                .registeredAt(user.getRegisteredAt())
                .build();

            return ResponseEntity.ok(response);

        } catch (RegistrationException e) {
            log.warn("注册失败 - 原因: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("REGISTRATION_FAILED", e.getMessage()));
        }
    }

    /**
     * 邮箱验证
     *
     * 对应 BDD 场景: "邮箱验证流程"
     * - When: 用户点击验证邮件中的链接
     * - Then: 邮箱验证应该成功，用户状态变更为"已激活"
     *
     * @param token 验证令牌
     * @return 验证结果
     */
    @GetMapping("/verify-email")
    @Operation(summary = "邮箱验证", description = "通过验证令牌激活用户邮箱")
    public ResponseEntity<?> verifyEmail(
            @RequestParam @Parameter(description = "验证令牌") String token) {

        log.info("接收到邮箱验证请求 - 令牌: {}", token);

        try {
            User user = registrationService.verifyEmail(token);

            VerifyEmailResponse response = VerifyEmailResponse.builder()
                .email(user.getEmail())
                .status(user.getStatus().getDescription())
                .verifiedAt(user.getEmailVerifiedAt())
                .build();

            return ResponseEntity.ok(response);

        } catch (RegistrationException e) {
            log.warn("邮箱验证失败 - 原因: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_TOKEN", e.getMessage()));
        }
    }

    /**
     * 检查邮箱可用性
     *
     * 对应 BDD 场景: "防止重复注册"（前端实时检查辅助接口）
     *
     * @param email 邮箱地址
     * @return 可用性结果
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱可用性", description = "检查邮箱是否已被注册")
    public ResponseEntity<CheckEmailResponse> checkEmail(
            @RequestParam @Parameter(description = "邮箱地址") String email) {

        log.info("检查邮箱可用性 - 邮箱: {}", email);

        boolean exists = registrationService.checkEmailExists(email);

        return ResponseEntity.ok(new CheckEmailResponse(!exists));
    }

    // ==================== Request/Response DTOs ====================

    /**
     * 注册请求
     */
    @lombok.Data
    public static class RegisterRequest {
        private String email;
        private String username;
        private String password;
    }

    /**
     * 注册成功响应
     */
    @lombok.Data
    @lombok.Builder
    public static class RegisterResponse {
        private Long id;
        private String email;
        private String username;
        private String status;
        private LocalDateTime registeredAt;
    }

    /**
     * 邮箱验证响应
     */
    @lombok.Data
    @lombok.Builder
    public static class VerifyEmailResponse {
        private String email;
        private String status;
        private LocalDateTime verifiedAt;
    }

    /**
     * 邮箱可用性检查响应
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CheckEmailResponse {
        private boolean available;
    }

    /**
     * 错误响应
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
    }
}
