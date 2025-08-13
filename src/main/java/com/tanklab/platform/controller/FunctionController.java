package com.tanklab.platform.controller;

import com.tanklab.platform.ds.req.QueryFunctionReq;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.service.FunctionService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/platform/function")
public class FunctionController {
    @Autowired
    FunctionService functionService;

    @ApiOperation(value = "查询函数的信息")
    @PostMapping("/queryFunctionInfo")
    public CommonResp queryContractInfo(@RequestBody QueryFunctionReq queryFunctionReq) {
        // 调用服务层方法，根据参数查询链的信息
        String ipChain = queryFunctionReq.getIpChain();
        Integer port = queryFunctionReq.getPort();
        String contractAddress = queryFunctionReq.getContractAddress();
        return functionService.queryFunctionInfo(ipChain, port, contractAddress);
    }

}