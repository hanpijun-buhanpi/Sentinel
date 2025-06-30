/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Additional Copyright 2025 Laiyancheng 1548749669@qq.com All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.controller.v2;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.command.vo.NodeVo;

import com.alibaba.csp.sentinel.dashboard.domain.ResourceTreeNode;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.ResourceVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource controller (v2).
 *
 * @author Carpenter Lee
 * @author Laiyancheng
 * @since 2.0.0
 */
@RestController
@RequestMapping(value = "/v2/resource")
public class ResourceControllerV2 {

    private static Logger logger = LoggerFactory.getLogger(ResourceControllerV2.class);

    @Autowired
    private SentinelApiClient httpFetcher;
    @Autowired
    private AppManagement appManagement;

    /**
     * Fetch real time statistics info of the machine.
     *
     * @param app       app to fetch
     * @param type      one of [root, default, cluster], 'root' means fetching from tree root node, 'default' means
     *                  fetching from tree default node, 'cluster' means fetching from cluster node.
     * @param searchKey key to search
     * @return node statistics info.
     */
    @GetMapping("/machineResource.json")
    public Result<List<ResourceVo>> fetchResourceChainListOfMachine(String app, String type, String searchKey) {
        AppInfo appInfo = appManagement.getDetailApp(app);
        Set<MachineInfo> machines = appInfo.getMachines();
        machines = machines.stream().filter(MachineInfo::isHealthy).collect(Collectors.toSet());
        if (machines.isEmpty()) {
            return Result.ofSuccess(null);
        }

        MachineInfo machineInfo = machines.iterator().next();
        String ip = machineInfo.getIp();
        Integer port = machineInfo.getPort();
        final String ROOT = "root";
        final String DEFAULT = "default";
        if (StringUtil.isEmpty(type)) {
            type = ROOT;
        }
        if (ROOT.equalsIgnoreCase(type) || DEFAULT.equalsIgnoreCase(type)) {
            List<NodeVo> nodeVos = httpFetcher.fetchResourceOfMachine(ip, port, type);
            if (nodeVos == null) {
                return Result.ofSuccess(null);
            }
            process(nodeVos);
            ResourceTreeNode treeNode = ResourceTreeNode.fromNodeVoList(nodeVos);
            treeNode.searchIgnoreCase(searchKey);
            return Result.ofSuccess(ResourceVo.fromResourceTreeNode(treeNode));
        } else {
            // Normal (cluster node).
            List<NodeVo> nodeVos = httpFetcher.fetchClusterNodeOfMachine(ip, port, true);
            if (nodeVos == null) {
                return Result.ofSuccess(null);
            }
            process(nodeVos);
            if (StringUtil.isNotEmpty(searchKey)) {
                nodeVos = nodeVos.stream().filter(node -> node.getResource()
                    .toLowerCase().contains(searchKey.toLowerCase()))
                    .collect(Collectors.toList());
            }
            return Result.ofSuccess(ResourceVo.fromNodeVoList(nodeVos));
        }
    }

    /**
     * Fetch real time statistics info of the app.
     *
     * @param app       app to fetch
     * @param type      one of [root, default, cluster], 'root' means fetching from tree root node, 'default' means
     *                  fetching from tree default node, 'cluster' means fetching from cluster node.
     * @param searchKey key to search
     * @return node statistics info.
     */
    @GetMapping("/appResource.json")
    public Result<List<ResourceVo>> fetchResourceChainListOfApp(String app, String type, String searchKey) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "invalid param, give app");
        }
        final String ROOT = "root";
        final String DEFAULT = "default";
        if (StringUtil.isEmpty(type)) {
            type = ROOT;
        }
        if (ROOT.equalsIgnoreCase(type) || DEFAULT.equalsIgnoreCase(type)) {
            List<NodeVo> nodeVos = fetchNodeOfApp(app, type, false);
            if (nodeVos == null) {
                return Result.ofSuccess(null);
            }
            ResourceTreeNode treeNode = ResourceTreeNode.fromNodeVoList(nodeVos);
            treeNode.searchIgnoreCase(searchKey);
            return Result.ofSuccess(ResourceVo.fromResourceTreeNode(treeNode));
        } else {
            // Normal (cluster node).
            List<NodeVo> nodeVos = fetchNodeOfApp(app, type, true);
            if (nodeVos == null) {
                return Result.ofSuccess(null);
            }
            if (StringUtil.isNotEmpty(searchKey)) {
                nodeVos = nodeVos.stream().filter(node -> node.getResource()
                    .toLowerCase().contains(searchKey.toLowerCase()))
                    .collect(Collectors.toList());
            }
            return Result.ofSuccess(ResourceVo.fromNodeVoList(nodeVos));
        }
    }

    /**
     * 处理数据
     *
     * @param nodeVos NodeVos
     */
    private void process(List<NodeVo> nodeVos) {
        // 统计所有节点比较耗时且麻烦，这里将数值统一改成-1，以体现区别
        nodeVos.forEach(n -> {
            if (n != null) {
                n.setThreadNum(-1); // 并发数
                n.setPassQps(-1L); // 通过QPS
                n.setBlockQps(-1L); // 拒绝QPS
                n.setTotalQps(-1L); // 控制面板上没显示
                n.setAverageRt(-1L); // 平均RT
                n.setSuccessQps(-1L); // 控制面板上没显示
                n.setExceptionQps(-1L); // 控制面板上没显示
                n.setOneMinuteException(-1L); // 控制面板上没显示
                n.setOneMinutePass(-1L); // 分钟通过
                n.setOneMinuteBlock(-1L); // 分钟拒绝
                n.setOneMinuteTotal(-1L); // 控制面板上没显示
            }
        });
    }

    /**
     * 根据APP获取节点列表
     *
     * @param app       App name
     * @param type      返回类型
     * @param isCluster 是否为集群节点
     * @return 节点列表
     */
    private List<NodeVo> fetchNodeOfApp(String app, String type, boolean isCluster) {
        AppInfo appInfo = appManagement.getDetailApp(app);
        if (appInfo == null) {
            return null;
        }
        Set<MachineInfo> machines = appInfo.getMachines();
        Map<String, NodeVo> map = new LinkedHashMap<>(machines.size());
        machines.stream()
                .filter(MachineInfo::isHealthy)
                .map(e -> {
                    if (!isCluster) {
                        return httpFetcher.fetchResourceOfMachine(e.getIp(), e.getPort(), type);
                    } else {
                        return httpFetcher.fetchClusterNodeOfMachine(e.getIp(), e.getPort(), true);
                    }
                })
                .filter(Objects::nonNull)
                .forEach(e -> {
                    for (NodeVo v : e) {
                        if (map.containsKey(v.getResource())) {
                            NodeVo node = map.get(v.getResource());
                            node.setThreadNum(node.getThreadNum() + v.getThreadNum()); // 并发数
                            node.setPassQps(node.getPassQps() + v.getPassQps()); // 通过QPS
                            node.setBlockQps(node.getBlockQps() + v.getBlockQps()); // 拒绝QPS
                            node.setTotalQps(node.getTotalQps() + v.getTotalQps()); // 控制面板上没显示
                            node.setAverageRt(node.getAverageRt() + v.getAverageRt()); // 平均RT
                            node.setSuccessQps(node.getSuccessQps() + v.getSuccessQps()); // 控制面板上没显示
                            node.setExceptionQps(node.getExceptionQps() + v.getExceptionQps()); // 控制面板上没显示
                            node.setOneMinuteException(node.getOneMinuteException() + v.getOneMinuteException()); // 控制面板上没显示
                            node.setOneMinutePass(node.getOneMinutePass() + v.getOneMinutePass()); // 分钟通过
                            node.setOneMinuteBlock(node.getOneMinuteBlock() + v.getOneMinuteBlock()); // 分钟拒绝
                            node.setOneMinuteTotal(node.getOneMinuteTotal() + v.getOneMinuteTotal()); // 控制面板上没显示
                        } else {
                            map.put(v.getResource(), v);
                        }
                    }
                });
        if (map.isEmpty()) {
            return null;
        }
        return new ArrayList<>(map.values());
    }
}
