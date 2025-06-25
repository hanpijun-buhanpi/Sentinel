/*
 * Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * @author Laiyancheng
 * @since 2.0.0
 */
public class SystemRuleApiPublisher implements DynamicRulePublisher<List<SystemRuleEntity>> {

    @Autowired
    private SentinelApiClient sentinelApiClient;
    @Autowired
    private AppManagement appManagement;

    @Override
    public void publish(String appName, List<SystemRuleEntity> rules) throws Exception {
        if (StringUtil.isBlank(appName)) {
            return;
        }
        if (rules == null) {
            return;
        }
        Set<MachineInfo> set = appManagement.getDetailApp(appName).getMachines();

        for (MachineInfo machine : set) {
            if (!machine.isHealthy()) {
                continue;
            }
            // TODO: parse the results
            sentinelApiClient.setSystemRuleOfMachine(appName, machine.getIp(), machine.getPort(), rules);
        }
    }
}
