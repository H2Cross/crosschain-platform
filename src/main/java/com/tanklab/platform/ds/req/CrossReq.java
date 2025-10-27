package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;




    @Data
    @ApiModel(value="跨链请求格式")
    public class CrossReq {

        @ApiModelProperty(value = "原链IP")
        private String src;
//        @ApiModelProperty(value = "原链端口")
//        private int srcPort;
        @ApiModelProperty(value = "目的链IP")
        private String dst;
//        @ApiModelProperty(value = "目的链端口")
//        private int dstPort;
        @ApiModelProperty(value = "源链类型")
        private String srcChainType;
        @ApiModelProperty(value = "目的链类型")
        private String dstChainType;




    }
