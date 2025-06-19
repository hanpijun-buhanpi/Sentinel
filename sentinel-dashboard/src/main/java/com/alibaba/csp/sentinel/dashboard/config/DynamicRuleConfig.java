/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.FlowRuleApolloProvider;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.FlowRuleApolloPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.FlowRuleNacosProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.FlowRuleNacosPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.FlowRuleZookeeperProvider;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.FlowRuleZookeeperPublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
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
    public static final String APOLLO_DYNAMIC_RULE_PREFIX = DYNAMIC_RULE_PREFIX + ".apollo";
    public static final String ZOOKEEPER_DYNAMIC_RULE_PREFIX = DYNAMIC_RULE_PREFIX + ".zookeeper";
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
     * Apollo 动态规则配置
     *
     * @author lyc
     * @since 1.8-SNAPSHOT
     */
    @ConditionalOnProperty(prefix = DYNAMIC_RULE_PREFIX, name = "type", havingValue = "apollo")
    @ConditionalOnClass(ApolloOpenApiClient.class)
    @ConditionalOnMissingBean(DynamicRuleProvider.class)
    @Configuration
    public static class ApolloDynamicRuleConfig {
        @Autowired
        private DynamicRuleProperties.Apollo properties;

        @Bean
        public ApolloOpenApiClient apolloOpenApiClient() throws Exception {
            return ApolloOpenApiClient.newBuilder()
                    .withPortalUrl(properties.getPortalUrl())
                    .withToken(properties.getToken())
                    .withConnectTimeout(properties.getConnectTimeout())
                    .withReadTimeout(properties.getReadTimeout())
                    .build();
        }

        @Bean(name = FLOW_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<FlowRuleEntity>> flowDynamicRuleProvider() {
            return new FlowRuleApolloProvider();
        }

        @Bean(name = FLOW_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<FlowRuleEntity>> flowDynamicRulePublisher() {
            return new FlowRuleApolloPublisher();
        }
    }

    /**
     * Zookeeper 动态规则配置
     *
     * @author lyc
     * @since 1.8-SNAPSHOT
     */
    @ConditionalOnProperty(prefix = DYNAMIC_RULE_PREFIX, name = "type", havingValue = "zookeeper")
    @ConditionalOnClass(CuratorFramework.class)
    @ConditionalOnMissingBean(DynamicRuleProvider.class)
    @Configuration
    public static class ZookeeperDynamicRuleConfig {
        @Autowired
        private DynamicRuleProperties.Zookeeper properties;
        private CuratorFramework zkClient;

        @Bean
        public CuratorFramework curatorFramework() throws Exception {
            zkClient = CuratorFrameworkFactory.newClient(properties.getConnectString(), properties.getSessionTimeout(), properties.getConnectionTimeout(),
                    new ExponentialBackoffRetry(properties.getBaseSleepTime(), properties.getMaxSleepTime(), properties.getRetryTimes()));
            zkClient.start();
            return zkClient;
        }

        @PreDestroy
        public void destroy() {
            if (zkClient != null) {
                zkClient.close();
            }
        }

        @Bean(name = FLOW_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<FlowRuleEntity>> flowDynamicRuleProvider() {
            return new FlowRuleZookeeperProvider();
        }

        @Bean(name = FLOW_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<FlowRuleEntity>> flowDynamicRulePublisher() {
            return new FlowRuleZookeeperPublisher();
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
