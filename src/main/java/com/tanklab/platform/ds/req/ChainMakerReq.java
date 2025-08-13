package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="长安链请求格式")
public class ChainMakerReq {
    @ApiModelProperty(value = "长安链")
    private String chainmaker;
    @ApiModelProperty(value = "交易id")
    private String txId;
    @ApiModelProperty(value = "区块哈希")
    private String blockHash;
}
