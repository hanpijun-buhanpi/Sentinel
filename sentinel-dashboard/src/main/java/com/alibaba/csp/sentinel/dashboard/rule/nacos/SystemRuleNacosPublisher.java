/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil.GROUP_ID;

/**
 * @author Laiyancheng
 * @since 2.0.0
 */
public class SystemRuleNacosPublisher implements DynamicRulePublisher<List<SystemRuleEntity>> {

    @Autowired
    private ConfigService configService;
    @Autowired
    @Qualifier(DynamicRuleConfig.SYSTEM_RULE_ENTITY_ENCODER)
    private Converter<List<SystemRuleEntity>, String> converter;

    @Override
    public void publish(String appName, List<SystemRuleEntity> rules) throws Exception {
        AssertUtil.notEmpty(appName, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        configService.publishConfig(NacosConfigUtil.getSystemDataId(appName), GROUP_ID, converter.convert(rules));
    }
}
