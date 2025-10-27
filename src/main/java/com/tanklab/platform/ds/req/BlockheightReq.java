package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "区块哈希请求格式")
public class BlockheightReq {

    @ApiModelProperty(value = "区块链IP")
    private String chainIP;
    @ApiModelProperty(value = "区块高度")
    private String blockHEIGHT;

    @ApiModelProperty(value = "身份token")
    private String authorizationToken;

}
