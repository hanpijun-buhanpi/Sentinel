/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.FlowRuleNacosProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.FlowRuleNacosPublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
    public static final String NACOS_DYNAMIC_RULE_PREFIX = DYNAMIC_RULE_PREFIX + ".nacos";
    /* Bean名称 */
    public static final String FLOW_RULE_ENTITY_ENCODER = "flowRuleEntityEncoder";
    public static final String FLOW_RULE_ENTITY_DECODER = "flowRuleEntityDecoder";
    public static final String FLOW_DYNAMIC_RULE_PROVIDER = "flowDynamicRuleProvider";
    public static final String FLOW_DYNAMIC_RULE_PUBLISHER = "flowDynamicRulePublisher";

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    /**
     * Nacos 动态规则配置
     *
     * @author lyc
     * @since 1.8-SNAPSHOT
     */
    @ConditionalOnProperty(prefix = DYNAMIC_RULE_PREFIX, name = "type", havingValue = "nacos")
    @ConditionalOnClass(ConfigFactory.class)
    @ConditionalOnMissingBean(DynamicRuleProvider.class)
    @Configuration
    public static class NacosDynamicRuleConfig {
        @Autowired
        private DynamicRuleProperties.Nacos properties;

        @Bean
        public ConfigService nacosConfigService() throws Exception {
            return ConfigFactory.createConfigService(properties.getProperties());
        }

        @Bean(name = FLOW_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<FlowRuleEntity>> flowDynamicRuleProvider() {
            return new FlowRuleNacosProvider();
        }

        @Bean(name = FLOW_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<FlowRuleEntity>> flowDynamicRulePublisher() {
            return new FlowRuleNacosPublisher();
        }
    }

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
