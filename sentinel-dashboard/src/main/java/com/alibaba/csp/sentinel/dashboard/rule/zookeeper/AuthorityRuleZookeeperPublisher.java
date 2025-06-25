/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
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
public class AuthorityRuleZookeeperPublisher implements DynamicRulePublisher<List<AuthorityRuleEntity>> {
    @Autowired
    private CuratorFramework zkClient;
    @Autowired
    @Qualifier(DynamicRuleConfig.AUTHORITY_RULE_ENTITY_ENCODER)
    private Converter<List<AuthorityRuleEntity>, String> converter;

    @Override
    public void publish(String appName, List<AuthorityRuleEntity> rules) throws Exception {
        AssertUtil.notEmpty(appName, "app name cannot be empty");

        String path = ZookeeperConfigUtil.getAuthorityPath(appName);
        Stat stat = zkClient.checkExists().forPath(path);
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
        }
        byte[] data = CollectionUtils.isEmpty(rules) ? "[]".getBytes(StandardCharsets.UTF_8) : converter.convert(rules).getBytes(StandardCharsets.UTF_8);
        zkClient.setData().forPath(path, data);
    }
}