package com.tanklab.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Contract;

public interface ContractService extends IService<Contract> {


    CommonResp queryContractInfo(String chainIp, Integer port);
}
