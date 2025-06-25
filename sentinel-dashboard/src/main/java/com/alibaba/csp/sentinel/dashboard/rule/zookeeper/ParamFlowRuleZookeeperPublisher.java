/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Laiyancheng
 * @since 2.0.0
 */
public class ParamFlowRuleZookeeperPublisher implements DynamicRulePublisher<List<ParamFlowRuleEntity>> {
    @Autowired
    private CuratorFramework zkClient;
    @Autowired
    @Qualifier(DynamicRuleConfig.PARAM_FLOW_RULE_ENTITY_ENCODER)
    private Converter<List<ParamFlowRuleEntity>, String> converter;

    @Override
    public void publish(String appName, List<ParamFlowRuleEntity> rules) throws Exception {
        AssertUtil.notEmpty(appName, "app name cannot be empty");

        String path = ZookeeperConfigUtil.getParamFLowPath(appName);
        Stat stat = zkClient.checkExists().forPath(path);
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
        }
        byte[] data = CollectionUtils.isEmpty(rules) ? "[]".getBytes(StandardCharsets.UTF_8) : converter.convert(rules).getBytes(StandardCharsets.UTF_8);
        zkClient.setData().forPath(path, data);
    }
}