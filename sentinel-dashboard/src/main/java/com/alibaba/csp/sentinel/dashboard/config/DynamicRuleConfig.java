/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 动态规则配置
 *
 * @author lyc
 * @since 1.8-SNAPSHOT
 */
@EnableConfigurationProperties({DynamicRuleProperties.class})
@Configuration
public class DynamicRuleConfig {
    /* 配置前缀 */
    public static final String DYNAMIC_RULE_PREFIX = "rules.dynamic";
    /* Bean名称 */
    public static final String FLOW_DYNAMIC_RULE_PROVIDER = "flowDynamicRuleProvider";
    public static final String FLOW_DYNAMIC_RULE_PUBLISHER = "flowDynamicRulePublisher";

    /**
     * 默认 动态规则配置
     *
     * @author lyc
     * @since 1.8-SNAPSHOT
     */
    @ConditionalOnProperty(prefix = DYNAMIC_RULE_PREFIX, name = "type", havingValue = "default", matchIfMissing = true)
    @ConditionalOnMissingBean(DynamicRuleProvider.class)
    @Configuration
    public static class DefaultDynamicRuleConfig {
        @Bean(name = FLOW_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<FlowRuleEntity>> flowDynamicRuleProvider() {
            return new FlowRuleApiProvider();
        }

        @Bean(name = FLOW_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<FlowRuleEntity>> flowDynamicRulePublisher() {
            return new FlowRuleApiPublisher();
        }
    }
}
