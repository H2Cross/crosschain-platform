package com.tanklab.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 跨链信息表
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
//@TableName("crossTx_info")
@ApiModel(value="Crosschain对象", description="跨链交易信息表")
public class Crosschain implements Serializable {

    private static final long serialVersionUID=1L;

      @TableId(value = "tx_id", type = IdType.AUTO)
    private int txId;
    private String srcIp;
    private int srcPort;
    private String dstIp;
    private int dstPort;
    private String srcChainType;
    private String dstChainType;
    private String srcHash;
    //@ApiModelProperty(value = "跨链类型")
    private String dstHash;
    private String responseHash;
    private Date txTime;
    private String txHash;


}
