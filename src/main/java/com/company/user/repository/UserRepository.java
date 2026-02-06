package com.company.user.repository;

import com.company.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 *
 * 对应 BDD 场景: behaviors/user/user_registration.feature
 * - 场景: 防止重复注册（existsByEmail 查重）
 * - 场景: 邮箱验证流程（findByVerificationToken 查找待验证用户）
 *
 * @author AI-Generated via user-registration skill
 * @version 1.0.0
 * @since 2026-02-06
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据邮箱查询用户
     *
     * 对应 BDD 场景: "防止重复注册"
     *
     * @param email 用户邮箱
     * @return 用户（可选）
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查邮箱是否已注册
     *
     * 对应 BDD 场景: "防止重复注册"
     * - Given: 系统中已存在用户
     * - Then: 注册应该失败，显示"该邮箱已被注册"
     *
     * @param email 用户邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据验证令牌查询用户
     *
     * 对应 BDD 场景: "邮箱验证流程"
     * - When: 用户点击验证邮件中的链接
     * - Then: 邮箱验证应该成功
     *
     * @param token 验证令牌
     * @return 用户（可选）
     */
    Optional<User> findByVerificationToken(String token);
}
