package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="用户修改昵称")
public class UserSetNickNameReq{

    @ApiModelProperty(value = "新昵称")
    private String new_nick_name;
}
