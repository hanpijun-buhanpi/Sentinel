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

public class ZookeeperConfigUtil {
    public static final String RULE_ROOT_PATH = "/sentinel/rule_config";
    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";

    public static String getFlowPath(String appName) {
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
        return stringBuilder.append(FLOW_DATA_ID_POSTFIX).toString();
    }
}