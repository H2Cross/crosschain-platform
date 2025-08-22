package com.tanklab.platform.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanklab.platform.common.ResultCode;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Sys;
import com.tanklab.platform.mapper.SysMapper;
import com.tanklab.platform.service.SysService;
import jnr.ffi.annotations.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

@Service
public class SysServiceImpl extends ServiceImpl<SysMapper, Sys> implements SysService {

    @Autowired
    private SysMapper sysMapper;

    public Date todate(String dateString){
        //String dateString = "2025-06-20";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 1. 先解析为 LocalDate
        LocalDate localDate = LocalDate.parse(dateString, formatter);

        // 2. 将 LocalDate 转换为 Date（通过 Instant）
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return date;
    }

    @Override
    public CommonResp queryCPUinfo(){
        CommonResp cpuresp = new CommonResp<>();
        cpuresp.setRet(ResultCode.SUCCESS);

        JSONArray two = new JSONArray();

        for (int i = 1;i<=10;i++){
            JSONObject cpu = new JSONObject();
            Random random = new Random();
            int min = 10;
            int max = 80;
            cpu.put("server","192.168.0."+ Integer.toString(i));
            cpu.put("cpu", (random.nextInt(max-min+1)+min)/100.0);
            cpu.put("memory", (random.nextInt(max-min+1)+min)/100.0);
            two.add(cpu);
        }
        cpuresp.setData(two);
        return cpuresp;
    }

    @Override
    public CommonResp queryProjectInfo(){
        CommonResp proinfo = new CommonResp<>();
        proinfo.setRet(ResultCode.SUCCESS);

        JSONObject intro = new JSONObject();
        intro.put("introduction","H2Cross跨链互通平台——平台通过创新性地结合信标-分片中继链架构、分层跨链交互协议栈、分布式数字身份管理和低耦合应用组件库，构建了一套完整的区块链互操作性解决方案，有效解决了异构区块链在数据结构、共识机制和智能合约执行环境差异带来的互操作挑战。该平台不仅支持长安链、以太坊私链等多种区块链系统的接入，还通过其模块化设计保证了系统的扩展性和适应性，为医疗数据等敏感信息在不同区块链网络间的安全、可信流转提供了技术基础。");
        JSONArray process = new JSONArray();

        JSONObject day1 = new JSONObject();
        day1.put("Time","2023-03-11");
        day1.put("Describe","课题启动会召开");
        process.add(day1);

        JSONObject day2 = new JSONObject();
        day2.put("Time","2023-07-01");
        day2.put("Describe","线下集中开发");
        process.add(day2);

        JSONObject day3 = new JSONObject();
        day3.put("Time","2024-01-01");
        day3.put("Describe","第一次年度报告会议召开");
        process.add(day3);


        JSONObject day4 = new JSONObject();
        day4.put("Time","2024-07-01");
        day4.put("Describe","中期报告会议");
        process.add(day4);


        JSONObject day5 = new JSONObject();
        day5.put("Time","2025-01-15");
        day5.put("Describe","跨链平台2.0阶段性总结会议");
        process.add(day5);


        JSONObject day6 = new JSONObject();
        day6.put("Time","2025-03-01");
        day6.put("Describe","第二次年度报告会议召开");
        process.add(day6);

        JSONObject day7 = new JSONObject();
        day7.put("Time","2025-03-15");
        day7.put("Describe","上海区块链专项集中会议召开");
        process.add(day7);

        JSONObject t = new JSONObject();
        t.put("introduction","H2Cross跨链互通平台——平台通过创新性地结合信标-分片中继链架构、分层跨链交互协议栈、分布式数字身份管理和低耦合应用组件库，构建了一套完整的区块链互操作性解决方案，有效解决了异构区块链在数据结构、共识机制和智能合约执行环境差异带来的互操作挑战。该平台不仅支持长安链、以太坊私链等多种区块链系统的接入，还通过其模块化设计保证了系统的扩展性和适应性，为医疗数据等敏感信息在不同区块链网络间的安全、可信流转提供了技术基础。");
        t.put("timeline",process);
        proinfo.setData(t);

        return proinfo;
    }

    @Override
    public CommonResp queryTxTimelineInfo(){
        CommonResp txinfo = new CommonResp<>();
        txinfo.setRet(ResultCode.SUCCESS);
        JSONArray d = new JSONArray();
        String today = "2025-06-1";
        for (int i=0;i<10;i++){
            JSONObject days = new JSONObject();
            String nowday = today + Integer.toString(i);
            Random random = new Random();
            int min = 1;
            int max = 20;
            days.put("day",nowday);
            days.put("num", random.nextInt(max-min+1)+min);
            d.add(days);
        }
        txinfo.setData(d);
        return txinfo;
    }

}

