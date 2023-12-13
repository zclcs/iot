package com.zclcs.iot.lib.codec.attendance.utils;

import com.zclcs.common.core.bean.Command843Data;
import com.zclcs.common.core.bean.ReceivedData;
import com.zclcs.common.core.exception.ParserException;
import com.zclcs.common.core.utils.CodeCUtil;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * @author zclcs
 */
public class AttendanceCodecUtil {

    public static Buffer encode(ReceivedData receivedData) {
        Buffer protocol = Buffer.buffer();
        //发送内容
        byte[] content = receivedData.getContent();

        //1添加头
        protocol.appendBytes(new byte[]{CodeCUtil.PACKET_DELIMITER});

        //2添加长度
        protocol.appendBytes(CodeCUtil.toBytes(content.length, 4));

        //3分包索引
        byte[] partIndex = {0x00, 0x00, 0x00, 0x00};
        protocol.appendBytes(partIndex);

        //4分包数量
        byte[] partCount = {0x00, 0x00, 0x00, 0x00};
        protocol.appendBytes(partCount);

        //5 版本
        byte[] version = CodeCUtil.toBytes(receivedData.getVersion(), 1);
        protocol.appendBytes(version);

        //6添加命令
        byte[] cmd = CodeCUtil.toBytes(receivedData.getCommand(), 2);
        protocol.appendBytes(cmd);

        //7.session
        byte[] session = receivedData.getSession();
        protocol.appendBytes(Arrays.copyOf(session, 16));

        //8.内容
        if (content.length > 0) {
            protocol.appendBytes(content);
        }

        //9.状态
        byte[] status = {receivedData.getFlag()};
        protocol.appendBytes(status);

        //10.结束标记
        protocol.appendBytes(new byte[]{CodeCUtil.PACKET_DELIMITER});
        return protocol;
    }


    public static int decodeHead(Buffer buffer) {
        // 包头校验
        var head = buffer.getByte(0);
        if (head != CodeCUtil.PACKET_DELIMITER) {
            // 包头错误
            throw new ParserException("包头错误", new ReceivedData());
        }
        // 数据包大小
        var bytes = buffer.getBytes(1, 5);
        return CodeCUtil.toInt(bytes);
    }

    public static void decodeBody(Buffer buffer, int bodySize, ReceivedData receivedData) {
        // 分包顺序索引
        buffer.getBytes(0, 4);
        // 分包总数
        buffer.getBytes(4, 8);
        // 版本
        byte[] version = buffer.getBytes(8, 9);
        receivedData.setVersion(CodeCUtil.toInt(version));
        // 命令
        byte[] command = buffer.getBytes(9, 11);
        receivedData.setCommand(CodeCUtil.toInt(command));
        // session
        byte[] session = buffer.getBytes(11, 27);
        receivedData.setSession(session);
        int bodyEnd = 27 + bodySize;
        byte[] body = buffer.getBytes(27, bodyEnd);
        // 包体校验
        if (body.length < bodySize) {
            // 包体内容长度不够
            throw new ParserException("包体内容长度不够", receivedData);
        }
        receivedData.setContent(body);
        // 状态
        buffer.getBytes(bodyEnd, bodyEnd + 1);
        // 包尾
        byte[] end = buffer.getBytes(bodyEnd + 1, bodyEnd + 2);
        if (end[0] != CodeCUtil.PACKET_DELIMITER) {
            // 包尾错误
            throw new ParserException("包尾错误", receivedData);
        }
    }

    public static byte[] encode843(Command843Data command843Data) {
        Buffer buffer = Buffer.buffer();
        buffer.appendBytes(Arrays.copyOf(command843Data.getThirdCode().getBytes(StandardCharsets.US_ASCII), 32));
        buffer.appendBytes(Arrays.copyOf(command843Data.getSerialNumber().getBytes(StandardCharsets.US_ASCII), 32));
        return buffer.getBytes();
    }


}
