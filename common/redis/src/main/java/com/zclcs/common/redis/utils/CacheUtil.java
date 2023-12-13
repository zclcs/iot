package com.zclcs.common.redis.utils;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zclcs.common.core.bean.DeviceProject;
import com.zclcs.common.core.bean.Project;
import com.zclcs.common.core.bean.ProjectWorker;
import com.zclcs.common.redis.service.RedisPrefixService;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static io.vertx.core.Future.await;

/**
 * @author zclcs
 */
@Slf4j
public class CacheUtil {

    private static RedisPrefixService REDIS_PREFIX_SERVICE;
    private static AsyncLoadingCache<String, DeviceProject> DEVICE_PROJECT_CACHE;
    private static AsyncLoadingCache<String, Map<Long, ProjectWorker>> PROJECT_WORKER_CACHE;
    private static AsyncLoadingCache<List<String>, Project> PROJECT_CACHE;

    public static void init(Context context, JsonObject config, RedisAPI redis) {
        REDIS_PREFIX_SERVICE = new RedisPrefixService(config);
        DEVICE_PROJECT_CACHE = setUpCaffeineCache(context, (key) ->
                redis.get(key).map(response -> response == null ? null : translateEscapesAndToBean(response, DeviceProject.class)));
        PROJECT_WORKER_CACHE = setUpCaffeineCache(context, (key) ->
                redis.hgetall(key).map(response -> response == null ? null : translateEscapesAndToMap(response, Long.class, ProjectWorker.class)));
        PROJECT_CACHE = setUpCaffeineCache(context, (key) ->
                redis.hget(key.getFirst(), key.get(1))
                        .map(response -> response == null ? null : translateEscapesAndToBean(response, Project.class)));
    }

    private static <T> T translateEscapesAndToBean(@NonNull Response response, @NonNull Class<T> clazz) {
        String jsonStr = response.toString();
        if (jsonStr.startsWith("\"")) {
            jsonStr = jsonStr.substring(1, jsonStr.length() - 1).translateEscapes();
        }
        return Json.decodeValue(jsonStr, clazz);
    }

    private static <K, V> Map<K, V> translateEscapesAndToMap(@NonNull Response response, @NonNull Class<K> kClass, @NonNull Class<V> vClass) {
        Set<String> keys = response.getKeys();
        Map<K, V> map = new HashMap<>(keys.size());
        keys.forEach(s -> map.put(Json.decodeValue(s, kClass), translateEscapesAndToBean(response.get(s), vClass)));
        return map;
    }

    private static <T, R> AsyncLoadingCache<T, R> setUpCaffeineCache(Context context, Function<T, Future<R>> future) {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .recordStats()
                .executor(cmd -> context.runOnContext(v -> cmd.run()))
                .buildAsync((key, exec) -> CompletableFuture.supplyAsync(() ->
                        future.apply(key).toCompletionStage(), exec).thenComposeAsync(Function.identity(), exec));
    }

    /**
     * 获取设备项目信息
     *
     * @param deviceType 设备类型
     * @param deviceNo   设备编号
     * @return 设备项目信息
     */
    public static DeviceProject getDeviceProject(String deviceType, String deviceNo) {
        return await(Future.fromCompletionStage(DEVICE_PROJECT_CACHE.get(REDIS_PREFIX_SERVICE.getDeviceProjectKey(deviceType, deviceNo))));
    }

    /**
     * 获取项目人员信息
     *
     * @param projectId 项目ID
     * @return 项目人员信息
     */
    public static Map<Long, ProjectWorker> getProjectWorker(Long projectId) {
        return await(Future.fromCompletionStage(PROJECT_WORKER_CACHE.get(REDIS_PREFIX_SERVICE.getProjectWorkerKey(projectId))));
    }

    /**
     * 获取项目信息
     *
     * @param projectId 项目ID
     * @return 项目信息
     */
    public static Project getProject(Long projectId) {
        return await(Future.fromCompletionStage(PROJECT_CACHE.get(Arrays.asList(REDIS_PREFIX_SERVICE.getProjectKey(), projectId.toString()))));
    }

}
