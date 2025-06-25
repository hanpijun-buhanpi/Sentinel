/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.config.DynamicRuleConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
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
public class SystemRuleZookeeperProvider implements DynamicRuleProvider<List<SystemRuleEntity>> {

    @Autowired
    private CuratorFramework zkClient;
    @Autowired
    @Qualifier(DynamicRuleConfig.SYSTEM_RULE_ENTITY_DECODER)
    private Converter<String, List<SystemRuleEntity>> converter;

    @Override
    public List<SystemRuleEntity> getRules(String appName) throws Exception {
        String path = ZookeeperConfigUtil.getDegradePath(appName);
        Stat stat = zkClient.checkExists().forPath(path);
        if(stat == null){
            return new ArrayList<>(0);
        }
        byte[] bytes = zkClient.getData().forPath(path);
        if (null == bytes || bytes.length == 0) {
            return new ArrayList<>();
        }
        String s = new String(bytes, StandardCharsets.UTF_8);

        return converter.convert(s);
    }
}