package com.zclcs.common.core.bean;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 班组人员数据表
 * </p>
 *
 * @author LiZhiHong
 * @since 2020-10-16
 */
@Data
public class ProjectWorker implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long projectWorkerId;

    private Long projectId;

    private Long workerId;

    private String corpCode;

    private String workerName;

    private Long teamId;

    private Long projectCorpId;

    private String idCardType;

    private String idCardNumber;

    private String phoneNumber;

    private String status;

    private String workType;

    private String workerRole;

    private String manageType;

    private String serialNumber;

    /**
     * 是否允许开闸
     */
    private Integer isAllowOpened;

    /**
     * 拒绝通行原因
     */
    private String rejectPassCause;

    /**
     * 民族
     */
    private String nation;

    /**
     * 住址
     */
    private String address;


    private String collectImage;


    private String collectImageSize;

}
