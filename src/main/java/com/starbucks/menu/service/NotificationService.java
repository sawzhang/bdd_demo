package com.starbucks.menu.service;

import com.starbucks.menu.domain.PriceChangeOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通知服务
 *
 * 对应 BDD 场景: "价格变更审批通过后自动生效"
 * Then: 应发送价格变更通知给所有区域门店
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /**
     * 发送价格变更成功通知
     *
     * @param order 价格变更单
     */
    public void sendPriceChangeNotification(PriceChangeOrder order) {
        log.info("发送价格变更通知 - 单号: {}, 区域: {}, 门店数: {}",
            order.getOrderNo(),
            order.getTargetRegion(),
            order.getAffectedStoreCount());

        // 发送到Kafka消息队列
        // kafkaTemplate.send("price-change-notification", buildNotification(order));

        // 发送邮件通知
        // emailService.sendToRegionalManagers(order.getTargetRegion(), buildEmail(order));

        // 发送短信通知
        // smsService.sendToStoreManagers(order.getAffectedStoreIds(), buildSms(order));
    }

    /**
     * 发送价格变更失败通知
     *
     * @param order 价格变更单
     * @param failureReason 失败原因
     */
    public void sendPriceChangeFailureNotification(
            PriceChangeOrder order,
            String failureReason) {

        log.error("发送价格变更失败通知 - 单号: {}, 失败原因: {}",
            order.getOrderNo(), failureReason);

        // 通知运营人员和技术支持团队
        // alertService.sendToOpsAndTechSupport(order, failureReason);
    }
}
