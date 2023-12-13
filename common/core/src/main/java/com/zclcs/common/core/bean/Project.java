package com.zclcs.common.core.bean;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 项目信息表
 * </p>
 *
 * @author zhouchenglong
 * @since 2020-10-13
 */
@Data
public class Project implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long projectId;

    private Long userId;

    private String projectName;

    private Long contractorId;

    private String category;

    private String areaCode;

    private String prjStatus;

    private BigDecimal lng;

    private BigDecimal lat;

}
