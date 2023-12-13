package com.zclcs.common.rabbit.starter;

import com.zclcs.common.core.service.StarterService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.extern.slf4j.Slf4j;

import static io.vertx.core.Future.await;

/**
 * @author zclcs
 */
@Slf4j
public class RabbitStarterImpl implements StarterService {

    private final Vertx vertx;
    private final JsonObject config;
    private RabbitMQClient rabbit;

    public RabbitStarterImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
    }

    @Override
    public void setUp() throws Exception {
        connectRabbitMQ(config);
        createExchange(config);
    }

    private void connectRabbitMQ(JsonObject config) {
        RabbitMQOptions options = new RabbitMQOptions();
        options.setUser(config.getString("RABBITMQ_USER", "guest"));
        options.setPassword(config.getString("RABBITMQ_PASSWORD", "guest"));
        options.setHost(config.getString("RABBITMQ_HOST", "127.0.0.1"));
        options.setPort(config.getInteger("RABBITMQ_PORT", 5672));
        options.setVirtualHost(config.getString("RABBITMQ_VIRTUAL_HOST", "/"));
        options.setConnectionTimeout(6000);
        options.setRequestedHeartbeat(60);
        options.setHandshakeTimeout(6000);
        options.setRequestedChannelMax(5);
        options.setNetworkRecoveryInterval(500);
        options.setAutomaticRecoveryEnabled(true);
        rabbit = RabbitMQClient.create(vertx, options);
        createExchange(config);
        await(rabbit.start());
        log.info("RabbitMQ connected");
    }

    private void createExchange(JsonObject config) {
        String nacosNamespace = config.getString("NACOS_NAMESPACE", "dev");
        String exchangeName = nacosNamespace + ".test.exchange";
        String queueName = nacosNamespace + ".test.queue";
        String routingKey = nacosNamespace + ".test.routingKey";
        rabbit.addConnectionEstablishedCallback(promise -> {
            rabbit.exchangeDeclare(exchangeName, "topic", true, false)
                    .compose(v -> rabbit.queueDeclare(queueName, true, false, false))
                    .compose(v -> rabbit.queueBind(queueName, exchangeName, routingKey))
                    .onComplete(promise);
            ;
        });
    }
}
