package com.tanklab.platform.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanklab.platform.common.ResultCode;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Contractfunc;
import com.tanklab.platform.mapper.ContractfuncMapper;
import com.tanklab.platform.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FunctionImpl extends ServiceImpl<ContractfuncMapper, Contractfunc> implements FunctionService {
    @Autowired
    public ContractfuncMapper functionMapper;
//    @Autowired
//    private ContractServiceImpl ContractService;



    @Override
    public CommonResp queryFunctionInfo(String ipChain, Integer port, String contractAddress) {
        CommonResp queryfunctionesp = new CommonResp();

        QueryWrapper<Contractfunc> wrapper = new QueryWrapper<>();
        wrapper.eq("ip_chain", ipChain);
        wrapper.eq("port", port);
        wrapper.eq("contract_address", contractAddress);

        List<Contractfunc> functions = functionMapper.selectList(wrapper);

        if (functions.isEmpty()) {
            queryfunctionesp.setRet(ResultCode.FAILURE);
            queryfunctionesp.setMessage("No contracts found with the provided criteria.");
            return queryfunctionesp;
        }

//        Set<String> functionNames = new HashSet<>();
        JSONArray allfunction = new JSONArray();
        for (Contractfunc function : functions) {
            JSONObject t = new JSONObject();
            t.put("functionName",function.getFunctionName());
            t.put("funcArgDes",function.getFuncArgDes());
            allfunction.add(t);
        }

        JSONObject contractsInfo = new JSONObject();
        contractsInfo.put("functions", allfunction);

        queryfunctionesp.setRet(ResultCode.SUCCESS);
        queryfunctionesp.setData(contractsInfo);
        return queryfunctionesp;

    }
}