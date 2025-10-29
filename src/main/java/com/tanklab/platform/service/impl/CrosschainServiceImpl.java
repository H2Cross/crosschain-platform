package com.tanklab.platform.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanklab.platform.common.ResultCode;
import com.tanklab.platform.common.SSHConfig;
import com.tanklab.platform.ds.req.CommandReq;
import com.tanklab.platform.ds.req.CrossReq;
import com.tanklab.platform.ds.resp.CommonResp;

import com.tanklab.platform.entity.Crosschain;
import com.tanklab.platform.entity.Sys;
import com.tanklab.platform.entity.User;
import com.tanklab.platform.mapper.CrosschainMapper;
import com.tanklab.platform.mapper.UserMapper;
import com.tanklab.platform.service.CrosschainService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bouncycastle.oer.Switch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 跨链信息表 进行跨链操作返回结果，还没有写与数据库的交互
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Service
public class CrosschainServiceImpl extends ServiceImpl<CrosschainMapper, Crosschain> implements CrosschainService {

    @Autowired
    private CrosschainMapper crosschainMapper;

    @Autowired
    public CrosschainServiceImpl CrosschainService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public CommonResp queryAllCrossTx(Integer txId) {
        CommonResp response = new CommonResp();

        try {
            // 使用QueryWrapper构建查询条件
            QueryWrapper<Crosschain> wrapper = new QueryWrapper<>();

            // 如果提供了txId且不为空，则根据txId查询
            if (txId != null) {
                wrapper.eq("tx_id", txId);
            }

            // 执行查询
            List<Crosschain> crosschainList = crosschainMapper.selectList(wrapper);
            System.out.println(crosschainList);
            Collections.reverse(crosschainList);

            // 构建响应数据
            JSONArray resultArray = new JSONArray();
            for (Crosschain crosschain : crosschainList) {
                JSONObject resultObj = new JSONObject();
                resultObj.put("txId", crosschain.getTxId());
                resultObj.put("srcIp", crosschain.getSrcIp());
                resultObj.put("srcPort", crosschain.getSrcPort());
                resultObj.put("dstIp", crosschain.getDstIp());
                resultObj.put("dstPort", crosschain.getDstPort());
                resultObj.put("srcChainType", crosschain.getSrcChainType());
                resultObj.put("dstChainType", crosschain.getDstChainType());
                resultObj.put("srcHash", crosschain.getSrcHash());
                resultObj.put("dstHash", crosschain.getDstHash());
                resultObj.put("txTime", crosschain.getTxTime());
                resultObj.put("txHash", crosschain.getTxHash());
                resultObj.put("responseHash", crosschain.getResponseHash());
                resultArray.add(resultObj);
            }
            // 设置响应数据
            JSONObject responseData = new JSONObject();
            responseData.put("crossTransactions", resultArray);
            response.setRet(ResultCode.SUCCESS);
            response.setData(responseData);

        } catch (Exception e) {
            // 错误处理
            response.setRet(ResultCode.FAILURE);
            response.setMessage("Failed to query cross-chain transactions. Error: " + e.getMessage());
        }

        return response;
    }

    @Override
    public CommonResp queryTxInfo(String txHash, Integer txId) {

        CommonResp response = new CommonResp();

        try {
            // 使用QueryWrapper构建查询条件
            QueryWrapper<Crosschain> wrapper = new QueryWrapper<>();
            if (txHash != null && !txHash.isEmpty()) {
                wrapper.eq("tx_hash", txHash);
            } else if (txId != null) {
                wrapper.eq("tx_id", txId);
            } else {
                // 如果没有提供有效的txHash或txId，返回错误信息
                response.setRet(ResultCode.FAILURE);
                response.setMessage("Please provide either txHash or txId");
                return response;
            }

            // 执行查询
            Crosschain crosschain = crosschainMapper.selectOne(wrapper);
            if (crosschain == null) {
                response.setRet(ResultCode.FAILURE);
                response.setMessage("No transaction found with the provided txHash or txId");
                return response;
            }

            // 构建响应数据
            JSONObject resultObj = new JSONObject();
            resultObj.put("txId", crosschain.getTxId());
            resultObj.put("srcChainType", crosschain.getSrcChainType());
            resultObj.put("dstChainType", crosschain.getDstChainType());
            resultObj.put("srcIp", crosschain.getSrcIp());
            resultObj.put("srcPort", crosschain.getSrcPort());
            resultObj.put("dstIp", crosschain.getDstIp());
            resultObj.put("dstPort", crosschain.getDstPort());
            resultObj.put("srcHash", crosschain.getSrcHash());
            resultObj.put("dstHash", crosschain.getDstHash());
            resultObj.put("responseHash", crosschain.getResponseHash());
            resultObj.put("txTime", crosschain.getTxTime());

            // 设置响应数据
            response.setRet(ResultCode.SUCCESS);
            response.setData(resultObj);

        } catch (Exception e) {
            // 错误处理
            response.setRet(ResultCode.FAILURE);
            response.setMessage("Failed to query transaction info. Error: " + e.getMessage());
        }

        return response;
    }

    @Override
    public CommonResp cmdExecute(CommandReq req) {
        CommonResp response = new CommonResp();
        String cmd = req.getCommand(); // 假设 CrossReq 有 command 字段
        System.out.println("[CMD] 执行命令: " + cmd);

        try {
            // 启动进程
            Process process = Runtime.getRuntime().exec(cmd);

            // 获取标准输出流和错误流
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder outputBuilder = new StringBuilder();
            String line;

            // 创建独立线程持续读取标准输出
            Thread outThread = new Thread(() -> {
                try {
                    String outLine;
                    while ((outLine = readLineSafe(stdOut)) != null) {
                        System.out.println("[OUT] " + outLine);
                        synchronized (outputBuilder) {
                            outputBuilder.append(outLine).append("\n");
                        }
                    }
                } catch (Exception ignored) {
                }
            });

            // 创建独立线程持续读取错误输出
            Thread errThread = new Thread(() -> {
                try {
                    String errLine;
                    while ((errLine = readLineSafe(stdErr)) != null) {
                        System.err.println("[ERR] " + errLine);
                        synchronized (outputBuilder) {
                            outputBuilder.append(errLine).append("\n");
                        }
                    }
                } catch (Exception ignored) {
                }
            });

            outThread.start();
            errThread.start();

            // 等待命令执行完
            int exitCode = process.waitFor();
            outThread.join();
            errThread.join();

            response.setRet(ResultCode.SUCCESS);
            response.setMsg("命令执行完成，exitCode=" + exitCode);
            response.setData(outputBuilder.toString());
        } catch (Exception e) {
            response.setRet(ResultCode.ERROR);
            response.setMsg("执行失败: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 安全地读取一行，避免阻塞或异常中断
     */
    private String readLineSafe(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public CommonResp addCrossTx(CrossReq crossReq) {
        String ethTxHash = new String();
        String chainmakerTxHash = new String();
        String h2chainTxHash = new String();
        CommonResp responseForF = new CommonResp();
        String[] srcIpAndPort = crossReq.getSrc().split(":");
        String srcIp = srcIpAndPort[0];
        int srcPort = Integer.parseInt(srcIpAndPort[1]);
        String[] dstIpAndPort = crossReq.getDst().split(":");
        String dstIp = dstIpAndPort[0];
        int dstPort = Integer.parseInt(dstIpAndPort[1]);
        Crosschain crosschain = new Crosschain().setSrcIp(srcIp).setDstIp(dstIp)
                .setSrcChainType(crossReq.getSrcChainType()).setDstChainType(crossReq.getDstChainType())
                .setSrcPort(srcPort).setDstPort(dstPort);
        String targetUrl = "http://192.168.0.44:8080/cross_chain?src-chain=" + crosschain.getSrcChainType()
                + "&dst-chain=" + crosschain.getDstChainType() + "&src-ip=" + srcIp + "&dst-ip=" + dstIp;
        // crosschain.setSrcPort(crossReq.getSrcPort());
        // crosschain.setDstPort(crossReq.getDstPort());
        // crosschain.setSrcIp(crossReq.getSrcIp());
        // crosschain.setDstIp(crossReq.getDstIp());
        System.out.println(crosschain);
        // crosschain.setDstIp("192.168.0.193");
        String logs = "";
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            logs = response.toString();
            // 正则表达式模式
            String src_hash_rex = "SOURCE_CHAIN_TX.*?(?:交易哈希|txHash):\\s?(0x[a-fA-F0-9]{64}|[a-fA-F0-9]{64})";

            // 编译正则表达式
            Pattern pattern = Pattern.compile(src_hash_rex);

            // 创建匹配器对象
            Matcher matcher = pattern.matcher(logs);

            // 检查是否找到匹配
            if (matcher.find()) {
                // 提取哈希值
                String src_hash = matcher.group(1);
                System.out.println("提取源链上的哈希值: " + src_hash);
                crosschain.setSrcHash(src_hash);
            } else {
                System.out.println("未找到匹配的哈希值");
            }
            /// 提取sendToEth的所有字符串
            String regex_eth = "sendToEth.*?(?:交易哈希|txHash)：?\\s?(0x[a-fA-F0-9]{64}|[a-fA-F0-9]{64})";
            Pattern pattern_eth = Pattern.compile(regex_eth);
            Matcher matcher_eth = pattern_eth.matcher(logs);
            ArrayList<String> hashValues_eth = new ArrayList<>();

            // Iterate over all matches and add them to the list
            while (matcher_eth.find()) {
                hashValues_eth.add(matcher_eth.group(1));
            }
            System.out.println(hashValues_eth);

            String regex_cmk = "sendToChainmaker.*?(?:交易哈希|txHash):?\\s?([a-fA-F0-9]{64})";
            Pattern pattern_cmk = Pattern.compile(regex_cmk);
            Matcher matcher_cmk = pattern_cmk.matcher(logs);
            ArrayList<String> hashValues_cmk = new ArrayList<>();

            // Iterate over all matches and add them to the list
            while (matcher_cmk.find()) {
                hashValues_cmk.add(matcher_cmk.group(1));
            }
            System.out.println(hashValues_cmk);

            String regex_h2c = "sendToH2Chain.*?(?:交易哈希|txHash):\\s?([a-fA-F0-9]{64})";
            Pattern pattern_h2c = Pattern.compile(regex_h2c);
            Matcher matcher_h2c = pattern_h2c.matcher(logs);
            ArrayList<String> hashValues_h2c = new ArrayList<>();
            System.out.println(logs);
            // Iterate over all matches and add them to the list
            while (matcher_h2c.find()) {
                hashValues_h2c.add(matcher_h2c.group(1));
            }
            System.out.println(hashValues_h2c);
            if (crosschain.getSrcChainType().equals("eth") && crosschain.getDstChainType().equals("chainmaker")) {
                crosschain.setResponseHash(hashValues_eth.get(0));
                crosschain.setDstHash(hashValues_cmk.get(0));
            } else if (crosschain.getSrcChainType().equals("eth") && crosschain.getDstChainType().equals("h2chain")) {
                crosschain.setResponseHash(hashValues_eth.get(0));
                crosschain.setDstHash(hashValues_h2c.get(0));
            } else if ((crosschain.getSrcChainType().equals("eth") && crosschain.getDstChainType().equals("eth"))) {
                crosschain.setResponseHash(hashValues_eth.get(0));
                crosschain.setDstHash(hashValues_eth.get(1));
            } else if ((crosschain.getSrcChainType().equals("h2chain") && crosschain.getDstChainType().equals("eth"))) {
                crosschain.setResponseHash(hashValues_h2c.get(0));
                crosschain.setDstHash(hashValues_eth.get(0));
            } else if ((crosschain.getSrcChainType().equals("h2chain")
                    && crosschain.getDstChainType().equals("chainmaker"))) {
                crosschain.setResponseHash(hashValues_h2c.get(0));
                crosschain.setDstHash(hashValues_cmk.get(0));
            } else if ((crosschain.getSrcChainType().equals("h2chain")
                    && crosschain.getDstChainType().equals("h2chain"))) {
                crosschain.setResponseHash(hashValues_h2c.get(0));
                crosschain.setDstHash(hashValues_h2c.get(1));
            } else if ((crosschain.getSrcChainType().equals("chainmaker")
                    && crosschain.getDstChainType().equals("eth"))) {
                crosschain.setResponseHash(hashValues_cmk.get(0));
                crosschain.setDstHash(hashValues_eth.get(0));
            } else if ((crosschain.getSrcChainType().equals("chainmaker")
                    && crosschain.getDstChainType().equals("h2chain"))) {
                crosschain.setResponseHash(hashValues_cmk.get(0));
                crosschain.setDstHash(hashValues_h2c.get(0));
            } else if ((crosschain.getSrcChainType().equals("chainmaker")
                    && crosschain.getDstChainType().equals("chainmaker"))) {
                crosschain.setResponseHash(hashValues_cmk.get(0));
                crosschain.setDstHash(hashValues_cmk.get(1));
            }

            // 提取源链响应哈希（获取第一个有效匹配）
            String h2cRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
            Pattern h2cRespRegex = Pattern.compile(h2cRespPattern);
            Matcher h2cRespMatcher = h2cRespRegex.matcher(logs);
            String h2cRespHash = h2cRespMatcher.find() ? h2cRespMatcher.group(1) : "";
            crosschain.setResponseHash(h2cRespHash);
            System.out.println(logs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject resultObj = new JSONObject();
        resultObj.put("txId", crosschain.getTxId());
        resultObj.put("srcIp", crosschain.getSrcIp());
        resultObj.put("srcPort", crosschain.getSrcPort());
        resultObj.put("dstIp", crosschain.getDstIp());
        resultObj.put("dstPort", crosschain.getDstPort());
        resultObj.put("srcHash", crosschain.getSrcHash());
        resultObj.put("dstHash", crosschain.getDstHash());
        resultObj.put("responseHash", crosschain.getResponseHash());
        // crosschain.setSrcHash(chainmakerTxHash);
        // crosschain.setDstHash(h2chainTxHash);
        // crosschain.setResponseHash(ethTxHash);

        // if(crosschain.getSrcChainType().equals("eth")){
        // resultObj.put("srcHash", ethTxHash);
        // resultObj.put("dstHash", chainmakerTxHash);
        // crosschain.setSrcHash(ethTxHash);
        // crosschain.setDstHash(chainmakerTxHash);
        // }else{
        // resultObj.put("srcHash", chainmakerTxHash);
        // resultObj.put("dstHash", ethTxHash);
        // crosschain.setSrcHash(chainmakerTxHash);
        // crosschain.setDstHash(ethTxHash);
        // }
        // 设置响应数据
        responseForF.setRet(ResultCode.SUCCESS);
        responseForF.setData(resultObj);
        try {
            // 尝试插入数据库
            crosschainMapper.insert(crosschain);
        } catch (Exception e) {
            // 数据库操作失败时只记录日志，不影响跨链操作的结果
            log.error("保存跨链记录到数据库失败: " + e.getMessage());
        }
        return responseForF;
    }

    @Override
    public CommonResp startGateways(String srcIp, String srcChainType, String dstIp, String dstChainType,
                                    String relayIp) {
        CommonResp response = new CommonResp();
        JSONObject resultObj = new JSONObject();

        try {
            // 1. 启动中继链网关
            startRelayChain(relayIp, resultObj);

            // 2. 启动源链网关
            startSourceChain(srcIp, srcChainType, dstIp, dstPort(dstChainType), dstChainType, resultObj);

            // 3. 启动目标链网关
            startDestinationChain(dstIp, dstChainType, srcIp, srcPort(srcChainType), srcChainType, resultObj);

            response.setRet(ResultCode.SUCCESS);
            response.setData(resultObj);

        } catch (Exception e) {
            response.setRet(ResultCode.FAILURE);
            response.setMessage("启动网关失败: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 启动中继链网关
     */
    private void startRelayChain(String relayIp, JSONObject resultObj) throws Exception {
        SSHConfig.connect(relayIp); // 使用默认的用户名和密码

        // 确保脚本有执行权限
        String chmodCmd = "chmod +x /root/shell/relay_start.sh";
        SSHConfig.executeCMD(chmodCmd, "UTF-8");

        String startCmd = "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/relay_start.sh > relay.log 2>&1 &";
        String result = SSHConfig.executeCMD(startCmd, "UTF-8");
        resultObj.put("relayStartResult", "中继链网关启动成功");
        resultObj.put("relayStartLog", result);
    }

    /**
     * 启动源链网关
     */
    private void startSourceChain(String srcIp, String srcChainType, String dstIp, int dstPort,
                                  String dstChainType, JSONObject resultObj) throws Exception {
        SSHConfig.connect(srcIp); // 使用默认的用户名和密码

        switch (srcChainType.toLowerCase()) {
            case "ethereum":
                String ethCmd = "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/eth_start.sh > eth.log 2>&1 &";
                String ethResult = SSHConfig.executeCMD(ethCmd, "UTF-8");
                resultObj.put("ethereumStartResult_" + srcIp, "以太坊网关启动成功");
                resultObj.put("ethereumStartLog_" + srcIp, ethResult);
                break;

            case "chainmaker":
                // String chainId = String.valueOf(getChainId("chainmaker", srcIp));
                // todo: for local test
                String cmCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/chainmaker_start1.sh %s %s %d > chainmaker.log 2>&1 &",
                        getChainId(dstChainType, dstIp), dstIp, srcPort(dstChainType));
                String cmResult = SSHConfig.executeCMD(cmCmd, "UTF-8");
                resultObj.put("chainmakerStartResult_" + srcIp, "长安链网关启动成功");
                resultObj.put("chainmakerStartLog_" + srcIp, cmResult);
                break;

            case "h2chain":
                // for test
                String h2cCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/h2chain_start.sh %d > h2chain.log 2>&1 &",
                        getChainId(dstChainType, dstIp));
                // String h2cCmd = String.format("source /etc/profile && source ~/.bashrc && cd
                // /root/shell && nohup /root/shell/h2chain_start.sh %d > h2chain.log 2>&1 &",
                // getChainId(dstChainType, dstIp));
                String h2cResult = SSHConfig.executeCMD(h2cCmd, "UTF-8");
                resultObj.put("h2chainStartResult_" + srcIp, "海河链网关启动成功");
                resultObj.put("h2chainStartLog_" + srcIp, h2cResult);
                break;

            case "fabric":
                // for test
                String fabricCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/fabric_start.sh %d > fabric.log 2>&1 &",
                        getChainId(dstChainType, dstIp));
                // String h2cCmd = String.format("source /etc/profile && source ~/.bashrc && cd
                // /root/shell && nohup /root/shell/h2chain_start.sh %d > h2chain.log 2>&1 &",
                // getChainId(dstChainType, dstIp));
                String fabricResult = SSHConfig.executeCMD(fabricCmd, "UTF-8");
                resultObj.put("fabricStartResult_" + srcIp, "fabric链网关启动成功");
                resultObj.put("fabricStartLog_" + srcIp, fabricResult);
                break;
            case "fisco":
                // for test
                String fiscoCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/fisco_start.sh %d > fisco.log 2>&1 &",
                        getChainId(dstChainType, dstIp));
                // String h2cCmd = String.format("source /etc/profile && source ~/.bashrc && cd
                // /root/shell && nohup /root/shell/h2chain_start.sh %d > h2chain.log 2>&1 &",
                // getChainId(dstChainType, dstIp));
                String fiscoResult = SSHConfig.executeCMD(fiscoCmd, "UTF-8");
                resultObj.put("fiscoStartResult_" + srcIp, "fisco链网关启动成功");
                resultObj.put("fiscoStartLog_" + srcIp, fiscoResult);
                break;

            case "bubi":
                String bubiCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/bubi_start.sh %s %s %s %d source > bubi.log 2>&1 &",
                        getChainId(srcChainType, srcIp), getChainId(dstChainType, dstIp), dstIp, srcPort(dstChainType));

                String bubiResult = SSHConfig.executeCMD(bubiCmd, "UTF-8");
                resultObj.put("bubiStartResult_" + srcIp, "布比链网关启动成功");
                resultObj.put("bubiStartLog_" + srcIp, bubiResult);
                break;

            default:
                throw new IllegalArgumentException("不支持的源链类型: " + srcChainType);
        }
    }

    /**
     * 启动目标链网关
     */
    private void startDestinationChain(String dstIp, String dstChainType, String srcIp, int srcPort,
                                       String srcChainType, JSONObject resultObj) throws Exception {
        SSHConfig.connect(dstIp); // 使用默认的用户名和密码

        switch (dstChainType.toLowerCase()) {
            case "ethereum":
                String ethCmd = "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/eth_start.sh > eth.log 2>&1 &";
                String ethResult = SSHConfig.executeCMD(ethCmd, "UTF-8");
                resultObj.put("ethereumStartResult_" + dstIp, "以太坊网关启动成功");
                resultObj.put("ethereumStartLog_" + dstIp, ethResult);
                break;

            case "chainmaker":
                String chainId = String.valueOf(getChainId("chainmaker", dstIp));
                // todo: for local test
                String cmCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/chainmaker_start1.sh %s %s %d > chainmaker.log 2>&1 &",
                        getChainId(srcChainType, srcIp), srcIp, srcPort(srcChainType));
                String cmResult = SSHConfig.executeCMD(cmCmd, "UTF-8");
                resultObj.put("chainmakerStartResult_" + dstIp, "长安链网关启动成功");
                resultObj.put("chainmakerStartLog_" + dstIp, cmResult);
                break;

            case "h2chain":
                // String h2cCmd = String.format("source /etc/profile && source ~/.bashrc && cd
                // /root/shell && nohup /root/shell/h2chain_start.sh %d > h2chain.log 2>&1 &",
                // getChainId(srcChainType, srcIp));
                // for test
                String h2cCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/h2chain_start.sh %d > h2chain.log 2>&1 &",
                        getChainId(dstChainType, dstIp));
                String h2cResult = SSHConfig.executeCMD(h2cCmd, "UTF-8");
                resultObj.put("h2chainStartResult_" + dstIp, "海河链网关启动成功");
                resultObj.put("h2chainStartLog_" + dstIp, h2cResult);
                break;

            case "fabric":
                // String h2cCmd = String.format("source /etc/profile && source ~/.bashrc && cd
                // /root/shell && nohup /root/shell/h2chain_start.sh %d > h2chain.log 2>&1 &",
                // getChainId(srcChainType, srcIp));
                // for test
                String fabricCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/fabric_start.sh %d > fabric.log 2>&1 &",
                        getChainId(dstChainType, dstIp));
                String fabricResult = SSHConfig.executeCMD(fabricCmd, "UTF-8");
                resultObj.put("fabricStartResult_" + dstIp, "fabric链网关启动成功");
                resultObj.put("fabricStartLog_" + dstIp, fabricResult);
                break;
            case "fisco":
                // String h2cCmd = String.format("source /etc/profile && source ~/.bashrc && cd
                // /root/shell && nohup /root/shell/h2chain_start.sh %d > h2chain.log 2>&1 &",
                // getChainId(srcChainType, srcIp));
                // for test
                String fiscoCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/fisco_start.sh %d > fisco.log 2>&1 &",
                        getChainId(dstChainType, dstIp));
                String fiscoResult = SSHConfig.executeCMD(fiscoCmd, "UTF-8");
                resultObj.put("fiscoStartResult_" + dstIp, "fisco链网关启动成功");
                resultObj.put("fiscoStartLog_" + dstIp, fiscoResult);
                break;

            case "bubi":
                // test
                String bubiCmd = String.format(
                        "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup /root/shell/bubi_start.sh %s %s %s %d target > bubi.log 2>&1 &",
                        getChainId(srcChainType, srcIp), getChainId(dstChainType, dstIp), srcIp, srcPort(srcChainType));
                // String bubiCmd = String.format(
                // "source /etc/profile && source ~/.bashrc && cd /root/shell && nohup
                // /root/shell/bubi_start.sh %s %s %s %d > bubi.log 2>&1 &",
                // 14002, 11002, "192.168.0.2", 8088);
                String bubiResult = SSHConfig.executeCMD(bubiCmd, "UTF-8");
                resultObj.put("bubiStartResult_" + dstIp, "布比链网关启动成功");
                resultObj.put("bubiStartLog_" + dstIp, bubiResult);
                break;

            default:
                throw new IllegalArgumentException("不支持的目标链类型: " + dstChainType);
        }
    }

    /**
     * 获取链的默认端口
     */
    private int srcPort(String chainType) {
        switch (chainType.toLowerCase()) {
            case "ethereum":
                return 8086;
            case "chainmaker":
                return 8088;
            case "h2chain":
                return 8087;
            case "bubi":
                return 8089;
            case "fabric":
                return 8090;
            case "fisco":
                return 8091;
            default:
                throw new IllegalArgumentException("不支持的链类型: " + chainType);
        }
    }

    /**
     * 获取链的默认端口
     */
    private int dstPort(String chainType) {
        return srcPort(chainType);
    }

    /**
     * 计算链ID
     */
    private int getChainId(String chainType, String ip) {
        // 从IP地址中提取最后一个数字
        String[] parts = ip.split("\\.");
        int lastNumber = Integer.parseInt(parts[3]);

        // 根据链类型计算chainId
        switch (chainType.toLowerCase()) {
            case "ethereum":
                return 12000 + lastNumber;
            case "chainmaker":
                return 11000 + lastNumber;
            case "h2chain":
                return 13000 + lastNumber;
            case "bubi":
                return 14000 + lastNumber;
            case "fabric":
                return 15000 + lastNumber;
            case "fisco":
                return 16000 + lastNumber;
            default:
                throw new IllegalArgumentException("不支持的链类型: " + chainType);
        }
    }

    /**
     * 执行跨链操作
     *
     * @param srcIp        源链IP
     * @param srcChainType 源链类型
     * @param dstIp        目标链IP
     * @param dstChainType 目标链类型
     * @return 跨链操作结果
     */
    @Override
    public CommonResp executeCrossChain(String srcIp, String srcChainType, String dstIp, String dstChainType,
                                        String srcappId, String dstappId, String appArgs) {
        CommonResp response = new CommonResp();
        JSONObject resultObj = new JSONObject();
        if (srcappId == null || srcappId.isEmpty()) {
            srcappId = "";
        }
        if (dstappId == null || dstappId.isEmpty()) {
            dstappId = "";
        }
        if (appArgs == null || appArgs.isEmpty()) {
            appArgs = "";
        }

        try {
            SSHConfig.connect(srcIp);

            // 根据链类型执行不同的跨链命令

            // 计算源链和目标链的chainId
            String getIpCmd = "ip -4 addr show eth0 | grep -oP '(?<=inet\\s)\\d+\\.\\d+\\.\\d+\\.\\d+' | cut -d. -f4";
            String ipLastPart = SSHConfig.executeCMD(getIpCmd, "UTF-8").trim();
            int srcChainId = 12000 + Integer.parseInt(ipLastPart);

            // 目标链ID计算
            String[] dstIpParts = dstIp.split("\\.");
            int dstChainId = 12000;
            if (dstChainType.equalsIgnoreCase("ethereum")) {
                dstChainId = 12000 + Integer.parseInt(dstIpParts[3]);
            } else if (dstChainType.equalsIgnoreCase("chainmaker")) {
                dstChainId = 11000 + Integer.parseInt(dstIpParts[3]);
            } else if (dstChainType.equalsIgnoreCase("h2chain")) {
                dstChainId = 13000 + Integer.parseInt(dstIpParts[3]);
            } else if (dstChainType.equalsIgnoreCase("bubi")) {
                dstChainId = 14000 + Integer.parseInt(dstIpParts[3]);
            } else if (dstChainType.equalsIgnoreCase("fabric")) {
                dstChainId = 15000 + Integer.parseInt(dstIpParts[3]);
            } else if (dstChainType.equalsIgnoreCase("fisco")) {
                dstChainId = 16000 + Integer.parseInt(dstIpParts[3]);
            }
            switch (srcChainType.toLowerCase()) {
                case "ethereum":
                    // 执行以太坊跨链命令
                    String ethAppArgs = appArgs.replace("\"", "\\\"");
                    String ethCmd = String.format(
                            "source /etc/profile && source ~/.bashrc && cd ~/CIPS-Gemini-v1/CIPS-Gemini-Ethereum && ./helper.sh SendCCMsg ws://127.0.0.1:10026 contract_addresses_%d.toml %d "
                                    + srcappId + " " + dstappId + " " + ethAppArgs,
                            srcChainId, getChainId(dstChainType, dstIp));
                    String ethResult = SSHConfig.executeCMD(ethCmd, "UTF-8");

                    // 打印命令输出用于调试
                    System.out.println("命令完整输出：");
                    System.out.println(ethResult);

                    // 从命令输出中提取源链请求哈希
                    String ethReqPattern = "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\s+\\[DEBG\\]:\\s+Send out on source chain succeed @ (0x[a-fA-F0-9]+)";
                    Pattern ethReqRegex = Pattern.compile(ethReqPattern);
                    Matcher ethReqMatcher = ethReqRegex.matcher(ethResult);

                    // 打印匹配结果用于调试
                    System.out.println("正在尝试匹配哈希值...");
                    String ethReqHash = "";
                    if (ethReqMatcher.find()) {
                        ethReqHash = ethReqMatcher.group(1);
                        System.out.println("成功提取到哈希值: " + ethReqHash);
                    } else {
                        System.out.println("未能匹配到哈希值，可能的原因：");
                        System.out.println("1. 输出格式不匹配");
                        System.out.println("2. 正则表达式不正确");
                        System.out.println("使用的正则表达式: " + ethReqPattern);
                        System.out.println("实际输出内容：");
                        System.out.println(ethResult);
                    }

                    // 等待50秒，确保日志已经生成
                    if (dstChainType.equalsIgnoreCase("bubi")) {
                        Thread.sleep(70000);
                    } else {
                        Thread.sleep(50000);
                    }

                    // 读取以太坊日志文件
                    String ethFromCmLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Ethereum/logs/eth.log";
                    String ethFromCmLogs = SSHConfig.executeCMD(ethFromCmLogCmd, "UTF-8");

                    // 连接目标链服务器
                    SSHConfig.connect(dstIp);

                    if (dstChainType.equalsIgnoreCase("h2chain")) {
                        // 读取海河链日志文件
                        String h2cFromEthLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-H2Chain/logs/h2chain.log";
                        String h2cFromEthLogs = SSHConfig.executeCMD(h2cFromEthLogCmd, "UTF-8");

                        // 提取源链响应哈希
                        String ethRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern ethRespRegex = Pattern.compile(ethRespPattern);
                        Matcher ethRespMatcher = ethRespRegex.matcher(ethFromCmLogs);
                        String ethRespHash = "";
                        while (ethRespMatcher.find()) {
                            ethRespHash = ethRespMatcher.group(1);
                        }

                        // 提取目标链哈希
                        String ethToH2cDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern ethToH2cDstRegex = Pattern.compile(ethToH2cDstPattern);
                        Matcher ethToH2cDstMatcher = ethToH2cDstRegex.matcher(h2cFromEthLogs);
                        String ethToH2cDstHash = "";
                        while (ethToH2cDstMatcher.find()) {
                            ethToH2cDstHash = ethToH2cDstMatcher.group(1);
                        }

                        resultObj.put("dstHash", ethToH2cDstHash);
                        resultObj.put("srcRespHash", ethRespHash);
                        resultObj.put("srcReqHash", ethReqHash);
                        resultObj.put("crossChainResult", "以太坊跨海河链操作执行成功");

                    } else if (dstChainType.equalsIgnoreCase("fabric")) {
                        // 读取海河链日志文件
                        String fabricFromEthLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fabric/logs/fabric.log";
                        String fabricFromEthLogs = SSHConfig.executeCMD(fabricFromEthLogCmd, "UTF-8");

                        // 提取源链响应哈希
                        String ethRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern ethRespRegex = Pattern.compile(ethRespPattern);
                        Matcher ethRespMatcher = ethRespRegex.matcher(ethFromCmLogs);
                        String ethRespHash = "";
                        while (ethRespMatcher.find()) {
                            ethRespHash = ethRespMatcher.group(1);
                        }

                        // 提取目标链哈希
                        String ethTofabricDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern ethTofabricDstRegex = Pattern.compile(ethTofabricDstPattern);
                        Matcher ethTofabricDstMatcher = ethTofabricDstRegex.matcher(fabricFromEthLogs);
                        String ethTofabricDstHash = "";
                        while (ethTofabricDstMatcher.find()) {
                            ethTofabricDstHash = ethTofabricDstMatcher.group(1);
                        }

                        resultObj.put("dstHash", ethTofabricDstHash);
                        resultObj.put("srcRespHash", ethRespHash);
                        resultObj.put("srcReqHash", ethReqHash);
                        resultObj.put("crossChainResult", "以太坊跨fabric操作执行成功");
                    } else if (dstChainType.equalsIgnoreCase("fisco")) {
                        // 读取海河链日志文件
                        String fiscoFromEthLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fisco/logs/fisco.log";
                        String fiscoFromEthLogs = SSHConfig.executeCMD(fiscoFromEthLogCmd, "UTF-8");

                        // 提取源链响应哈希
                        String ethRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern ethRespRegex = Pattern.compile(ethRespPattern);
                        Matcher ethRespMatcher = ethRespRegex.matcher(ethFromCmLogs);
                        String ethRespHash = "";
                        while (ethRespMatcher.find()) {
                            ethRespHash = ethRespMatcher.group(1);
                        }

                        // 提取目标链哈希
                        String ethTofiscoDstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern ethTofiscoDstRegex = Pattern.compile(ethTofiscoDstPattern);
                        Matcher ethTofiscoDstMatcher = ethTofiscoDstRegex.matcher(fiscoFromEthLogs);
                        String ethTofiscoDstHash = "";
                        while (ethTofiscoDstMatcher.find()) {
                            ethTofiscoDstHash = ethTofiscoDstMatcher.group(1);
                        }

                        resultObj.put("dstHash", ethTofiscoDstHash);
                        resultObj.put("srcRespHash", ethRespHash);
                        resultObj.put("srcReqHash", ethReqHash);
                        resultObj.put("crossChainResult", "以太坊跨fisco操作执行成功");
                    } else if (dstChainType.equalsIgnoreCase("chainmaker")) {
                        // 读取长安链日志文件
                        String cmFromEthLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker/logs/chainmaker.log";
                        String cmFromEthLogs = SSHConfig.executeCMD(cmFromEthLogCmd, "UTF-8");

                        // 提取源链响应哈希
                        String ethRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern ethRespRegex = Pattern.compile(ethRespPattern);
                        Matcher ethRespMatcher = ethRespRegex.matcher(ethFromCmLogs);
                        String ethRespHash = "";
                        while (ethRespMatcher.find()) {
                            ethRespHash = ethRespMatcher.group(1);
                        }

                        // 提取目标链哈希
                        String ethToCmDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern ethToCmDstRegex = Pattern.compile(ethToCmDstPattern);
                        Matcher ethToCmDstMatcher = ethToCmDstRegex.matcher(cmFromEthLogs);
                        String ethToCmDstHash = "";
                        while (ethToCmDstMatcher.find()) {
                            ethToCmDstHash = ethToCmDstMatcher.group(1);
                        }

                        resultObj.put("dstHash", ethToCmDstHash);
                        resultObj.put("srcRespHash", ethRespHash);
                        resultObj.put("srcReqHash", ethReqHash);
                        resultObj.put("crossChainResult", "以太坊跨长安链操作执行成功");
                    } else if (dstChainType.equalsIgnoreCase("bubi")) {
                        // 读取布比链日志文件
                        String bubiLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Bubi/logs/bubi.log";
                        String bubiLogs = SSHConfig.executeCMD(bubiLogCmd, "UTF-8");

                        // 提取源链响应哈希
                        String ethRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern ethRespRegex = Pattern.compile(ethRespPattern);
                        Matcher ethRespMatcher = ethRespRegex.matcher(ethFromCmLogs);
                        String ethRespHash = "";
                        while (ethRespMatcher.find()) {
                            ethRespHash = ethRespMatcher.group(1);
                        }

                        // 添加调试信息
                        System.out.println("正在尝试匹配响应哈希...");
                        System.out.println("使用的正则表达式: " + ethRespPattern);
                        System.out.println("日志内容：");
                        System.out.println(bubiLogs);

                        while (ethRespMatcher.find()) {
                            ethRespHash = ethRespMatcher.group(1);
                            System.out.println("成功提取到响应哈希: " + ethRespHash);
                        }

                        if (ethRespHash.isEmpty()) {
                            System.out.println("未能匹配到响应哈希");
                        }

                        // 提取目标链哈希
                        String ethToBubiDstPattern = "contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern ethToBubiDstRegex = Pattern.compile(ethToBubiDstPattern);
                        Matcher ethToBubiDstMatcher = ethToBubiDstRegex.matcher(bubiLogs);
                        String ethToBubiDstHash = "";
                        while (ethToBubiDstMatcher.find()) {
                            ethToBubiDstHash = ethToBubiDstMatcher.group(1);
                        }

                        resultObj.put("dstHash", ethToBubiDstHash);
                        resultObj.put("srcRespHash", ethRespHash);
                        resultObj.put("srcReqHash", ethReqHash);
                        resultObj.put("crossChainResult", "以太坊跨布比链操作执行成功");
                    }
                    break;

                case "h2chain":
                    // 执行海河链跨链命令
                    String h2cCmd = "source /etc/profile && source ~/.bashrc && cd /root/CIPS-Gemini-v1/CIPS-Gemini-H2Chain && "
                            + "./crossH2C test " + srcappId + " " + dstappId + " " + appArgs;
                    String h2cResult = SSHConfig.executeCMD(h2cCmd, "UTF-8");

                    // 等待35秒，确保日志已经生成
                    Thread.sleep(35000);

                    // 读取海河链日志文件
                    String h2cSrcLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-H2Chain/logs/h2chain.log";
                    String h2cSrcLogs = SSHConfig.executeCMD(h2cSrcLogCmd, "UTF-8");

                    // 连接目标链服务器
                    SSHConfig.connect(dstIp);

                    String srcReqHash = "";
                    String srcRespHash = "";
                    String dstHash = "";

                    if (dstChainType.equalsIgnoreCase("ethereum")) {
                        // 读取以太坊日志文件
                        String ethLogCmdH2c = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Ethereum/logs/eth.log";
                        String ethLogsH2c = SSHConfig.executeCMD(ethLogCmdH2c, "UTF-8");

                        // 提取源链请求哈希
                        String h2cReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern h2cReqRegex = Pattern.compile(h2cReqPattern);
                        Matcher h2cReqMatcher = h2cReqRegex.matcher(h2cSrcLogs);
                        srcReqHash = h2cReqMatcher.find() ? h2cReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String h2cRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern h2cRespRegex = Pattern.compile(h2cRespPattern);
                        Matcher h2cRespMatcher = h2cRespRegex.matcher(h2cSrcLogs);
                        srcRespHash = h2cRespMatcher.find() ? h2cRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String h2cDstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern h2cDstRegex = Pattern.compile(h2cDstPattern);
                        Matcher h2cDstMatcher = h2cDstRegex.matcher(ethLogsH2c);
                        dstHash = h2cDstMatcher.find() ? h2cDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "海河链跨以太坊操作执行成功");

                    } else if (dstChainType.equalsIgnoreCase("chainmaker")) {
                        // 读取长安链日志文件
                        String cmFromEthLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker/logs/chainmaker.log";
                        String cmFromEthLogs = SSHConfig.executeCMD(cmFromEthLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String h2cReqPattern = "\\[DEBG\\]:\\s+Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern h2cReqRegex = Pattern.compile(h2cReqPattern);
                        Matcher h2cReqMatcher = h2cReqRegex.matcher(h2cSrcLogs);
                        srcReqHash = h2cReqMatcher.find() ? h2cReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String h2cRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern h2cRespRegex = Pattern.compile(h2cRespPattern);
                        Matcher h2cRespMatcher = h2cRespRegex.matcher(h2cSrcLogs);
                        srcRespHash = h2cRespMatcher.find() ? h2cRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String h2cDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern h2cDstRegex = Pattern.compile(h2cDstPattern);
                        Matcher h2cDstMatcher = h2cDstRegex.matcher(cmFromEthLogs);
                        dstHash = h2cDstMatcher.find() ? h2cDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "海河链跨长安链操作执行成功");
                    }else if(dstChainType.equalsIgnoreCase("fabric")) {
                        // 读取fabric日志文件
                        String fabricLogCmdH2c = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fabric/logs/fabric.log";
                        String fabricLogsH2c = SSHConfig.executeCMD(fabricLogCmdH2c, "UTF-8");

                        // 提取源链请求哈希
                        String h2cReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern h2cReqRegex = Pattern.compile(h2cReqPattern);
                        Matcher h2cReqMatcher = h2cReqRegex.matcher(h2cSrcLogs);
                        srcReqHash = h2cReqMatcher.find() ? h2cReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String h2cRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern h2cRespRegex = Pattern.compile(h2cRespPattern);
                        Matcher h2cRespMatcher = h2cRespRegex.matcher(h2cSrcLogs);
                        srcRespHash = h2cRespMatcher.find() ? h2cRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String h2cDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern h2cDstRegex = Pattern.compile(h2cDstPattern);
                        Matcher h2cDstMatcher = h2cDstRegex.matcher(fabricLogsH2c);
                        dstHash = h2cDstMatcher.find() ? h2cDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "海河链跨fabric操作执行成功");


                    }
                    else if(dstChainType.equalsIgnoreCase("fisco")) {
                        // 读取fisco日志文件
                        String fiscoLogCmdH2c = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fisco/logs/fisco.log";
                        String fiscoLogsH2c = SSHConfig.executeCMD(fiscoLogCmdH2c, "UTF-8");

                        // 提取源链请求哈希
                        String h2cReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern h2cReqRegex = Pattern.compile(h2cReqPattern);
                        Matcher h2cReqMatcher = h2cReqRegex.matcher(h2cSrcLogs);
                        srcReqHash = h2cReqMatcher.find() ? h2cReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String h2cRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern h2cRespRegex = Pattern.compile(h2cRespPattern);
                        Matcher h2cRespMatcher = h2cRespRegex.matcher(h2cSrcLogs);
                        srcRespHash = h2cRespMatcher.find() ? h2cRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String h2cDstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern h2cDstRegex = Pattern.compile(h2cDstPattern);
                        Matcher h2cDstMatcher = h2cDstRegex.matcher(fiscoLogsH2c);
                        dstHash = h2cDstMatcher.find() ? h2cDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "海河链跨fisco操作执行成功");


                    }
                    else if (dstChainType.equalsIgnoreCase("bubi")) {
                        // 读取布比链日志文件
                        String bubiLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Bubi/logs/bubi.log";
                        String bubiLogs = SSHConfig.executeCMD(bubiLogCmd, "UTF-8");

                        // 提取源链请求哈希（从bubiResult中提取）
                        String h2cReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern h2cReqRegex = Pattern.compile(h2cReqPattern);
                        Matcher h2cReqMatcher = h2cReqRegex.matcher(h2cSrcLogs);
                        srcReqHash = h2cReqMatcher.find() ? h2cReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String h2cRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern h2cRespRegex = Pattern.compile(h2cRespPattern);
                        Matcher h2cRespMatcher = h2cRespRegex.matcher(h2cSrcLogs);
                        srcRespHash = h2cRespMatcher.find() ? h2cRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String h2cToBubiDstPattern = "contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern h2cToBubiDstRegex = Pattern.compile(h2cToBubiDstPattern);
                        Matcher h2cToBubiDstMatcher = h2cToBubiDstRegex.matcher(bubiLogs);
                        String h2cToBubiDstHash = "";
                        while (h2cToBubiDstMatcher.find()) {
                            h2cToBubiDstHash = h2cToBubiDstMatcher.group(1);
                        }

                        resultObj.put("dstHash", h2cToBubiDstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "海河链跨布比链操作执行成功");
                    }
                    break;
                case "fisco":
                    // 执行fisco跨链命令
                    String appArgsfisco = appArgs.replaceAll("\"", "\\\\\\\""); // 转义双引号
                    String fiscoCmd = "source /etc/profile && source ~/.bashrc && cd /root/CIPS-Gemini-v1/CIPS-Gemini-Fisco && "
                            + "./crossFab test " + dstChainId + " " + srcappId + " " + dstappId + " " + appArgsfisco;
                    String fiscoResult = SSHConfig.executeCMD(fiscoCmd, "UTF-8");

                    // 等待35秒，确保日志已经生成
                    Thread.sleep(35000);

                    // 读取fisco日志文件
                    String fiscoSrcLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fisco/logs/fisco.log";
                    String fiscoSrcLogs = SSHConfig.executeCMD(fiscoSrcLogCmd, "UTF-8");

                    // 连接目标链服务器
                    SSHConfig.connect(dstIp);

                    srcReqHash = "";
                    srcRespHash = "";
                    dstHash = "";

                    if (dstChainType.equalsIgnoreCase("ethereum")) {
                        // 读取以太坊日志文件
                        String ethLogCmdfisco = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Ethereum/logs/eth.log";
                        String ethLogsfisco = SSHConfig.executeCMD(ethLogCmdfisco, "UTF-8");

                        // 提取源链请求哈希
                        String fiscoReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fiscoReqRegex = Pattern.compile(fiscoReqPattern);
                        Matcher fiscoReqMatcher = fiscoReqRegex.matcher(fiscoSrcLogs);
                        srcReqHash = fiscoReqMatcher.find() ? fiscoReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fiscoRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern fiscoRespRegex = Pattern.compile(fiscoRespPattern);
                        Matcher fiscoRespMatcher = fiscoRespRegex.matcher(fiscoSrcLogs);
                        srcRespHash = fiscoRespMatcher.find() ? fiscoRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fiscoDstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern fiscoDstRegex = Pattern.compile(fiscoDstPattern);
                        Matcher fiscoDstMatcher = fiscoDstRegex.matcher(ethLogsfisco);
                        dstHash = fiscoDstMatcher.find() ? fiscoDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fisco跨以太坊操作执行成功");

                    } else if (dstChainType.equalsIgnoreCase("chainmaker")) {
                        // 读取长安链日志文件
                        String cmFromfiscoLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker/logs/chainmaker.log";
                        String cmFromfiscoLogs = SSHConfig.executeCMD(cmFromfiscoLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String fiscoReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fiscoReqRegex = Pattern.compile(fiscoReqPattern);
                        Matcher fiscoReqMatcher = fiscoReqRegex.matcher(fiscoSrcLogs);
                        srcReqHash = fiscoReqMatcher.find() ? fiscoReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fiscoRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern fiscoRespRegex = Pattern.compile(fiscoRespPattern);
                        Matcher fiscoRespMatcher = fiscoRespRegex.matcher(fiscoSrcLogs);
                        srcRespHash = fiscoRespMatcher.find() ? fiscoRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fiscoDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern fiscoDstRegex = Pattern.compile(fiscoDstPattern);
                        Matcher fiscoDstMatcher = fiscoDstRegex.matcher(cmFromfiscoLogs);
                        dstHash = fiscoDstMatcher.find() ? fiscoDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fisco跨长安链操作执行成功");
                    }else if(dstChainType.equalsIgnoreCase("fabric")) {
                        // 读取fabric日志文件
                        String fabricLogCmdfisco = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fabric/logs/fabric.log";
                        String fabricLogsfisco = SSHConfig.executeCMD(fabricLogCmdfisco, "UTF-8");

                        // 提取源链请求哈希
                        String fiscoReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fiscoReqRegex = Pattern.compile(fiscoReqPattern);
                        Matcher fiscoReqMatcher = fiscoReqRegex.matcher(fiscoSrcLogs);
                        srcReqHash = fiscoReqMatcher.find() ? fiscoReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fiscoRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern fiscoRespRegex = Pattern.compile(fiscoRespPattern);
                        Matcher fiscoRespMatcher = fiscoRespRegex.matcher(fiscoSrcLogs);
                        srcRespHash = fiscoRespMatcher.find() ? fiscoRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fiscoDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern fiscoDstRegex = Pattern.compile(fiscoDstPattern);
                        Matcher fiscoDstMatcher = fiscoDstRegex.matcher(fabricLogsfisco);
                        dstHash = fiscoDstMatcher.find() ? fiscoDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fisco跨fabric操作执行成功");


                    }
                     else if (dstChainType.equalsIgnoreCase("h2chain")) {
                        // 读取海河智链日志文件
                        String h2chainFromfiscoLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-H2Chain/logs/h2chain.log";
                        String h2chainFromfiscoLogs = SSHConfig.executeCMD(h2chainFromfiscoLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String fiscoReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fiscoReqRegex = Pattern.compile(fiscoReqPattern);
                        Matcher fiscoReqMatcher = fiscoReqRegex.matcher(fiscoSrcLogs);
                        srcReqHash = fiscoReqMatcher.find() ? fiscoReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fiscoRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern fiscoRespRegex = Pattern.compile(fiscoRespPattern);
                        Matcher fiscoRespMatcher = fiscoRespRegex.matcher(fiscoSrcLogs);
                        srcRespHash = fiscoRespMatcher.find() ? fiscoRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fiscoDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern fiscoDstRegex = Pattern.compile(fiscoDstPattern);
                        Matcher fiscoDstMatcher = fiscoDstRegex.matcher(h2chainFromfiscoLogs);
                        dstHash = fiscoDstMatcher.find() ? fiscoDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fisco跨海河链操作执行成功");
                    }
                    else if (dstChainType.equalsIgnoreCase("bubi")) {
                        // 读取布比链日志文件
                        String bubiLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Bubi/logs/bubi.log";
                        String bubiLogs = SSHConfig.executeCMD(bubiLogCmd, "UTF-8");

                        // 提取源链请求哈希（从bubiResult中提取）
                        String fiscoReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fiscoReqRegex = Pattern.compile(fiscoReqPattern);
                        Matcher fiscoReqMatcher = fiscoReqRegex.matcher(fiscoSrcLogs);
                        srcReqHash = fiscoReqMatcher.find() ? fiscoReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fiscoRespPattern = "get resp txhash: (0x[a-fA-F0-9]+)";
                        Pattern fiscoRespRegex = Pattern.compile(fiscoRespPattern);
                        Matcher fiscoRespMatcher = fiscoRespRegex.matcher(fiscoSrcLogs);
                        srcRespHash = fiscoRespMatcher.find() ? fiscoRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fiscoToBubiDstPattern = "contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern fiscoToBubiDstRegex = Pattern.compile(fiscoToBubiDstPattern);
                        Matcher fiscoToBubiDstMatcher = fiscoToBubiDstRegex.matcher(bubiLogs);
                        String fiscoToBubiDstHash = "";
                        while (fiscoToBubiDstMatcher.find()) {
                            fiscoToBubiDstHash = fiscoToBubiDstMatcher.group(1);
                        }

                        resultObj.put("dstHash", fiscoToBubiDstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fisco跨布比链操作执行成功");
                    }
                    break;
                case "fabric":
                    // 执行fabric链跨链命令
                    String appArgsFabric = appArgs.replaceAll("\"", "\\\\\""); // 转义双引号
                    String fabricCmd = "source /etc/profile && source ~/.bashrc && cd /root/CIPS-Gemini-v1/CIPS-Gemini-Fabric && "
                            + "./crossFab test " + dstChainId + " " + srcappId + " " + dstappId + " " + appArgsFabric;
                    String fabricResult = SSHConfig.executeCMD(fabricCmd, "UTF-8");

                    // 等待35秒，确保日志已经生成
                    Thread.sleep(35000);

                    // 读取海河链日志文件
                    String fabricSrcLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fabric/logs/fabric.log";
                    String fabricSrcLogs = SSHConfig.executeCMD(fabricSrcLogCmd, "UTF-8");

                    // 连接目标链服务器
                    SSHConfig.connect(dstIp);

                    srcReqHash = "";
                    srcRespHash = "";
                    dstHash = "";
                    // 读取以太坊日志文件
                    if (dstChainType.equalsIgnoreCase("ethereum")) {
                        // 读取以太坊日志文件
                        String fabrictoethLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Ethereum/logs/eth.log";
                        String fabrictoethlog = SSHConfig.executeCMD(fabrictoethLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String fabricReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fabricReqRegex = Pattern.compile(fabricReqPattern);
                        Matcher fabricReqMatcher = fabricReqRegex.matcher(fabricSrcLogs);
                        srcReqHash = fabricReqMatcher.find() ? fabricReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fabricRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern fabricRespRegex = Pattern.compile(fabricRespPattern);
                        Matcher fabricRespMatcher = fabricRespRegex.matcher(fabricSrcLogs);
                        srcRespHash = fabricRespMatcher.find() ? fabricRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fabrictoethDstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern fabrictoethDstRegex = Pattern.compile(fabrictoethDstPattern);
                        Matcher fabrictoethDstMatcher = fabrictoethDstRegex.matcher(fabrictoethlog);
                        dstHash = fabrictoethDstMatcher.find() ? fabrictoethDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fabric链跨以太坊操作执行成功");

                    } else if (dstChainType.equalsIgnoreCase("chainmaker")) {
                        // 读取以太坊日志文件
                        String fabrictochainmakerLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker/logs/chainmaker.log";
                        String fabrictochainmakerlog = SSHConfig.executeCMD(fabrictochainmakerLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String fabricReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fabricReqRegex = Pattern.compile(fabricReqPattern);
                        Matcher fabricReqMatcher = fabricReqRegex.matcher(fabricSrcLogs);
                        srcReqHash = fabricReqMatcher.find() ? fabricReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fabricRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern fabricRespRegex = Pattern.compile(fabricRespPattern);
                        Matcher fabricRespMatcher = fabricRespRegex.matcher(fabricSrcLogs);
                        srcRespHash = fabricRespMatcher.find() ? fabricRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fabrictoh2cDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern fabrictoh2cDstRegex = Pattern.compile(fabrictoh2cDstPattern);
                        Matcher fabrictoh2cDstMatcher = fabrictoh2cDstRegex.matcher(fabrictochainmakerlog);
                        dstHash = fabrictoh2cDstMatcher.find() ? fabrictoh2cDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fabric链跨长安链操作执行成功");

                    } else if (dstChainType.equalsIgnoreCase("h2chain")) {
                        // 读取以太坊日志文件
                        String fabrictoh2cLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-H2Chain/logs/h2chain.log";
                        String fabrictoh2clog = SSHConfig.executeCMD(fabrictoh2cLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String fabricReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fabricReqRegex = Pattern.compile(fabricReqPattern);
                        Matcher fabricReqMatcher = fabricReqRegex.matcher(fabricSrcLogs);
                        srcReqHash = fabricReqMatcher.find() ? fabricReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fabricRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern fabricRespRegex = Pattern.compile(fabricRespPattern);
                        Matcher fabricRespMatcher = fabricRespRegex.matcher(fabricSrcLogs);
                        srcRespHash = fabricRespMatcher.find() ? fabricRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fabrictoh2cDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern fabrictoh2cDstRegex = Pattern.compile(fabrictoh2cDstPattern);
                        Matcher fabrictoh2cDstMatcher = fabrictoh2cDstRegex.matcher(fabrictoh2clog);
                        dstHash = fabrictoh2cDstMatcher.find() ? fabrictoh2cDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fabric链跨海河链操作执行成功");

                    }else if (dstChainType.equalsIgnoreCase("fisco")) {
                        // 读取fisco日志文件
                        String fabrictofiscoLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fisco/logs/fisco.log";
                        String fabrictofiscolog = SSHConfig.executeCMD(fabrictofiscoLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String fabricReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fabricReqRegex = Pattern.compile(fabricReqPattern);
                        Matcher fabricReqMatcher = fabricReqRegex.matcher(fabricSrcLogs);
                        srcReqHash = fabricReqMatcher.find() ? fabricReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fabricRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern fabricRespRegex = Pattern.compile(fabricRespPattern);
                        Matcher fabricRespMatcher = fabricRespRegex.matcher(fabricSrcLogs);
                        srcRespHash = fabricRespMatcher.find() ? fabricRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fabrictofiscoDstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern fabrictofiscoDstRegex = Pattern.compile(fabrictofiscoDstPattern);
                        Matcher fabrictofiscoDstMatcher = fabrictofiscoDstRegex.matcher(fabrictofiscolog);
                        dstHash = fabrictofiscoDstMatcher.find() ? fabrictofiscoDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fabric链跨fisco链操作执行成功");

                    }
                    else if (dstChainType.equalsIgnoreCase("bubi")) {
                        // 读取以太坊日志文件
                        String fabrictobubiLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Bubi/logs/bubi.log";
                        String fabrictobubilog = SSHConfig.executeCMD(fabrictobubiLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String fabricReqPattern = "Obtained request cmhash on the source chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern fabricReqRegex = Pattern.compile(fabricReqPattern);
                        Matcher fabricReqMatcher = fabricReqRegex.matcher(fabricSrcLogs);
                        srcReqHash = fabricReqMatcher.find() ? fabricReqMatcher.group(1) : "";

                        // 提取源链响应哈希
                        String fabricRespPattern = "\\[DEBG\\]:\\s+get resp txhash: ([a-fA-F0-9]+)";
                        Pattern fabricRespRegex = Pattern.compile(fabricRespPattern);
                        Matcher fabricRespMatcher = fabricRespRegex.matcher(fabricSrcLogs);
                        srcRespHash = fabricRespMatcher.find() ? fabricRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String fabrictobubiDstPattern = "contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern fabrictobubiDstRegex = Pattern.compile(fabrictobubiDstPattern);
                        Matcher fabrictobubiDstMatcher = fabrictobubiDstRegex.matcher(fabrictobubilog);
                        dstHash = fabrictobubiDstMatcher.find() ? fabrictobubiDstMatcher.group(1) : "";

                        resultObj.put("dstHash", dstHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("crossChainResult", "fabric链跨bubi链操作执行成功");

                    }
                    // resultObj.put("dstHash", dstHash);
                    // resultObj.put("srcRespHash", srcRespHash);
                    // resultObj.put("srcReqHash", srcReqHash);
                    // resultObj.put("crossChainResult", "fabric链跨链操作执行成功");
                    break;
                case "chainmaker":
                    // 执行长安链跨链命令
                    String chainmakerAppArgs = appArgs.replaceAll("\",", "\", "); // 处理参数中的逗号分隔
                    String cmCmd = "source /etc/profile && source ~/.bashrc && cd /root/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker && go run main.go send 1 "
                            + chainmakerAppArgs;
                    String cmResult = SSHConfig.executeCMD(cmCmd, "UTF-8");

                    // 等待5秒，确保日志已经生成
                    if (dstChainType.equalsIgnoreCase("bubi")) {
                        Thread.sleep(40000);
                    } else {
                        Thread.sleep(10000);
                    }

                    // 读取长安链日志文件
                    String cmFromEthLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker/logs/chainmaker.log";
                    String cmFromEthLogs = SSHConfig.executeCMD(cmFromEthLogCmd, "UTF-8");

                    // 连接目标链服务器
                    SSHConfig.connect(dstIp);

                    srcReqHash = "";
                    srcRespHash = "";
                    dstHash = "";

                    if (dstChainType.equalsIgnoreCase("ethereum")) {
                        // 读取以太坊日志文件
                        String ethLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Ethereum/logs/eth.log";
                        String ethLogs = SSHConfig.executeCMD(ethLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String srcReqPattern = "Obtained request cmhash on the source chain\\([0-9]+\\): ([a-fA-F0-9]+)";
                        Pattern srcReqRegex = Pattern.compile(srcReqPattern);
                        Matcher srcReqMatcher = srcReqRegex.matcher(cmFromEthLogs);
                        srcReqHash = srcReqMatcher.find() ? srcReqMatcher.group(1) : "";

                        // 提取源链响应哈希（获取第一个有效匹配）
                        String srcRespPattern = "get resp txhash: ([a-fA-F0-9]+)";
                        Pattern srcRespRegex = Pattern.compile(srcRespPattern);
                        Matcher srcRespMatcher = srcRespRegex.matcher(cmFromEthLogs);
                        srcRespHash = srcRespMatcher.find() ? srcRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String dstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern dstRegex = Pattern.compile(dstPattern);
                        Matcher dstMatcher = dstRegex.matcher(ethLogs);
                        dstHash = dstMatcher.find() ? dstMatcher.group(1) : "";

                    } else if (dstChainType.equalsIgnoreCase("h2chain")) {
                        // 读取海河链日志文件
                        String h2cLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-H2Chain/logs/h2chain.log";
                        String h2cLogs = SSHConfig.executeCMD(h2cLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String srcReqPattern = "Obtained request cmhash on the source chain\\([0-9]+\\): ([a-fA-F0-9]+)";
                        Pattern srcReqRegex = Pattern.compile(srcReqPattern);
                        Matcher srcReqMatcher = srcReqRegex.matcher(cmFromEthLogs);
                        srcReqHash = srcReqMatcher.find() ? srcReqMatcher.group(1) : "";

                        // 提取源链响应哈希（获取第一个有效匹配）
                        String srcRespPattern = "get resp txhash: ([a-fA-F0-9]+)";
                        Pattern srcRespRegex = Pattern.compile(srcRespPattern);
                        Matcher srcRespMatcher = srcRespRegex.matcher(cmFromEthLogs);
                        srcRespHash = srcRespMatcher.find() ? srcRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String dstPattern = "Obtained response cmhash on the target chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern dstRegex = Pattern.compile(dstPattern);
                        Matcher dstMatcher = dstRegex.matcher(h2cLogs);
                        dstHash = dstMatcher.find() ? dstMatcher.group(1) : "";
                    }else if (dstChainType.equalsIgnoreCase("fisco")) {
                        // 读取海河链日志文件
                        String fiscoLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fisco/logs/fisco.log";
                        String fiscoLogs = SSHConfig.executeCMD(fiscoLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String srcReqPattern = "Obtained request cmhash on the source chain\\([0-9]+\\): ([a-fA-F0-9]+)";
                        Pattern srcReqRegex = Pattern.compile(srcReqPattern);
                        Matcher srcReqMatcher = srcReqRegex.matcher(cmFromEthLogs);
                        srcReqHash = srcReqMatcher.find() ? srcReqMatcher.group(1) : "";

                        // 提取源链响应哈希（获取第一个有效匹配）
                        String srcRespPattern = "get resp txhash: ([a-fA-F0-9]+)";
                        Pattern srcRespRegex = Pattern.compile(srcRespPattern);
                        Matcher srcRespMatcher = srcRespRegex.matcher(cmFromEthLogs);
                        srcRespHash = srcRespMatcher.find() ? srcRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String dstPattern = "Obtained response cmhash on the target chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern dstRegex = Pattern.compile(dstPattern);
                        Matcher dstMatcher = dstRegex.matcher(fiscoLogs);
                        dstHash = dstMatcher.find() ? dstMatcher.group(1) : "";
                    }
                    else if (dstChainType.equalsIgnoreCase("fabric")) {
                        // 读取海河链日志文件
                        String fabriLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fabric/logs/fabric.log";
                        String fabriLogs = SSHConfig.executeCMD(fabriLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String srcReqPattern = "Obtained request cmhash on the source chain\\([0-9]+\\): ([a-fA-F0-9]+)";
                        Pattern srcReqRegex = Pattern.compile(srcReqPattern);
                        Matcher srcReqMatcher = srcReqRegex.matcher(cmFromEthLogs);
                        srcReqHash = srcReqMatcher.find() ? srcReqMatcher.group(1) : "";

                        // 提取源链响应哈希（获取第一个有效匹配）
                        String srcRespPattern = "get resp txhash: ([a-fA-F0-9]+)";
                        Pattern srcRespRegex = Pattern.compile(srcRespPattern);
                        Matcher srcRespMatcher = srcRespRegex.matcher(cmFromEthLogs);
                        srcRespHash = srcRespMatcher.find() ? srcRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String dstPattern = "Obtained response cmhash on the target chain\\(chainid: [0-9]+, cmhash: ([a-fA-F0-9]+)\\)";
                        Pattern dstRegex = Pattern.compile(dstPattern);
                        Matcher dstMatcher = dstRegex.matcher(fabriLogs);
                        dstHash = dstMatcher.find() ? dstMatcher.group(1) : "";
                    } else if (dstChainType.equalsIgnoreCase("bubi")) {
                        // 读取布比链日志文件
                        String bubiLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Bubi/logs/bubi.log";
                        String bubiLogs = SSHConfig.executeCMD(bubiLogCmd, "UTF-8");

                        // 读取长安链日志文件
                        String cmLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker/logs/chainmaker.log";
                        String cmLogs = SSHConfig.executeCMD(cmLogCmd, "UTF-8");

                        // 提取源链请求哈希
                        String srcReqPattern = "Obtained request cmhash on the source chain\\([0-9]+\\): ([a-fA-F0-9]+)";
                        Pattern srcReqRegex = Pattern.compile(srcReqPattern);
                        Matcher srcReqMatcher = srcReqRegex.matcher(cmFromEthLogs);
                        srcReqHash = srcReqMatcher.find() ? srcReqMatcher.group(1) : "";

                        // 提取源链响应哈希（获取第一个有效匹配）
                        String srcRespPattern = "get resp txhash: ([a-fA-F0-9]+)";
                        Pattern srcRespRegex = Pattern.compile(srcRespPattern);
                        Matcher srcRespMatcher = srcRespRegex.matcher(cmFromEthLogs);
                        srcRespHash = srcRespMatcher.find() ? srcRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String bubiToCmDstPattern = "contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern bubiToCmDstRegex = Pattern.compile(bubiToCmDstPattern);
                        Matcher bubiToCmDstMatcher = bubiToCmDstRegex.matcher(bubiLogs);
                        String bubiToCmDstHash = "";
                        if (bubiToCmDstMatcher.find()) {
                            bubiToCmDstHash = bubiToCmDstMatcher.group(1);
                            System.out.println("成功提取到目标链哈希: " + bubiToCmDstHash);
                        } else {
                            System.out.println("未能匹配到目标链哈希");
                            System.out.println("使用的正则表达式: " + bubiToCmDstPattern);
                            System.out.println("实际日志内容：");
                            System.out.println(cmLogs);
                        }

                        resultObj.put("srcReqHash", srcReqHash);
                        resultObj.put("srcRespHash", srcRespHash);
                        resultObj.put("dstHash", bubiToCmDstHash);
                        resultObj.put("crossChainResult", "布比链跨长安链操作执行成功");
                    }

                    resultObj.put("dstHash", dstHash);
                    resultObj.put("srcRespHash", srcRespHash);
                    resultObj.put("srcReqHash", srcReqHash);
                    resultObj.put("crossChainResult", "长安链跨链操作执行成功");
                    break;
                case "bubi":
                    // 执行布比链跨链命令
                    String bubiAppArgs = appArgs.replaceAll("\"", "\\\\\""); // 转义双引号
                    String bubiCmd = "source /etc/profile && source ~/.bashrc && cd /root/CIPS-Gemini-v1/CIPS-Gemini-Bubi && docker exec crossbubi_container go run main.go send "
                            + srcappId + " " + dstappId + " " + bubiAppArgs;
                    String bubiResult = SSHConfig.executeCMD(bubiCmd, "UTF-8");

                    // 打印命令输出用于调试
                    System.out.println("命令完整输出：");
                    System.out.println(bubiResult);

                    // 等待跨链操作完成
                    Thread.sleep(70000);

                    // 读取布比链日志文件
                    String bubiLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Bubi/logs/bubi.log";
                    String bubiLogs = SSHConfig.executeCMD(bubiLogCmd, "UTF-8");

                    // 连接目标链服务器
                    SSHConfig.connect(dstIp);

                    if (dstChainType.equalsIgnoreCase("chainmaker")) {
                        // 读取长安链日志文件
                        String cmLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker/logs/chainmaker.log";
                        String cmLogs = SSHConfig.executeCMD(cmLogCmd, "UTF-8");

                        // 提取源链请求哈希（从bubiResult中提取）
                        String bubiReqPattern = "chain: SendMsg succeed @ ([a-fA-F0-9]+)";
                        Pattern bubiReqRegex = Pattern.compile(bubiReqPattern);
                        Matcher bubiReqMatcher = bubiReqRegex.matcher(bubiResult);
                        String bubiReqHash = "";
                        if (bubiReqMatcher.find()) {
                            bubiReqHash = bubiReqMatcher.group(1);
                            System.out.println("成功提取到源链请求哈希: " + bubiReqHash);
                        } else {
                            System.out.println("未能匹配到源链请求哈希");
                            System.out.println("使用的正则表达式: " + bubiReqPattern);
                            System.out.println("实际输出内容：");
                            System.out.println(bubiResult);
                        }

                        // 提取源链响应哈希
                        String bubiRespPattern = "contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern bubiRespRegex = Pattern.compile(bubiRespPattern);
                        Matcher bubiRespMatcher = bubiRespRegex.matcher(bubiLogs);
                        String bubiRespHash = "";
                        if (bubiRespMatcher.find()) {
                            bubiRespHash = bubiRespMatcher.group(1);
                            System.out.println("成功提取到源链响应哈希: " + bubiRespHash);
                        } else {
                            System.out.println("未能匹配到源链响应哈希");
                            System.out.println("使用的正则表达式: " + bubiRespPattern);
                            System.out.println("实际日志内容：");
                            System.out.println(bubiLogs);
                        }

                        // 提取目标链哈希
                        String bubiToCmDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern bubiToCmDstRegex = Pattern.compile(bubiToCmDstPattern);
                        Matcher bubiToCmDstMatcher = bubiToCmDstRegex.matcher(cmLogs);
                        String bubiToCmDstHash = "";
                        if (bubiToCmDstMatcher.find()) {
                            bubiToCmDstHash = bubiToCmDstMatcher.group(1);
                            System.out.println("成功提取到目标链哈希: " + bubiToCmDstHash);
                        } else {
                            System.out.println("未能匹配到目标链哈希");
                            System.out.println("使用的正则表达式: " + bubiToCmDstPattern);
                            System.out.println("实际日志内容：");
                            System.out.println(cmLogs);
                        }

                        resultObj.put("srcReqHash", bubiReqHash);
                        resultObj.put("srcRespHash", bubiRespHash);
                        resultObj.put("dstHash", bubiToCmDstHash);
                        resultObj.put("crossChainResult", "布比链跨长安链操作执行成功");

                    } else if (dstChainType.equalsIgnoreCase("ethereum")) {
                        // 读取以太坊日志文件
                        String ethLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Ethereum/logs/eth.log";
                        String ethLogs = SSHConfig.executeCMD(ethLogCmd, "UTF-8");

                        // 提取源链请求哈希（从bubiResult中提取）
                        String bubiReqPattern = "chain: SendMsg succeed @ ([a-fA-F0-9]+)";
                        Pattern bubiReqRegex = Pattern.compile(bubiReqPattern);
                        Matcher bubiReqMatcher = bubiReqRegex.matcher(bubiResult);
                        String bubiReqHash = "";
                        if (bubiReqMatcher.find()) {
                            bubiReqHash = bubiReqMatcher.group(1);
                            System.out.println("成功提取到源链请求哈希: " + bubiReqHash);
                        } else {
                            System.out.println("未能匹配到源链请求哈希");
                            System.out.println("使用的正则表达式: " + bubiReqPattern);
                            System.out.println("实际输出内容：");
                            System.out.println(bubiResult);
                        }

                        // 提取源链响应哈希
                        String bubiRespPattern = "\\[DEBG\\]:\\s*contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern bubiRespRegex = Pattern.compile(bubiRespPattern);
                        Matcher bubiRespMatcher = bubiRespRegex.matcher(bubiLogs);
                        String bubiRespHash = bubiRespMatcher.find() ? bubiRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String bubiToEthDstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern bubiToEthDstRegex = Pattern.compile(bubiToEthDstPattern);
                        Matcher bubiToEthDstMatcher = bubiToEthDstRegex.matcher(ethLogs);
                        String bubiToEthDstHash = bubiToEthDstMatcher.find() ? bubiToEthDstMatcher.group(1) : "";

                        resultObj.put("srcReqHash", bubiReqHash);
                        resultObj.put("srcRespHash", bubiRespHash);
                        resultObj.put("dstHash", bubiToEthDstHash);
                        resultObj.put("crossChainResult", "布比链跨以太坊操作执行成功");

                    } else if (dstChainType.equalsIgnoreCase("h2chain")) {
                        // 读取海河链日志文件
                        String h2cLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-H2Chain/logs/h2chain.log";
                        String h2cLogs = SSHConfig.executeCMD(h2cLogCmd, "UTF-8");

                        // 提取源链请求哈希（从bubiResult中提取）
                        String bubiReqPattern = "chain: SendMsg succeed @ ([a-fA-F0-9]+)";
                        Pattern bubiReqRegex = Pattern.compile(bubiReqPattern);
                        Matcher bubiReqMatcher = bubiReqRegex.matcher(bubiResult);
                        String bubiReqHash = "";
                        if (bubiReqMatcher.find()) {
                            bubiReqHash = bubiReqMatcher.group(1);
                            System.out.println("成功提取到源链请求哈希: " + bubiReqHash);
                        } else {
                            System.out.println("未能匹配到源链请求哈希");
                            System.out.println("使用的正则表达式: " + bubiReqPattern);
                            System.out.println("实际输出内容：");
                            System.out.println(bubiResult);
                        }

                        // 提取源链响应哈希
                        String bubiRespPattern = "\\[DEBG\\]:\\s*contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern bubiRespRegex = Pattern.compile(bubiRespPattern);
                        Matcher bubiRespMatcher = bubiRespRegex.matcher(bubiLogs);
                        String bubiRespHash = bubiRespMatcher.find() ? bubiRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String bubiToH2cDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern bubiToH2cDstRegex = Pattern.compile(bubiToH2cDstPattern);
                        Matcher bubiToH2cDstMatcher = bubiToH2cDstRegex.matcher(h2cLogs);
                        String bubiToH2cDstHash = bubiToH2cDstMatcher.find() ? bubiToH2cDstMatcher.group(1) : "";

                        resultObj.put("srcReqHash", bubiReqHash);
                        resultObj.put("srcRespHash", bubiRespHash);
                        resultObj.put("dstHash", bubiToH2cDstHash);
                        resultObj.put("crossChainResult", "布比链跨海河链操作执行成功");
                    }else if (dstChainType.equalsIgnoreCase("fisco")) {
                        // 读取fisco日志文件
                        String fiscoLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fisco/logs/fisco.log";
                        String fiscoLogs = SSHConfig.executeCMD(fiscoLogCmd, "UTF-8");

                        // 提取源链请求哈希（从bubiResult中提取）
                        String bubiReqPattern = "chain: SendMsg succeed @ ([a-fA-F0-9]+)";
                        Pattern bubiReqRegex = Pattern.compile(bubiReqPattern);
                        Matcher bubiReqMatcher = bubiReqRegex.matcher(bubiResult);
                        String bubiReqHash = "";
                        if (bubiReqMatcher.find()) {
                            bubiReqHash = bubiReqMatcher.group(1);
                            System.out.println("成功提取到源链请求哈希: " + bubiReqHash);
                        } else {
                            System.out.println("未能匹配到源链请求哈希");
                            System.out.println("使用的正则表达式: " + bubiReqPattern);
                            System.out.println("实际输出内容：");
                            System.out.println(bubiResult);
                        }

                        // 提取源链响应哈希
                        String bubiRespPattern = "\\[DEBG\\]:\\s*contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern bubiRespRegex = Pattern.compile(bubiRespPattern);
                        Matcher bubiRespMatcher = bubiRespRegex.matcher(bubiLogs);
                        String bubiRespHash = bubiRespMatcher.find() ? bubiRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String bubiTofiscoDstPattern = "get req txhash: (0x[a-fA-F0-9]+)";
                        Pattern bubiTofiscoDstRegex = Pattern.compile(bubiTofiscoDstPattern);
                        Matcher bubiTofiscoDstMatcher = bubiTofiscoDstRegex.matcher(fiscoLogs);
                        String bubiTofiscoDstHash = bubiTofiscoDstMatcher.find() ? bubiTofiscoDstMatcher.group(1) : "";

                        resultObj.put("srcReqHash", bubiReqHash);
                        resultObj.put("srcRespHash", bubiRespHash);
                        resultObj.put("dstHash", bubiTofiscoDstHash);
                        resultObj.put("crossChainResult", "布比链跨fisco操作执行成功");
                    }
                    else if (dstChainType.equalsIgnoreCase("fabric")) {
                        // 读取海河链日志文件
                        String fabricLogCmd = "cat /root/CIPS-Gemini-v1/CIPS-Gemini-Fabric/logs/fabric.log";
                        String fabricLogs = SSHConfig.executeCMD(fabricLogCmd, "UTF-8");

                        // 提取源链请求哈希（从bubiResult中提取）
                        String bubiReqPattern = "chain: SendMsg succeed @ ([a-fA-F0-9]+)";
                        Pattern bubiReqRegex = Pattern.compile(bubiReqPattern);
                        Matcher bubiReqMatcher = bubiReqRegex.matcher(bubiResult);
                        String bubiReqHash = "";
                        if (bubiReqMatcher.find()) {
                            bubiReqHash = bubiReqMatcher.group(1);
                            System.out.println("成功提取到源链请求哈希: " + bubiReqHash);
                        } else {
                            System.out.println("未能匹配到源链请求哈希");
                            System.out.println("使用的正则表达式: " + bubiReqPattern);
                            System.out.println("实际输出内容：");
                            System.out.println(bubiResult);
                        }

                        // 提取源链响应哈希
                        String bubiRespPattern = "\\[DEBG\\]:\\s*contractCallGo succeed,hash=([a-fA-F0-9]+)";
                        Pattern bubiRespRegex = Pattern.compile(bubiRespPattern);
                        Matcher bubiRespMatcher = bubiRespRegex.matcher(bubiLogs);
                        String bubiRespHash = bubiRespMatcher.find() ? bubiRespMatcher.group(1) : "";

                        // 提取目标链哈希
                        String bubiToH2cDstPattern = "get req txhash: ([a-fA-F0-9]+)";
                        Pattern bubiToH2cDstRegex = Pattern.compile(bubiToH2cDstPattern);
                        Matcher bubiToH2cDstMatcher = bubiToH2cDstRegex.matcher(fabricLogs);
                        String bubiToH2cDstHash = bubiToH2cDstMatcher.find() ? bubiToH2cDstMatcher.group(1) : "";

                        resultObj.put("srcReqHash", bubiReqHash);
                        resultObj.put("srcRespHash", bubiRespHash);
                        resultObj.put("dstHash", bubiToH2cDstHash);
                        resultObj.put("crossChainResult", "布比链跨fabric操作执行成功");
                    }
                    break;

                default:
                    throw new IllegalArgumentException("不支持的源链类型: " + srcChainType);
            }

            response.setRet(ResultCode.SUCCESS);
            response.setData(resultObj);

            // 保存跨链交易记录到数据库
            try {
                saveCrossChainRecord(srcIp, srcChainType, dstIp, dstChainType, resultObj);
            } catch (Exception e) {
                log.error("保存跨链记录到数据库失败: " + e.getMessage());
                // 数据库保存失败不影响跨链操作结果
            }

        } catch (Exception e) {
            response.setRet(ResultCode.FAILURE);
            response.setMessage("跨链操作失败: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 执行完整的跨链操作（包括启动网关和执行跨链）
     */
    @Override
    public CommonResp executeFullCrossChain(String srcIp, String srcChainType, String dstIp, String dstChainType,
                                            String relayIp, String srcappId, String dstappId, String appArgs, String token) {
        CommonResp response = new CommonResp();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("token", token);
        User user = userMapper.selectOne(wrapper);
        if (appArgs.contains("Access")) {// 如果是带权限控制，就把Access去掉变成普通的调用
            String[] spl = appArgs.split("Access");
            appArgs = spl[0] + spl[1];
            if (user.getAuthority() == 0) {// 没有权限直接退出
                response.setRet(ResultCode.AUTH_ERROR);
            }
        }

        if (srcappId == null || srcappId.isEmpty()) {
            srcappId = "";
        }
        if (dstappId == null || dstappId.isEmpty()) {
            dstappId = "";
        }
        if (appArgs == null || appArgs.isEmpty()) {
            appArgs = "";
        }

        // 所有的appid都为1
        srcappId = "1";
        dstappId = "1";

        try {
            // 第一步：启动网关
            CommonResp gatewayResponse = startGateways(srcIp, srcChainType, dstIp, dstChainType, relayIp);
            if (!ResultCode.SUCCESS.Code.equals(gatewayResponse.getCode())) {
                return gatewayResponse; // 如果网关启动失败，直接返回错误
            }

            // 等待网关启动完成（这里等待10秒，确保网关完全启动）
            Thread.sleep(10000);

            // 第二步：执行跨链操作
            CommonResp crossChainResponse = executeCrossChain(srcIp, srcChainType, dstIp, dstChainType, srcappId,
                    dstappId, appArgs);
            if (!ResultCode.SUCCESS.Code.equals(crossChainResponse.getCode())) {
                return crossChainResponse; // 如果跨链操作失败，直接返回错误
            }

            // 设置成功响应，只返回跨链执行结果
            response.setRet(ResultCode.SUCCESS);
            response.setData(crossChainResponse.getData());

        } catch (Exception e) {
            response.setRet(ResultCode.FAILURE);
            response.setMessage("完整跨链操作失败: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 保存跨链交易记录到数据库
     *
     * @param srcIp        源链IP
     * @param srcChainType 源链类型
     * @param dstIp        目标链IP
     * @param dstChainType 目标链类型
     * @param resultData   跨链操作结果数据
     */
    private void saveCrossChainRecord(String srcIp, String srcChainType, String dstIp,
                                      String dstChainType, Object resultData) {
        try {
            Crosschain crosschain = new Crosschain();

            // 设置基本信息
            crosschain.setSrcIp(srcIp);
            crosschain.setSrcChainType(srcChainType);
            crosschain.setDstIp(dstIp);
            crosschain.setDstChainType(dstChainType);

            // 设置端口信息
            crosschain.setSrcPort(srcPort(srcChainType));
            crosschain.setDstPort(dstPort(dstChainType));

            // 生成唯一的交易哈希
            String txHash = generateTxHash(srcIp, dstIp, srcChainType, dstChainType);
            crosschain.setTxHash(txHash);

            // 设置时间戳
            crosschain.setTxTime(new Date());

            // 从结果数据中提取哈希信息
            if (resultData instanceof JSONObject) {
                JSONObject resultObj = (JSONObject) resultData;

                // 提取源链请求哈希
                String srcReqHash = resultObj.getString("srcReqHash");
                if (srcReqHash != null && !srcReqHash.isEmpty()) {
                    crosschain.setSrcHash(srcReqHash);
                }

                // 提取目标链哈希
                String dstHash = resultObj.getString("dstHash");
                if (dstHash != null && !dstHash.isEmpty()) {
                    crosschain.setDstHash(dstHash);
                }

                // 提取响应哈希
                String srcRespHash = resultObj.getString("srcRespHash");
                if (srcRespHash != null && !srcRespHash.isEmpty()) {
                    crosschain.setResponseHash(srcRespHash);
                }
            }

            // 插入数据库
            crosschainMapper.insert(crosschain);
            System.out.println("跨链交易记录已保存到数据库，交易哈希: " + txHash);
        } catch (Exception e) {
            System.out.println("保存跨链记录到数据库时发生错误: " + e.getMessage());
            throw e; // 重新抛出异常，让调用方处理
        }
    }

    /**
     * 生成唯一的交易哈希
     *
     * @param srcIp        源链IP
     * @param dstIp        目标链IP
     * @param srcChainType 源链类型
     * @param dstChainType 目标链类型
     * @return 唯一的交易哈希
     */
    private String generateTxHash(String srcIp, String dstIp, String srcChainType, String dstChainType) {
        // 使用UUID和时间戳生成唯一哈希
        String uniqueId = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis();
        String hashInput = srcIp + dstIp + srcChainType + dstChainType + timestamp + uniqueId;

        // 哈希生成
        return String.valueOf(hashInput.hashCode()).replace("-", "") +
                String.valueOf(timestamp).substring(8); // 取时间戳的后几位
    }

}
