/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Properties;

/**
 * 动态规则属性
 *
 * @author lyc
 * @since 1.8-SNAPSHOT
 */
@EnableConfigurationProperties({DynamicRuleProperties.Nacos.class, DynamicRuleProperties.Apollo.class,
        DynamicRuleProperties.Zookeeper.class})
@ConfigurationProperties(prefix = DynamicRuleConfig.DYNAMIC_RULE_PREFIX)
public class DynamicRuleProperties {
    /**
     * 动态规则类型
     */
    private Type type = Type.DEFAULT;
    public enum Type {
        /**
         * 默认
         */
        DEFAULT,
        /**
         * Nacos
         */
        NACOS,
        /**
         * Apollo
         */
        APOLLO,
        /**
         * Zookeeper
         */
        ZOOKEEPER,
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Nacos配置属性
     *
     * @author lyc
     * @since 1.8-SNAPSHOT
     */
    @ConfigurationProperties(prefix = DynamicRuleConfig.NACOS_DYNAMIC_RULE_PREFIX)
    public static class Nacos {
        /**
         * Macos 服务地址
         */
        private String serverAddr = "localhost:8848";
        /**
         * 用户名
         */
        private String username = "nacos";
        /**
         * 密码
         */
        private String password = "nacos";
        /**
         * 命名空间
         */
        private String namespace = "";

        public String getServerAddr() {
            return serverAddr;
        }

        public void setServerAddr(String serverAddr) {
            this.serverAddr = serverAddr;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public Properties getProperties() {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("username", username);
            properties.put("password", password);
            properties.put("namespace", namespace);
            return properties;
        }
    }

    /**
     * Apollo配置属性
     *
     * @author lyc
     * @since 1.8-SNAPSHOT
     */
    @ConfigurationProperties(prefix = DynamicRuleConfig.APOLLO_DYNAMIC_RULE_PREFIX)
    public static class Apollo {
        /**
         * 登记在Apollo的应用ID
         */
        public static final String APP_ID = "sentinel";

        /**
         * Portal地址
         */
        private String portalUrl = "http://localhost:8070";
        /**
         * 访问密钥
         */
        private String token = null;
        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 1000;
        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 5000;
        /**
         * 环境
         */
        private String env = "DEV";
        /**
         * 集群
         */
        private String cluster = "default";
        /**
         * 命名空间
         */
        private String namespace = "application";
        /**
         * 操作者
         */
        private String operator = "apollo";

        public String getPortalUrl() {
            return portalUrl;
        }

        public void setPortalUrl(String portalUrl) {
            this.portalUrl = portalUrl;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public String getEnv() {
            return env;
        }

        public void setEnv(String env) {
            this.env = env;
        }

        public String getCluster() {
            return cluster;
        }

        public void setCluster(String cluster) {
            this.cluster = cluster;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }
    }

    /**
     * Zookeeper配置属性
     *
     * @author lyc
     * @since 1.8-SNAPSHOT
     */
    @ConfigurationProperties(prefix = DynamicRuleConfig.ZOOKEEPER_DYNAMIC_RULE_PREFIX)
    public static class Zookeeper {
        /**
         * Zookeeper 地址
         */
        private String connectString = "localhost:2181";
        /**
         * 会话超时（毫秒）
         */
        private int sessionTimeout = 60000;
        /**
         * 连接超时（毫秒）
         */
        private int connectionTimeout = 15000;
        /**
         * 重试次数
         */
        private int retryTimes = 3;
        /**
         * 基础重试睡眠时间（毫秒）
         */
        private int baseSleepTime = 1000;
        /**
         * 最大重试睡眠时间（毫秒）
         */
        private int maxSleepTime = 5000;

        public String getConnectString() {
            return connectString;
        }

        public void setConnectString(String connectString) {
            this.connectString = connectString;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getRetryTimes() {
            return retryTimes;
        }

        public void setRetryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
        }

        public int getBaseSleepTime() {
            return baseSleepTime;
        }

        public void setBaseSleepTime(int baseSleepTime) {
            this.baseSleepTime = baseSleepTime;
        }

        public int getMaxSleepTime() {
            return maxSleepTime;
        }

        public void setMaxSleepTime(int maxSleepTime) {
            this.maxSleepTime = maxSleepTime;
        }
    }
}
