package com.tanklab.platform.controller;

import com.tanklab.platform.ds.req.CrossReq;
import com.tanklab.platform.ds.req.StartGatewaysReq;
import com.tanklab.platform.ds.req.CrossChainReq;
import com.tanklab.platform.ds.req.FullCrossChainReq;
import com.tanklab.platform.ds.req.CommandReq;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.service.CrosschainService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 跨链信息表 前端控制器
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@RestController
@RequestMapping("/platform/crosschain")
public class CrosschainController {

    @Autowired
    CrosschainService crosschainService;

    @ApiOperation(value="查询历史跨链记录")
    @GetMapping("/queryAllCrossTx")
    public CommonResp QueryChainInfo(){
        return crosschainService.queryAllCrossTx();
    }

    @ApiOperation(value = "查询某个交易的具体信息")
    @GetMapping("/queryTxInfo")
    public CommonResp queryTxInfo(
            @RequestParam(required = false) String txHash,
            @RequestParam(required = false) Integer txId
    ) {
        return crosschainService.queryTxInfo(txHash, txId);
    }

    @ApiOperation(value="插入跨链请求内容")
    @PostMapping("/addCrossTx")
    public CommonResp addCrossTx (@RequestBody CrossReq addbeefreq) {
        return crosschainService.addCrossTx(addbeefreq);
    }

    @ApiOperation(value="开发者模式命令执行")
    @PostMapping("/cmdExecute")
    public CommonResp cmdExecute (@RequestBody CommandReq req) {
        return crosschainService.cmdExecute(req);
    }

    @ApiOperation(value="启动跨链网关")
    @PostMapping("/startGateways")
    public CommonResp startGateways(@RequestBody StartGatewaysReq req) {
        return crosschainService.startGateways(
            req.getSrcIp(),
            req.getSrcChainType(),
            req.getDstIp(),
            req.getDstChainType(),
            req.getRelayIp()
        );
    }

    @ApiOperation("执行跨链操作")
    @PostMapping("/execute")
    public CommonResp executeCrossChain(@RequestBody CrossChainReq req) {
        return crosschainService.executeCrossChain(
            req.getSrcIp(),
            req.getSrcChainType(),
            req.getDstIp(),
            req.getDstChainType()
        );
    }

    @ApiOperation("执行完整的跨链操作(包含启动网关)")
    @PostMapping("/execute/full")
    public CommonResp executeFullCrossChain(@RequestBody FullCrossChainReq req) {
        return crosschainService.executeFullCrossChain(
            req.getSrcIp(),
            req.getSrcChainType(),
            req.getDstIp(),
            req.getDstChainType(),
            req.getRelayIp()
        );
    }
}


