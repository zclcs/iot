package com.zclcs.common.core.service;

/**
 * @author zhouc
 */
public interface EncodeService<T> {

    /**
     * 编码
     *
     * @param t 内容
     * @return 编码后的内容
     */
    byte[] encode(T t);

}
