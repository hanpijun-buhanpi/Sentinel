/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 动态规则属性
 *
 * @author lyc
 * @since 1.8-SNAPSHOT
 */
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
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
