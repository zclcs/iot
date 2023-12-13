package com.zclcs.common.redis.service;

import com.zclcs.common.core.constant.RedisPrefix;
import io.vertx.core.json.JsonObject;

/**
 * @author zhouc
 */
public class RedisPrefixService {

    private final String nacosNamespace;

    public RedisPrefixService(JsonObject config) {
        this.nacosNamespace = config.getString("NACOS_NAMESPACE", "dev");
    }

    public String getDeviceProjectKey(String deviceType, String deviceNo) {
        return RedisPrefix.DEVICE_PROJECT.formatted(nacosNamespace, deviceType, deviceNo);
    }

    public String getProjectWorkerKey(Long projectId) {
        return RedisPrefix.PROJECT_WORKER.formatted(nacosNamespace, projectId);
    }

    public String getProjectKey() {
        return RedisPrefix.PROJECT.formatted(nacosNamespace);
    }
}
