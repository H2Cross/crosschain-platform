package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="完整跨链操作请求（包含网关启动和跨链操作）")
public class FullCrossChainReq {
    @ApiModelProperty(value = "源链服务器IP")
    private String srcIp;

    @ApiModelProperty(value = "源链类型 (ethereum/chainmaker/h2chain)")
    private String srcChainType;

    @ApiModelProperty(value = "目标链服务器IP")
    private String dstIp;

    @ApiModelProperty(value = "目标链类型 (ethereum/chainmaker/h2chain)")
    private String dstChainType;

    @ApiModelProperty(value = "中继链服务器IP")
    private String relayIp;

    @ApiModelProperty(value = "源链应用ID")
    private String srcappId;

    @ApiModelProperty(value = "目的链应用ID")
    private String dstappId;

    @ApiModelProperty(value = "应用参数")
    private String appArgs;
} 