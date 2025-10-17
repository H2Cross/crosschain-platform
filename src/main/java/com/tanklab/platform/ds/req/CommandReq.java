package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;




    @Data
    @ApiModel(value="命令执行格式")
    public class CommandReq {

        @ApiModelProperty(value = "命令")
        private String command;

    }
