package com.tanklab.platform.controller;

import com.tanklab.platform.ds.resp.CommonResp;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.tanklab.platform.service.SysService;

import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZZY
 * @date 2025/6/5 23:34
**/

@RestController
@RequestMapping("/platform/system")
public class SysController {

    @Autowired
    SysService sysService;

    @ApiOperation(value = "查询服务器CPU占用情况")
    @GetMapping("/queryCPUInfo")
    public CommonResp QueryCPUinfo() {
        return sysService.queryCPUinfo();
    }

    @ApiOperation(value = "查询简介+时间轴")
    @GetMapping("/queryProjectInfo")
    public CommonResp queryProjectInfo() {
        return sysService.queryProjectInfo();
    }

    @ApiOperation(value = "查询交易时间轴")
    @GetMapping("/queryTxTimelineInfo")
    public CommonResp queryTxTimelineInfo() {
        return sysService.queryTxTimelineInfo();
    }

//    @ApiOperation(value = "查询近期跨链数据")
//    @GetMapping("/queryTxDataInfo")
//    public CommonResp queryTxDataInfo() {
//        return sysService.queryTxDataInfo();
//    }

}
