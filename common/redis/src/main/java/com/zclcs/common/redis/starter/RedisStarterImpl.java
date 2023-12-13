package com.zclcs.common.redis.starter;

import com.zclcs.common.core.service.StarterService;
import com.zclcs.common.core.utils.StringsUtil;
import com.zclcs.common.redis.utils.CacheUtil;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static io.vertx.core.Future.await;

/**
 * @author zclcs
 */
@Slf4j
public class RedisStarterImpl implements StarterService {

    private final Vertx vertx;
    private final Context context;
    private final JsonObject config;
    private RedisAPI redis;

    public RedisStarterImpl(Vertx vertx, Context context, JsonObject config) {
        this.vertx = vertx;
        this.context = context;
        this.config = config;
    }

    @Override
    public void setUp() throws Exception {
        connectRedis(config);
        CacheUtil.init(context, config, redis);
    }

    private void connectRedis(JsonObject config) {
        RedisOptions options = new RedisOptions();
        String redisHost = config.getString("REDIS_HOST", "127.0.0.1");
        String redisPort = config.getString("REDIS_PORT", "6379");
        String redisDatabase = config.getString("REDIS_DATABASE", "0");
        String redisPassword = config.getString("REDIS_PASSWORD", "");
        String connectionUrl;
        if (StringsUtil.isBlank(redisPassword)) {
            connectionUrl = String.format("redis://%s:%s/%s", redisHost, redisPort, redisDatabase);
        } else {
            connectionUrl = String.format("redis://:%s@%s:%s/%s", redisPassword, redisHost, redisPort, redisDatabase);
        }
        options.setConnectionString(connectionUrl);
        options.setMaxPoolSize(100);
        options.setMaxPoolWaiting(1000);
        Redis client = Redis.createClient(vertx, options);
        await(client.connect().timeout(1, TimeUnit.SECONDS));
        this.redis = RedisAPI.api(client);
        log.info("Redis connected");
    }
}
