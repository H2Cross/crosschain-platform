package com.tanklab.platform.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanklab.platform.common.ResultCode;
import com.tanklab.platform.ds.resp.CommonResp;

import com.tanklab.platform.entity.Contract;

import com.tanklab.platform.mapper.ContractMapper;

import com.tanklab.platform.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {
    @Autowired
    public ContractMapper contractMapper;
//    @Autowired
//    private ContractServiceImpl ContractService;

    @Override
    public CommonResp queryContractInfo(String ipChain, Integer port) {
        CommonResp querycontractesp = new CommonResp();

        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        if (ipChain != null && ! ipChain.isEmpty()) {
            wrapper.eq("ip_chain", ipChain);
        }
        if (port != null) {
            wrapper.eq("port", port);
        }
//        if (chainNumber != null) {
//            wrapper.eq("chain_number", chainNumber);
//        }

        List<Contract> contracts = contractMapper.selectList(wrapper);
        System.out.println(contracts);
        JSONObject contractsinfo = new JSONObject();
        JSONArray contractsarr = new JSONArray();
        for (int i=0;i<contracts.size();i++){
            JSONObject percontractinfo = new JSONObject();
            percontractinfo.put("contractName",contracts.get(i).getContractName());
            percontractinfo.put("contractAddress",contracts.get(i).getContractAddress());
            contractsarr.add(percontractinfo);
        }
        contractsinfo.put("contractsinfo",contractsarr);
        querycontractesp.setRet(ResultCode.SUCCESS);
        querycontractesp.setData(contractsinfo);
        return querycontractesp;
    }
}
