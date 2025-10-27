package com.tanklab.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Contractfunc;



public interface FunctionService extends IService<Contractfunc> {


    //CommonResp doCross(int param, int crosstype, int crossFrom, int crossTo);



    CommonResp queryFunctionInfo(String ipChain, Integer port, String contractAddress);


}
