/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
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
public class ParamFlowRuleApolloProvider implements DynamicRuleProvider<List<ParamFlowRuleEntity>> {

    @Autowired
    private DynamicRuleProperties.Apollo properties;
    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    @Qualifier(DynamicRuleConfig.PARAM_FLOW_RULE_ENTITY_DECODER)
    private Converter<String, List<ParamFlowRuleEntity>> converter;

    @Override
    public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {
        String flowDataId = ApolloConfigUtil.getParamFLowDataId(appName);
        OpenItemDTO openItemDTO = apolloOpenApiClient.getItem(DynamicRuleProperties.Apollo.APP_ID,
                properties.getEnv(), properties.getCluster(), properties.getNamespace(), flowDataId);
        String rules = openItemDTO == null ? "" : openItemDTO.getValue();

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }
}
