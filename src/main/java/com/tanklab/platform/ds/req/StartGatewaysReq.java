package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="启动网关请求")
public class StartGatewaysReq {
    @ApiModelProperty(value = "源链服务器IP", required = true)
    private String srcIp;
    
    @ApiModelProperty(value = "源链类型(ethereum/chainmaker/h2chain)", required = true)
    private String srcChainType;
    
    @ApiModelProperty(value = "目标链服务器IP", required = true)
    private String dstIp;
    
    @ApiModelProperty(value = "目标链类型(ethereum/chainmaker/h2chain)", required = true)
    private String dstChainType;
    
    @ApiModelProperty(value = "中继链服务器IP", required = true)
    private String relayIp;
} 