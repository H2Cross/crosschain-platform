package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "区块链IP请求格式")
public class ChainReq {

    @ApiModelProperty(value = "TopN")
    private Integer topN;

    @ApiModelProperty(value = "区块链IP")
    private String chainIP;

//    @ApiModelProperty(value = "身份token")
//    private String authorizationToken;

}
