package com.starbucks.menu.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信发送响应
 *
 * @author AI-Generated
 * @version 1.0.0
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse {

    /**
     * 响应码
     * 0 表示成功
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 短信ID
     */
    private String messageId;

    /**
     * 发送时间（时间戳）
     */
    private Long timestamp;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return code == 0;
    }
}
