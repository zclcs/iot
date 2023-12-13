package com.zclcs;

import com.zclcs.common.core.bean.Command843Data;
import com.zclcs.common.core.bean.ReceivedData;
import com.zclcs.iot.lib.codec.attendance.stream.BatchStream;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import static com.zclcs.iot.lib.codec.attendance.utils.AttendanceCodecUtil.encode843;
import static io.vertx.core.Future.await;

/**
 * @author zhouc
 */
@Slf4j
public class IotEmulatorVerticle extends AbstractVerticle {

    public static void main(String[] args) throws Exception {
        var vertx = Vertx.vertx();
        try {
            vertx.deployVerticle(IotEmulatorVerticle.class, new DeploymentOptions()
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
        NetSocket netSocket = await(vertx.createNetClient().connect(5555, "127.0.0.1"));
        log.info("Connected to server");
        // Create batch stream for reading and writing
        BatchStream batchStream = new BatchStream(netSocket, netSocket, (receivedData, serialNumber) -> {
        });

        // Pause reading data
        batchStream.pause();

        // Register read stream handler
        batchStream.handler(receivedData -> {
                    log.info("Received data: {}", receivedData.decodeContentAsString());
                })
                .endHandler(v -> batchStream.end())
                .exceptionHandler(t -> {
                    log.error("exceptionHandler {}", t.getMessage(), t);
                    batchStream.end();
                });

        // Resume reading data
        batchStream.resume();

        ReceivedData receivedData = new ReceivedData();
        Command843Data command843Data = new Command843Data("q312312saasd", "1436607");
        receivedData.setContent(encode843(command843Data));
        receivedData.setCommand(843);
        receivedData.setSession("4sd54f5s4df5s4".getBytes());
        receivedData.setVersion(1);

        batchStream.write(receivedData);

        netSocket
                .exceptionHandler(throwable -> {
                    log.error("Connection error: " + throwable.getMessage(), throwable);
                });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}