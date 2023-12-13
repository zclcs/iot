package com.zclcs.common.core.bean;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 设备与项目绑定关系数据表_用电设备
 * </p>
 *
 * @author zhouchenglong
 * @since 2021-08-13
 */
@Data
public class DeviceProject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long deviceBindingId;

    private String serialNumber;

    private Long projectId;

    private Long subprjId;

    private String installLocation;

    private String workerName;

    private String idCardNumber;

    private String bindingStatus;

    private LocalDateTime bindingTime;

    private LocalDateTime untieTime;

    private Long corpId;

    private String devicePerson;

    private String devicePersonPhone;

    private String remark;

    private String createName;

    private LocalDateTime createDate;

    private String updateName;

    private LocalDateTime updateDate;

    private String deleteName;

    private LocalDateTime deleteDate;

    private String deleted;

    private String enterExitStatus;

    private String deviceNo;

    private BigDecimal limitedWeight;

    private String deviceType;

    private String dataSource;

}
