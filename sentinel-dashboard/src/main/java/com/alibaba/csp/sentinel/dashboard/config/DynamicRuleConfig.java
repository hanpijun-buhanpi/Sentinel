/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.*;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.*;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.*;
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
    public static final String DEGRADE_RULE_ENTITY_ENCODER = "degradeRuleEntityEncoder";
    public static final String DEGRADE_RULE_ENTITY_DECODER = "degradeRuleEntityDecoder";
    public static final String DEGRADE_DYNAMIC_RULE_PROVIDER = "degradeDynamicRuleProvider";
    public static final String DEGRADE_DYNAMIC_RULE_PUBLISHER = "degradeDynamicRulePublisher";
    public static final String SYSTEM_RULE_ENTITY_ENCODER = "systemRuleEntityEncoder";
    public static final String SYSTEM_RULE_ENTITY_DECODER = "systemRuleEntityDecoder";
    public static final String SYSTEM_DYNAMIC_RULE_PROVIDER = "systemDynamicRuleProvider";
    public static final String SYSTEM_DYNAMIC_RULE_PUBLISHER = "systemDynamicRulePublisher";

    @Bean(FLOW_RULE_ENTITY_ENCODER)
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean(FLOW_RULE_ENTITY_DECODER)
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    @Bean(DEGRADE_RULE_ENTITY_ENCODER)
    public Converter<List<DegradeRuleEntity>, String> degradeRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean(DEGRADE_RULE_ENTITY_DECODER)
    public Converter<String, List<DegradeRuleEntity>> degradeRuleEntityDecoder() {
        return s -> JSON.parseArray(s, DegradeRuleEntity.class);
    }

    @Bean(SYSTEM_RULE_ENTITY_ENCODER)
    public Converter<List<SystemRuleEntity>, String> systemRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean(SYSTEM_RULE_ENTITY_DECODER)
    public Converter<String, List<SystemRuleEntity>> systemRuleEntityDecoder() {
        return s -> JSON.parseArray(s, SystemRuleEntity.class);
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

        @Bean(name = DEGRADE_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<DegradeRuleEntity>> degradeDynamicRuleProvider() {
            return new DegradeRuleNacosProvider();
        }

        @Bean(name = DEGRADE_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<DegradeRuleEntity>> degradeDynamicRulePublisher() {
            return new DegradeRuleNacosPublisher();
        }

        @Bean(name = SYSTEM_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<SystemRuleEntity>> systemDynamicRuleProvider() {
            return new SystemRuleNacosProvider();
        }

        @Bean(name = SYSTEM_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<SystemRuleEntity>> systemDynamicRulePublisher() {
            return new SystemRuleNacosPublisher();
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

        @Bean(name = DEGRADE_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<DegradeRuleEntity>> degradeDynamicRuleProvider() {
            return new DegradeRuleApolloProvider();
        }

        @Bean(name = DEGRADE_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<DegradeRuleEntity>> degradeDynamicRulePublisher() {
            return new DegradeRuleApolloPublisher();
        }

        @Bean(name = SYSTEM_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<SystemRuleEntity>> systemDynamicRuleProvider() {
            return new SystemRuleApolloProvider();
        }

        @Bean(name = SYSTEM_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<SystemRuleEntity>> systemDynamicRulePublisher() {
            return new SystemRuleApolloPublisher();
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

        @Bean(name = DEGRADE_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<DegradeRuleEntity>> degradeDynamicRuleProvider() {
            return new DegradeRuleZookeeperProvider();
        }

        @Bean(name = DEGRADE_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<DegradeRuleEntity>> degradeDynamicRulePublisher() {
            return new DegradeRuleZookeeperPublisher();
        }

        @Bean(name = SYSTEM_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<SystemRuleEntity>> systemDynamicRuleProvider() {
            return new SystemRuleZookeeperProvider();
        }

        @Bean(name = SYSTEM_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<SystemRuleEntity>> systemDynamicRulePublisher() {
            return new SystemRuleZookeeperPublisher();
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

        @Bean(name = DEGRADE_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<DegradeRuleEntity>> degradeDynamicRuleProvider() {
            return new DegradeRuleApiProvider();
        }

        @Bean(name = DEGRADE_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<DegradeRuleEntity>> degradeDynamicRulePublisher() {
            return new DegradeRuleApiPublisher();
        }

        @Bean(name = SYSTEM_DYNAMIC_RULE_PROVIDER)
        public DynamicRuleProvider<List<SystemRuleEntity>> systemDynamicRuleProvider() {
            return new SystemRuleApiProvider();
        }

        @Bean(name = SYSTEM_DYNAMIC_RULE_PUBLISHER)
        public DynamicRulePublisher<List<SystemRuleEntity>> systemDynamicRulePublisher() {
            return new SystemRuleApiPublisher();
        }
    }
}
