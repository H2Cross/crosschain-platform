package com.tanklab.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Sys;


public interface SysService extends IService<Sys> {

    CommonResp queryCPUinfo();
//    CommonResp queryFunctionInfo(String ipChain, Integer port, Integer chainNumber, String contractName);
    CommonResp queryProjectInfo();

    CommonResp queryTxTimelineInfo();

}
