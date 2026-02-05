package com.company.menu.integration;

import com.company.menu.integration.dto.SmsResponse;

/**
 * 短信服务接口（外部依赖）
 *
 * 模拟与第三方短信平台的集成
 * 在 BDD 测试中使用 @MockBean 进行 Mock
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
public interface SmsService {

    /**
     * 发送短信
     *
     * @param phoneNumber 手机号
     * @param content 短信内容
     * @return 发送结果
     */
    SmsResponse sendSms(String phoneNumber, String content);

    /**
     * 发送验证码
     *
     * @param phoneNumber 手机号
     * @return 发送结果
     */
    SmsResponse sendVerificationCode(String phoneNumber);
}
