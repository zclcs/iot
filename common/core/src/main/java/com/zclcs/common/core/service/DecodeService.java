package com.zclcs.common.core.service;

import com.zclcs.common.core.bean.ReceivedData;

/**
 * @author zhouc
 */
public interface DecodeService<T> {

    /**
     * 解码
     *
     * @param receivedData 包体
     * @return 结果
     */
    T decode(ReceivedData receivedData);

}
