package com.tanklab.platform.controller;

import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.service.ContractService;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 链信息表 前端控制器
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@RestController
@RequestMapping("/platform/contract")
public class ContractController {
    @Autowired
    ContractService contractService;

    @ApiOperation(value = "查询某条链的所有合约描述+地址")
    @GetMapping("/queryContractInfo")
    public CommonResp queryContractInfo(
            @RequestParam(required = true) String ipChain,
            @RequestParam(required = true) Integer port
    ) {
        // 调用服务层方法，根据参数查询链的信息
        return contractService.queryContractInfo(ipChain, port);
    }

}