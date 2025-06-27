/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil.GROUP_ID;

/**
 * @author Laiyancheng
 * @since 2.0.0
 */
public class GatewayFlowRuleNacosProvider implements DynamicRuleProvider<List<GatewayFlowRuleEntity>> {

    @Autowired
    private ConfigService configService;
    @Autowired
    @Qualifier(DynamicRuleConfig.GATEWAY_FLOW_RULE_ENTITY_DECODER)
    private Converter<String, List<GatewayFlowRuleEntity>> converter;

    @Override
    public List<GatewayFlowRuleEntity> getRules(String appName) throws Exception {
        String rules = configService.getConfig(NacosConfigUtil.getGatewayFlowDataId(appName), GROUP_ID, 3000);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }
}
