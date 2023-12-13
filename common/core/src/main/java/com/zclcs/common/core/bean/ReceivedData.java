package com.zclcs.common.core.bean;

import com.zclcs.common.core.constant.Commands;
import com.zclcs.common.core.utils.CodeCUtil;
import com.zclcs.common.core.utils.StringsUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author zhouc
 */
@Getter
@EqualsAndHashCode
public class ReceivedData {

    /**
     * 接收到的消息 or 传递的消息
     */
    private byte[] content;

    /**
     * 设备会话标识
     */
    private byte[] session;

    /**
     * 指令
     */
    private int command;

    /**
     * 0x00 成功 0x01 失败
     */
    private byte flag;

    /**
     * 协议版本
     */
    private int version;

    /**
     * 传递设备序列号
     */
    private String serialNumber;

    /**
     * 返回得消息
     */
    private String message;

    public ReceivedData() {
        this.reset();
    }

    public ReceivedData(byte[] content, int version, byte[] session, int command, byte flag) {
        this.content = content;
        this.version = version;
        this.session = session;
        this.command = command;
        this.flag = flag;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setContent(String content) {
        this.content = StringsUtil.isBlank(content) ? null : content.getBytes(StandardCharsets.UTF_8);
    }

    public void setSession(byte[] session) {
        this.session = session;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag ? CodeCUtil.FLAG_SUCCESS : CodeCUtil.FLAG_ERROR;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        if (command == Commands.LOGIN_843) {
            throw new IllegalArgumentException("未登录前，设备序列号为空");
        }
        return serialNumber;
    }

    public void message(Boolean flag, String message) {
        setFlag(flag);
        setContent(message);
        this.message = message;
    }

    public void message(Boolean flag, byte[] message) {
        setFlag(flag);
        setContent(message);
        this.message = "";
    }

    public String dubugString() {
        if (StringsUtil.isBlank(message)) {
            return String.format("encode content command=%s flag=%s content=%s", command, isSuccess(), Arrays.toString(content));
        }
        return String.format("encode message command=%s flag=%s message=%s", command, isSuccess(), message);
    }

    public String decodeContentAsString() {
        return String.format("encode content command=%s flag=%s content=%s", command, isSuccess(), new String(content, StandardCharsets.UTF_8));
    }

    public boolean isSuccess() {
        return flag == CodeCUtil.FLAG_SUCCESS;
    }

    public void reset() {
        this.content = null;
        this.session = new byte[16];
        this.command = 0;
        this.flag = CodeCUtil.FLAG_SUCCESS;
        this.version = 0;
        this.serialNumber = null;
        this.message = null;
    }

    @Override
    public String toString() {
        return "ReceivedData{" +
                "content=" + Arrays.toString(content) +
                ", session=" + Arrays.toString(session) +
                ", command=" + command +
                ", flag=" + isSuccess() +
                ", version=" + version +
                ", serialNumber='" + serialNumber + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public String toRawString() {
        return "ReceivedData{" +
                "content=" + Arrays.toString(content) +
                ", session=" + Arrays.toString(session) +
                ", command=" + command +
                ", flag=" + flag +
                ", version=" + version +
                '}';
    }
}