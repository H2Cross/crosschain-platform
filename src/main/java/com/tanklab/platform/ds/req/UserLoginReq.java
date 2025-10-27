package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="用户登录请求统一格式")
public class UserLoginReq {
    @ApiModelProperty(value = "账户")
    private String user_account;

    @ApiModelProperty(value = "密码")
    private String user_pswd;
}