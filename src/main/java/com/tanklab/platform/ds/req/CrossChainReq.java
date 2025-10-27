package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="跨链操作请求")
public class CrossChainReq {
    @ApiModelProperty(value = "源链服务器IP")
    private String srcIp;

    @ApiModelProperty(value = "源链类型 (ethereum/chainmaker/h2chain)")
    private String srcChainType;

    @ApiModelProperty(value = "目标链服务器IP")
    private String dstIp;

    @ApiModelProperty(value = "目标链类型 (ethereum/chainmaker/h2chain)")
    private String dstChainType;
} 