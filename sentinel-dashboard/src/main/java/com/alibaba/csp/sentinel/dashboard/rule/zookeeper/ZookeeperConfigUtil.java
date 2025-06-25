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
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import org.apache.commons.lang.StringUtils;

/**
 * @author kexianjun
 * @author Laiyancheng
 * @since 1.6.0
 */
public class ZookeeperConfigUtil {
    public static final String RULE_ROOT_PATH = "/sentinel/rule_config";
    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";
    public static final String DEGRADE_DATA_ID_POSTFIX = "-degrade-rules";
    public static final String SYSTEM_DATA_ID_POSTFIX = "-system-rules";
    public static final String AUTHORITY_DATA_ID_POSTFIX = "-authority-rules";

    public static String getFlowPath(String appName) {
        return getPath(appName, FLOW_DATA_ID_POSTFIX);
    }

    public static String getDegradePath(String appName) {
        return getPath(appName, DEGRADE_DATA_ID_POSTFIX);
    }

    public static String getSystemPath(String appName) {
        return getPath(appName, SYSTEM_DATA_ID_POSTFIX);
    }

    public static String getAuthorityPath(String appName) {
        return getPath(appName, AUTHORITY_DATA_ID_POSTFIX);
    }

    private static String getPath(String appName, String postfix) {
        StringBuilder stringBuilder = new StringBuilder(RULE_ROOT_PATH);

        if (StringUtils.isBlank(appName)) {
            return stringBuilder.toString();
        }
        if (appName.startsWith("/")) {
            stringBuilder.append(appName);
        } else {
            stringBuilder.append("/")
                    .append(appName);
        }
        return stringBuilder.append(postfix).toString();
    }
}