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
@EnableConfigurationProperties({DynamicRuleProperties.Nacos.class})
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
}
