package com.zclcs;

import com.zclcs.common.config.starter.JacksonStarterImpl;
import com.zclcs.common.core.bean.ReceivedData;
import com.zclcs.common.core.constant.Commands;
import com.zclcs.common.core.exception.ParserException;
import com.zclcs.common.core.service.StarterService;
import com.zclcs.common.rabbit.starter.RabbitStarterImpl;
import com.zclcs.common.redis.starter.RedisStarterImpl;
import com.zclcs.iot.lib.codec.attendance.stream.BatchStream;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

import static com.zclcs.iot.lib.codec.attendance.function.DecodeContentFunction.commandMappings;
import static io.vertx.core.Future.await;

/**
 * @author zclcs
 */
@Slf4j
public class IotGatewayVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        var vertx = Vertx.vertx();
        try {
            vertx.deployVerticle(IotGatewayVerticle.class, new DeploymentOptions()
                            .setThreadingModel(ThreadingModel.VIRTUAL_THREAD))
                    .toCompletionStage()
                    .toCompletableFuture()
                    .get();
        } catch (Throwable e) {
            log.error("start up error", e);
        }
    }

    @Override
    public void start() throws Exception {
        ConfigStoreOptions store = new ConfigStoreOptions()
                .setType("env");
        ConfigRetriever retriever = ConfigRetriever.create(vertx,
                new ConfigRetrieverOptions().addStore(store));
        JsonObject config = await(retriever.getConfig());
        StarterService configSetUp = new JacksonStarterImpl();
        configSetUp.setUp();
        StarterService redisSetUp = new RedisStarterImpl(vertx, context, config);
        redisSetUp.setUp();
        StarterService rabbitStarter = new RabbitStarterImpl(vertx, config);
        rabbitStarter.setUp();
        startUpHttpServer(config);
        startUpNetServer(config);
    }

    private void startUpHttpServer(JsonObject config) {
        var iotGatewayHttpPort = config.getInteger("IOT_GATEWAY_HTTP_PORT", 8888);
        var server = vertx.createHttpServer();
        server.requestHandler(request -> {
            request.response().end("Hello World");
        });
        await(server.listen(iotGatewayHttpPort));
        log.info("HTTP server started on port {}", iotGatewayHttpPort);
    }


    private void startUpNetServer(JsonObject config) {
        Integer iotGatewayNetPort = config.getInteger("IOT_GATEWAY_NET_PORT", 5555);
        NetServer netServer = vertx.createNetServer()
                .connectHandler(socket -> {
                    BatchStream batchStream = getStream(socket);
                    // Pause reading data
                    batchStream.pause();
                    // Register read stream handler
                    batchStream.handler(data -> {
                                log.info("Received data: {}", data.toRawString());
                                // Write back batch object to the client
                                batchStream.write(data);

                                // Check if write queue is full
                                if (batchStream.writeQueueFull()) {

                                    // Pause reading data
                                    batchStream.pause();

                                    // Called once write queue is ready to accept more data
                                    batchStream.drainHandler(done -> {

                                        // Resume reading data
                                        batchStream.resume();

                                    });
                                }
                            }).endHandler(v -> batchStream.end())
                            .exceptionHandler(t -> {
                                if (t instanceof ParserException parserException) {
                                    ReceivedData receivedData = parserException.getReceivedData();
                                    receivedData.message(true, "系统繁忙");
                                    batchStream.write(receivedData);
                                }
                                log.error("exceptionHandler {}", t.getMessage(), t);
                                batchStream.end();
                            })
                    ;
                    // Resume reading data
                    batchStream.resume();
                })
                .exceptionHandler(throwable -> {
                    log.error("Connection error: " + throwable.getMessage(), throwable);
                });
        await(netServer.listen(iotGatewayNetPort));
        log.info("NET server started on port {}", iotGatewayNetPort);
    }

    private static BatchStream getStream(NetSocket socket) {
        return new BatchStream(socket, socket, (receivedData, serialNumber) -> {
            int command = receivedData.getCommand();
            Function<ReceivedData, String> function = commandMappings.get(command);
            if (function != null) {
                if (command == Commands.LOGIN_843) {
                    serialNumber.set(function.apply(receivedData));
                } else {
                    if (serialNumber.get() == null) {
                        receivedData.message(false, "未登录");
                    } else {
                        receivedData.setSerialNumber(serialNumber.get());
                        function.apply(receivedData);
                    }
                }
            } else {
                receivedData.message(false, "未找到指令");
            }
            if (!receivedData.isSuccess()) {
                log.info("Error Result :{}", receivedData.dubugString());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
