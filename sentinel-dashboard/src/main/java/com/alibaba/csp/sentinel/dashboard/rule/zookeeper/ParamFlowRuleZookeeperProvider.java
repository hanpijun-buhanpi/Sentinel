/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Laiyancheng
 * @since 2.0.0
 */
public class ParamFlowRuleZookeeperProvider implements DynamicRuleProvider<List<ParamFlowRuleEntity>> {

    @Autowired
    private CuratorFramework zkClient;
    @Autowired
    @Qualifier(DynamicRuleConfig.PARAM_FLOW_RULE_ENTITY_DECODER)
    private Converter<String, List<ParamFlowRuleEntity>> converter;

    @Override
    public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {
        String zkPath = ZookeeperConfigUtil.getParamFLowPath(appName);
        Stat stat = zkClient.checkExists().forPath(zkPath);
        if(stat == null){
            return new ArrayList<>(0);
        }
        byte[] bytes = zkClient.getData().forPath(zkPath);
        if (null == bytes || bytes.length == 0) {
            return new ArrayList<>();
        }
        String s = new String(bytes, StandardCharsets.UTF_8);

        return converter.convert(s);
    }
}