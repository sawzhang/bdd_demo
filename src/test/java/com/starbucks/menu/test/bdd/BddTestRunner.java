package com.starbucks.menu.test.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Cucumber BDD 测试运行器
 *
 * 执行方式:
 * 1. Maven: mvn test -Dtest=BddTestRunner
 * 2. IDE: 直接运行这个类
 * 3. CI/CD: 集成到构建流程
 *
 * @author AI-Generated via menu-pricing skill
 * @version 1.0.0
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:behaviors/menu/price_update.feature",  // BDD 场景文件位置
    glue = "com.starbucks.menu.test.bdd",                        // Step Definitions 包路径
    plugin = {
        "pretty",                                                 // 控制台输出
        "html:target/cucumber-reports/cucumber.html",            // HTML 报告
        "json:target/cucumber-reports/cucumber.json",            // JSON 报告
        "junit:target/cucumber-reports/cucumber.xml"             // JUnit XML 报告
    },
    monochrome = true,                                           // 控制台输出格式化
    tags = "not @skip"                                           // 排除标记为 @skip 的场景
)
public class BddTestRunner {
    // Cucumber 会自动发现并执行测试
}
