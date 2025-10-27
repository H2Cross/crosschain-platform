package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="添加链的请求格式")
public class AddChainReq {
    @ApiModelProperty(value = "链IP")
    private String uploadchainIP;

    @ApiModelProperty(value = "链类型")
    private Integer chainTYPE;

    @ApiModelProperty(value = "持有者id")
    private Integer ownerID;

    @ApiModelProperty(value = "状态")
    private Integer chainSTATUS;

}
