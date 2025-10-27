package com.tanklab.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 链信息表
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chain_info")
@ApiModel(value = "Chain对象", description = "链信息表")
public class Chain implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "chain_id")
    private Integer chainId;

    private String ipAddress;

    private Integer port;

    private String chainType;

    // private Boolean isProcessed;

}
