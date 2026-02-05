package com.starbucks.menu.integration;

/**
 * 会员服务接口（外部依赖）
 *
 * 模拟与会员系统的集成
 * 在 BDD 测试中使用 @MockBean 进行 Mock
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
public interface MemberService {

    /**
     * 获取用户积分
     *
     * @param userId 用户ID
     * @return 积分余额
     */
    int getPoints(String userId);

    /**
     * 扣减积分
     *
     * @param userId 用户ID
     * @param points 扣减积分数
     * @return 是否扣减成功
     */
    boolean deductPoints(String userId, int points);

    /**
     * 增加积分
     *
     * @param userId 用户ID
     * @param points 增加积分数
     * @return 是否增加成功
     */
    boolean addPoints(String userId, int points);
}
