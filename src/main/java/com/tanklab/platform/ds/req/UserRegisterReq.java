package com.tanklab.platform.ds.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="用户注册请求统一格式")
public class UserRegisterReq {
    @ApiModelProperty(value = "手机号")
    private String user_mobile;

    @ApiModelProperty(value = "邮箱")
    private String user_email;

    @ApiModelProperty(value = "所在链")
    private String link_url;

    @ApiModelProperty(value = "密码")
    private String user_pswd;
}

