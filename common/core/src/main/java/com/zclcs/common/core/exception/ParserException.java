package com.zclcs.common.core.exception;

import com.zclcs.common.core.bean.ReceivedData;

import java.io.Serial;

/**
 * @author zclcs
 */
public class ParserException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ReceivedData receivedData;

    public ParserException(String message, Throwable cause, ReceivedData receivedData) {
        super(message, cause);
        this.receivedData = receivedData;
    }

    public ParserException(String message, ReceivedData receivedData) {
        super(message);
        this.receivedData = receivedData;
    }

    public ReceivedData getReceivedData() {
        return receivedData;
    }
}
