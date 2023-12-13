package com.zclcs.common.core.service;

import com.zclcs.common.core.bean.ReceivedData;

/**
 * @author zclcs
 */

public class SimpleDecodeService<T> {

    private final DecodeService<T> decodeService;
    private final ReceivedData receivedData;

    public SimpleDecodeService(DecodeService<T> decodeService, ReceivedData receivedData) {
        this.decodeService = decodeService;
        this.receivedData = receivedData;
    }

    public T decode() {
        return decodeService.decode(receivedData);
    }
}
