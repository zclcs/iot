package com.zclcs.iot.lib.codec.attendance.function;

import com.zclcs.common.core.bean.*;
import com.zclcs.common.core.constant.Commands;
import com.zclcs.common.core.constant.DeviceType;
import com.zclcs.common.core.service.SimpleDecodeService;
import com.zclcs.common.core.utils.CodeCUtil;
import com.zclcs.common.core.utils.StringsUtil;
import com.zclcs.common.redis.utils.CacheUtil;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zclcs
 */
public class DecodeContentFunction {

    public static Map<Integer, Function<ReceivedData, String>> commandMappings = new HashMap<>();

    private static final Function<ReceivedData, String> FUNCTION_843 = (ReceivedData receivedData) -> {
        Command843Data command843Data = new SimpleDecodeService<>(t -> {
            Buffer buffer = Buffer.buffer(t.getContent());
            var thirdCode = CodeCUtil.trimString(new String(buffer.getBytes(0, 32), StandardCharsets.US_ASCII));
            var serialNumber = CodeCUtil.trimString(new String(buffer.getBytes(32, 64), StandardCharsets.US_ASCII));
            return new Command843Data(thirdCode, serialNumber);
        }, receivedData).decode();
        String serialNumber = command843Data.getSerialNumber();
        DeviceProject deviceProject = CacheUtil.getDeviceProject(DeviceType.ATTENDANCE, serialNumber);
        if (deviceProject != null) {
            receivedData.message(true, "登录成功");
            return serialNumber;
        } else {
            receivedData.message(false, "未找到设备");
            return null;
        }
    };


    private static final Function<ReceivedData, String> FUNCTION_839 = (ReceivedData receivedData) -> {
        DeviceProject deviceProject = CacheUtil.getDeviceProject(DeviceType.ATTENDANCE, receivedData.getSerialNumber());
        if (deviceProject == null || deviceProject.getProjectId() == null) {
            receivedData.message(false, "设备已解绑");
            return null;
        }
        Long projectId = deviceProject.getProjectId();
        Project project = CacheUtil.getProject(projectId);
        String projectName = project.getProjectName();
        Buffer protocol = Buffer.buffer();
        if (StringsUtil.isBlank(projectName)) {
            protocol.appendBytes(new byte[100]);
        } else {
            protocol.appendBytes(Arrays.copyOf(projectName.getBytes(StandardCharsets.UTF_8), 100));
        }
        Map<Long, ProjectWorker> projectWorkers = CacheUtil.getProjectWorker(projectId);
        if (projectWorkers != null) {
            for (ProjectWorker projectWorker : projectWorkers.values()) {
                if (projectWorker.getSerialNumber() != null && !projectWorker.getSerialNumber().equals(receivedData.getSerialNumber())) {
                    continue;
                }
                if (!"1".equals(projectWorker.getStatus())) {
                    continue;
                }
                protocol.appendBytes(CodeCUtil.toBytes(Math.toIntExact(projectWorker.getWorkerId()), 4));
                byte[] workerNameBytes;
                byte[] workerName = projectWorker.getWorkerName().getBytes(StandardCharsets.UTF_8);
                if (receivedData.getVersion() == 5) {
                    //版本等于5,按照协议上50个字节返回最少支持15个汉字
                    workerNameBytes = Arrays.copyOf(workerName, 50);
                } else {
                    //版本不是5，按照协议上10个字节返回
                    workerNameBytes = Arrays.copyOf(workerName, 10);
                }
                protocol.appendBytes(workerNameBytes);
                protocol.appendBytes(Arrays.copyOf(projectWorker.getIdCardNumber().getBytes(StandardCharsets.UTF_8), 18));
                protocol.appendBytes(CodeCUtil.toBytes(Integer.parseInt(projectWorker.getCollectImageSize()), 4));
            }
        }
        receivedData.message(true, protocol.getBytes());
        return null;
    };

    static {
        commandMappings.put(Commands.LOGIN_843, FUNCTION_843);
        commandMappings.put(Commands.WHITELIST_839, FUNCTION_839);
    }
}
