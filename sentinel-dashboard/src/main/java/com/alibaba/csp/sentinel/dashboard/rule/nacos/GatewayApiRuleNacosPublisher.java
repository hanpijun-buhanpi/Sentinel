/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
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
public class GatewayApiRuleNacosPublisher implements DynamicRulePublisher<List<ApiDefinitionEntity>> {

    @Autowired
    private ConfigService configService;
    @Autowired
    @Qualifier(DynamicRuleConfig.GATEWAY_API_RULE_ENTITY_ENCODER)
    private Converter<List<ApiDefinitionEntity>, String> converter;

    @Override
    public void publish(String appName, List<ApiDefinitionEntity> rules) throws Exception {
        AssertUtil.notEmpty(appName, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        configService.publishConfig(NacosConfigUtil.getGatewayApiDataId(appName), GROUP_ID, converter.convert(rules));
    }
}
