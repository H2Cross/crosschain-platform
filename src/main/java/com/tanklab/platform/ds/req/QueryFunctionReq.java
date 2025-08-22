package com.tanklab.platform.ds.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@ApiModel(value="查询函数")
public class QueryFunctionReq {

    private String ipChain;
    private Integer port;
    private String contractAddress;
}
