/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Laiyancheng
 * @since 2.0.0
 */
public class SystemRuleApolloProvider implements DynamicRuleProvider<List<SystemRuleEntity>> {

    @Autowired
    private DynamicRuleProperties.Apollo properties;
    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    @Qualifier(DynamicRuleConfig.SYSTEM_RULE_ENTITY_DECODER)
    private Converter<String, List<SystemRuleEntity>> converter;

    @Override
    public List<SystemRuleEntity> getRules(String appName) throws Exception {
        String dataId = ApolloConfigUtil.getSystemDataId(appName);
        OpenItemDTO openItemDTO = apolloOpenApiClient.getItem(DynamicRuleProperties.Apollo.APP_ID,
                properties.getEnv(), properties.getCluster(), properties.getNamespace(), dataId);
        String rules = openItemDTO == null ? "" : openItemDTO.getValue();

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }
}
