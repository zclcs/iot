package com.zclcs.iot.lib.codec.attendance.stream;

import com.zclcs.common.core.bean.ReceivedData;
import com.zclcs.common.core.exception.ParserException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.zclcs.iot.lib.codec.attendance.utils.AttendanceCodecUtil.*;

/**
 * @author zhouc
 */
@Slf4j
public class BatchStream implements ReadStream<ReceivedData>, WriteStream<ReceivedData> {

    private final RecordParser recordParser;
    private final WriteStream<Buffer> writeStream;
    private int size = -1;
    private Handler<Throwable> exceptionHandler;
    private final int lenHeader = 5;
    private final int antherLength = 29;
    private final AtomicReference<String> serialNumber = new AtomicReference<>();
    /**
     * 解码完成后的操作
     */
    private final BiConsumer<ReceivedData, AtomicReference<String>> consumer;

    public BatchStream(ReadStream<Buffer> rs, WriteStream<Buffer> ws, BiConsumer<ReceivedData, AtomicReference<String>> consumer) {
        Objects.requireNonNull(rs, "ReadStream");
        Objects.requireNonNull(ws, "WriteStream");
        Objects.requireNonNull(consumer, "Consumer");
        recordParser = RecordParser.newFixed(lenHeader, rs);
        writeStream = ws;
        // Propagate exceptions to the current stream
        recordParser.exceptionHandler(throwable -> {
            if (exceptionHandler != null) {
                exceptionHandler.handle(throwable);
            }
        });
        writeStream.exceptionHandler(throwable -> {
            if (exceptionHandler != null) {
                exceptionHandler.handle(throwable);
            }
        });
        this.consumer = consumer;
    }

    @Override
    public BatchStream exceptionHandler(Handler<Throwable> handler) {
        exceptionHandler = handler;
        return this;
    }

    @Override
    public Future<Void> write(ReceivedData receivedData) {
        if (receivedData == null) {
            return Future.failedFuture(new NullPointerException());
        }
        Future<Void> write = writeStream.write(encode(receivedData));
        receivedData.reset();
        return write;
    }

    @Override
    public void write(ReceivedData receivedData, Handler<AsyncResult<Void>> handler) {
        if (receivedData == null) {
            NullPointerException err = new NullPointerException();
            if (exceptionHandler != null) {
                exceptionHandler.handle(err);
            }
            if (handler != null) {
                handler.handle(Future.failedFuture(err));
            }
        } else {
            writeStream.write(encode(receivedData), handler);
            receivedData.reset();
        }
    }

    @Override
    public Future<Void> end() {
        return writeStream.end();
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        writeStream.end(handler);
    }

    @Override
    public BatchStream setWriteQueueMaxSize(int maxSize) {
        writeStream.setWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return writeStream.writeQueueFull();
    }

    @Override
    public BatchStream drainHandler(Handler<Void> handler) {
        writeStream.drainHandler(handler);
        return this;
    }

    @Override
    public BatchStream handler(Handler<ReceivedData> handler) {
        if (handler == null) {
            recordParser.handler(null);
            recordParser.exceptionHandler(null);
            recordParser.endHandler(null);
            return this;
        }
        ReceivedData receivedData = new ReceivedData();
        recordParser.handler(buffer -> {
            try {
                if (size == -1) {
                    // Message size mode
                    size = decodeHead(buffer);
                    recordParser.fixedSizeMode(size + antherLength);
                } else {
                    // Message body mode
                    int bodySize = size;
                    size = -1;
                    recordParser.fixedSizeMode(lenHeader);
                    decodeBody(buffer, bodySize, receivedData);
                    consumer.accept(receivedData, serialNumber);
                    handler.handle(receivedData);
                }
            } catch (Throwable throwable) {
                if (exceptionHandler != null) {
                    ParserException parserException = new ParserException(throwable.getMessage(), throwable, receivedData);
                    exceptionHandler.handle(parserException);
                }
            }
        });
        return this;
    }

    @Override
    public BatchStream pause() {
        recordParser.pause();
        return this;
    }

    @Override
    public BatchStream fetch(long l) {
        recordParser.fetch(l);
        return this;
    }

    @Override
    public BatchStream resume() {
        recordParser.resume();
        return this;
    }

    @Override
    public BatchStream endHandler(Handler<Void> endHandler) {
        recordParser.endHandler(endHandler);
        return this;
    }
}
