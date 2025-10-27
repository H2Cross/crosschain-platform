package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="用户修改密码")
public class UserSetPswdReq{

    @ApiModelProperty(value = "旧密码")
    private String old_pswd;

    @ApiModelProperty(value = "新密码")
    private String new_pswd;
}

