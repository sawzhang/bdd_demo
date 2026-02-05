package com.company.menu.test.bdd.hooks;

import com.company.menu.test.bdd.context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Cucumber 钩子
 *
 * 在场景执行前后执行清理和初始化操作
 * 确保每个场景独立运行，状态不互相影响
 *
 * @author AI-Generated
 * @version 1.0.0
 */
@Slf4j
public class CucumberHooks {

    @Autowired
    private ScenarioContext scenarioContext;

    /**
     * 场景执行前的钩子
     * 清理上一个场景的数据，确保场景独立性
     */
    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🎬 开始执行场景: {}", scenario.getName());
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 清理场景上下文
        if (scenarioContext != null) {
            scenarioContext.clear();
        }

        log.debug("✓ 场景上下文已清理");
    }

    /**
     * 场景执行后的钩子
     * 记录场景执行结果，清理资源
     */
    @After
    public void afterScenario(Scenario scenario) {
        // 记录场景执行结果
        if (scenario.isFailed()) {
            log.error("❌ 场景执行失败: {}", scenario.getName());

            // 记录失败时的状态
            if (scenarioContext != null && scenarioContext.getCurrentOrder() != null) {
                log.error("失败时的订单状态: {}",
                    scenarioContext.getCurrentOrder().getStatus());
                log.error("状态历史: {}",
                    scenarioContext.getStatusHistoryString());
            }
        } else {
            log.info("✅ 场景执行成功: {}", scenario.getName());

            // 记录成功时的状态历史
            if (scenarioContext != null) {
                String statusHistory = scenarioContext.getStatusHistoryString();
                if (!statusHistory.isEmpty()) {
                    log.info("状态流转: {}", statusHistory);
                }
            }
        }

        // 清理场景上下文
        if (scenarioContext != null) {
            scenarioContext.clear();
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🏁 场景执行完成");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("");
    }

    /**
     * 针对特定标签的钩子示例
     * 只在带有 @cleanup 标签的场景后执行
     */
    @After("@cleanup")
    public void cleanupAfterTaggedScenario() {
        log.debug("执行带 @cleanup 标签的场景清理");
        // 可以在这里添加特定的清理逻辑
        // 例如：清理数据库、重置外部系统状态等
    }

    /**
     * 针对特定标签的前置钩子示例
     * 只在带有 @setup 标签的场景前执行
     */
    @Before("@setup")
    public void setupBeforeTaggedScenario() {
        log.debug("执行带 @setup 标签的场景初始化");
        // 可以在这里添加特定的初始化逻辑
    }
}
