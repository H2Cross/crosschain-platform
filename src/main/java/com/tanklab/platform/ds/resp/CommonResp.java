package com.tanklab.platform.ds.resp;

import com.tanklab.platform.common.ResultCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="所有请求返回内容统一格式")
public class CommonResp<T> {
    @ApiModelProperty(value = "请求提示code")
    private String code;

    @ApiModelProperty(value = "说明信息")
    private String msg;

    @ApiModelProperty(value = "数据（泛型）")
    private T data;

    public void setRet(ResultCode ret){
        this.setCode(ret.Code);
        this.setMsg(ret.Msg);
    }

    public CommonResp(){
        this.setCode(ResultCode.TEST.Code);
        this.setMsg(ResultCode.TEST.Msg);
    }

    public void setMessage(String s) {
    }
}
