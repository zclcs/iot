package com.zclcs.common.core.utils;

/**
 * @author zclcs
 */
public class CodeCUtil {

    /**
     * 8位
     */
    private static final int EIGHT = 8;

    /**
     * 包分隔符
     */
    public static final int PACKET_DELIMITER = 0x01;

    /**
     * 成功标志
     */
    public static final byte FLAG_SUCCESS = 0x00;

    /**
     * 失败标志
     */
    public static final byte FLAG_ERROR = 0x01;

    /**
     * 整形转换字节数组
     *
     * @param data 被转换得整形
     * @param size 字节数组大小
     * @return 字节数组
     */
    public static byte[] toBytes(int data, int size) {
        byte[] bytes = new byte[size];

        for (int i = 0; i < bytes.length; i++) {
            int position = i * EIGHT;
            bytes[i] = (byte) ((data & (0xff << position)) >> position);
        }
        return bytes;
    }

    /**
     * 字节数组转换整形
     *
     * @param bytes 字节数组
     * @return 整形
     */
    public static int toInt(byte[] bytes) {
        int num = 0;
        for (int i = 0; i < bytes.length; i++) {
            int position = i * EIGHT;
            num = num | (0xff << position & (bytes[i] << position));
        }
        return num;
    }

    /**
     * 去除字符串末尾的空格
     *
     * @param str 字符串
     * @return 去除空格后的字符串
     */
    public static String trimString(String str) {
        int blankIndex = str.indexOf('\0');
        if (blankIndex != -1) {
            return str.substring(0, blankIndex).strip();
        }
        return str;
    }

}
