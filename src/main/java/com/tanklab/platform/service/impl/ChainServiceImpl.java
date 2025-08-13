package com.tanklab.platform.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanklab.platform.common.*;
import com.tanklab.platform.ds.req.*;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Chain;
import com.tanklab.platform.mapper.ChainMapper;
import com.tanklab.platform.service.ChainService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.web3j.protocol.core.methods.response.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

/**
 * <p>
 * 链信息表 服务实现类
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
/*
 * public class ChainServiceImpl extends ServiceImpl<ChainMapper, Chain>
 * implements ChainService {
 * 
 * 
 * }
 */
@Service
public class ChainServiceImpl extends ServiceImpl<ChainMapper, Chain> implements ChainService {
    // 这边的post格式用编号是不是比链IP更方便前端开发？如果是的话可以修改
    @Autowired
    private ChainMapper chainMapper;
    @Autowired
    public ChainServiceImpl ChainService;

    private static String cmcpath = "~/CIPS-Gemini-ChainMaker/chainmaker/chainmaker-go/tools/cmc";
    // server服务器 ~/CIPS-H2Chain
    // 学弟服务器 ~/ChainMaker/chainmaker-go/tools/cmc

    private static String cmcexepath = "./cmc";
    // server服务器 ./chainmaker-go/tools/cmc/cmc
    // 学弟服务器 ./cmc

    private static String ethname = "8086";
    private static String h2Chainname = "8087";
    private static String chainmakername = "8088";
    private static String bubiname = "8089";
    private static String fabricname = "8090";
    private static String liantongname = "8099";

    private static String ethWsPort = "10026";

    private static String sdktype = "./testdata/sdk_config_pk.yml";
    // server服务器
    // /root/CIPS-H2Chain/chainmaker/config_files/sdkconfigs/chain3_sdkconfig1.yml
    // 学弟服务器 ./testdata/sdk_config.yml


    /**
     * @author ZZY
     * @date 2025/6/4 22:55
    **/
    @Override
    public CommonResp querychainInfo() {
        CommonResp querychainresp = new CommonResp();

        // QueryWrapper<Chain> wrapper = new QueryWrapper<>();
        // wrapper.select("chain_id", "ip_address", "port", "chain_type");
        // List<Chain> chains = chainMapper.selectList(wrapper);

        String ip = "192.168.0.";


        Integer chainsize = 5;

        JSONObject totalInfo = new JSONObject();
        JSONArray chainsInfo = new JSONArray();

        int x = 0;
        for (CrosschainInfo chain: CrosschainInfo.values()){
            JSONObject singleChain = new JSONObject();
//            JsonObject singleChain = new JsonObject();
            singleChain.put("Id",chain.ChainName);
            singleChain.put("Name",chain.ChineseName);
            JSONArray children = new JSONArray();
            for (int i = 0; i< chainsize; i++){
                JSONObject single = new JSONObject();
                single.put("Id",chain.ChainId + i);
                single.put("Name",ip + String.valueOf(i + 1)+":"+String.valueOf(chain.ChainPort));
                JSONObject parent = new JSONObject();
                parent.put("Id",chain.ChainName);
                parent.put("Name",chain.ChineseName);
                single.put("parent",parent);
                children.add(single);
            }
            x++;
            singleChain.put("children",children);
            chainsInfo.add(singleChain);
        }
        totalInfo.put("totalInfo",chainsInfo);
        querychainresp.setRet(ResultCode.SUCCESS);
        querychainresp.setData(totalInfo);
        System.out.println(totalInfo);
        return querychainresp;
    }

    public CommonResp checkChainnewblock(ChainReq chainreq) {
        int colonIndex = chainreq.getChainIP().indexOf(":");
        String ipAddress = chainreq.getChainIP().substring(0, colonIndex);
        String portNumber = chainreq.getChainIP().substring(colonIndex + 1);

        // QueryWrapper<Chain> wrapper = new QueryWrapper<>();
        // wrapper.select("chain_type");
        // wrapper.eq("ip_address", ipAddress);
        // wrapper.eq("port", portNumber);
        // Chain chain = chainMapper.selectOne(wrapper);
        // String chainType = chain.getChainType();

        CommonResp chainresp = new CommonResp();
        // System.out.println(chainreq.getChainIP());
        if (portNumber.equals(ethname)) {
            // try {
            // // 创建 OkHttpClient 实例，并设置超时时间
            // OkHttpClient.Builder builder = new OkHttpClient.Builder()
            // .connectTimeout(1000, TimeUnit.SECONDS)
            // .readTimeout(1000, TimeUnit.SECONDS);

            // // 创建自定义的 HttpService，并传入 OkHttpClient 实例
            // HttpService httpService = new HttpService("http://" + ipAddress + ":10012",
            // builder.build());
            // // HttpService httpService = new
            // // HttpService(String.valueOf("http://116.204.36.31:10012"),
            // builder.build());
            // // 创建 Web3j 实例
            // Web3j web3j = Web3j.build(httpService);

            // // 查询该链的块高
            // BigInteger blockHeight = web3j.ethBlockNumber().send().getBlockNumber();
            // JSONObject heightinfo = new JSONObject();
            // heightinfo.put("heightinfo", blockHeight);
            // chainresp.setData(heightinfo);
            // } catch (IOException e) {
            // e.printStackTrace();
            // chainresp.setData("Failed to connect to the blockchain node.");
            // }

            WebSocketService webSocketService = null;
            try {
                // WebSocket 地址
                String wsUrl = "ws://" + ipAddress + ":" + ethWsPort;

                // 创建 WebSocketService，并自动连接
                webSocketService = new WebSocketService(wsUrl, true);
                webSocketService.connect(); // 连接 WebSocket

                // 创建 Web3j 实例
                Web3j web3j = Web3j.build(webSocketService);

                // 查询该链的块高
                BigInteger blockHeight = web3j.ethBlockNumber().send().getBlockNumber();
                JSONObject heightinfo = new JSONObject();
                heightinfo.put("heightinfo", blockHeight);
                chainresp.setData(heightinfo);

            } catch (Exception e) {
                e.printStackTrace();
                chainresp.setData("Error retrieving block height.");
            }
        } else if (portNumber.equals(chainmakername)) {
            String logs = "";
            try {
                SSHConfig.connect(ipAddress);
                logs = SSHConfig.executeCMD(
                        "cd " + cmcpath
                                + " && " + cmcexepath + " query block-by-height --chain-id=chain1 --sdk-conf-path="
                                + sdktype,
                        "UTF-8");
            } catch (Exception e) {
                System.out.println("SSH ERROR");
            }
            BigInteger blockHeight = JsonParser.parseString(logs).getAsJsonObject().getAsJsonObject("block")
                    .getAsJsonObject("header")
                    .get("block_height").getAsBigInteger();
            JSONObject heightinfo = new JSONObject();
            heightinfo.put("heightinfo", blockHeight);
            chainresp.setData(heightinfo);
        } else if (portNumber.equals(h2Chainname)) {
            String targetUrl = "http://" + ipAddress + ":8000/api/blockChain/blockHeight";
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
                BigInteger blockHeight = BigInteger.valueOf(Long.parseLong(response.toString()));
                JSONObject heightinfo = new JSONObject();
                heightinfo.put("heightinfo", blockHeight);
                chainresp.setData(heightinfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (portNumber.equals(bubiname)) {
            String targetUrl = "http://" + ipAddress + ":19333/getLedger";
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
                JsonObject headerJsonObj = JsonParser.parseString(response.toString()).getAsJsonObject()
                        .getAsJsonObject("result")
                        .getAsJsonObject("header");
                BigInteger height = headerJsonObj.get("seq").getAsBigInteger();
                JSONObject heightinfo = new JSONObject();
                heightinfo.put("heightinfo", height);
                chainresp.setData(heightinfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (portNumber.equals(liantongname)) {
//            String targetUrl = "https://121.37.119.118:8443/api/baas/explorer/unicom/blockchains/4e8776c142f845e589bd00db6448d449/channels/ch1/blocks";
//            String authorizationToken = chainreq.getAuthorizationToken();
//
//            String logs = "";
//
//            try {
//                // 禁用 SSL 验证
//                disableSSLVerification();
//
//                // 创建URL对象
//                URL url = new URL(targetUrl);
//                // 打开连接
//                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//
//                // 设置Authorization头部
//                connection.setRequestProperty("authorization", authorizationToken);
//
//                // 读取响应数据
//                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//
//                BigInteger height = JsonParser.parseString(response.toString()).getAsJsonObject()
//                        .get("total_block_count").getAsBigInteger().subtract(BigInteger.valueOf(1));
//                JSONObject heightinfo = new JSONObject();
//                heightinfo.put("heightinfo", height);
//                chainresp.setData(heightinfo);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        return chainresp;
    }

    public CommonResp checkHeightInfo(BlockheightReq blockheightReq) { /////////////////
        CommonResp queryBlockInfoResp = new CommonResp();
        int colonIndex = blockheightReq.getChainIP().indexOf(":");
        String ipAddress = blockheightReq.getChainIP().substring(0, colonIndex);
        String portNumber = blockheightReq.getChainIP().substring(colonIndex + 1);

        // QueryWrapper<Chain> wrapper = new QueryWrapper<>();
        // wrapper.select("chain_type");
        // wrapper.eq("ip_address", ipAddress);
        // wrapper.eq("port", portNumber);
        // Chain chain = chainMapper.selectOne(wrapper);
        // String chainType = chain.getChainType();
        // 用块高来查询
        // 查询指定块高的区块信息
        if (portNumber.equals(ethname)) {
            // Web3j web3j = Web3j.build(new HttpService("http://" +
            // blockheightReq.getChainIP())); // 替换为你的节点地址

            WebSocketService webSocketService = null;
            try {
                // DefaultBlockParameter blockParameter = new DefaultBlockParameterNumber(
                // Long.parseLong(blockheightReq.getBlockHEIGHT()));
                // EthBlock.Block block = web3j.ethGetBlockByNumber(blockParameter, false)
                // .send()
                // .getBlock();

                // WebSocket 地址（确保 Geth 启用了 WebSocket 并监听 10026 端口）
                String wsUrl = "ws://" + ipAddress + ":" + ethWsPort;

                // 创建 WebSocketService
                webSocketService = new WebSocketService(wsUrl, true);
                webSocketService.connect(); // 连接 WebSocket

                // 创建 Web3j 实例
                Web3j web3j = Web3j.build(webSocketService);

                // 获取指定区块的信息
                DefaultBlockParameter blockParameter = new DefaultBlockParameterNumber(
                        Long.parseLong(blockheightReq.getBlockHEIGHT()));

                EthBlock.Block block = web3j.ethGetBlockByNumber(blockParameter, false)
                        .send()
                        .getBlock();

                BigInteger timestamp = block.getTimestamp();
                BigInteger blockSize = block.getSize();
                String previousBlockAddress = block.getParentHash();
                BigInteger gasLimit = block.getGasLimit();
                BigInteger gasUsed = block.getGasUsed();
                String stateRoot = block.getStateRoot();
                BigInteger totalDifficulty = block.getTotalDifficulty();
                BigInteger difficulty = block.getDifficulty();
                BigInteger blockReward = block.getGasUsed().multiply(block.getGasLimit());
                String minerAddress = block.getMiner();
                int transactionCount = block.getTransactions().size();

                JSONObject blockInfo = new JSONObject();
                blockInfo.put("blockHeight", blockheightReq.getBlockHEIGHT());
                blockInfo.put("timestamp", timestamp);
                blockInfo.put("blockSize", blockSize);
                blockInfo.put("transactionCount", transactionCount);
                blockInfo.put("previousBlockAddress", previousBlockAddress);
                blockInfo.put("gasLimit", gasLimit);
                blockInfo.put("gasUsed", gasUsed);
                blockInfo.put("stateRoot", stateRoot);
                blockInfo.put("totalDifficulty", totalDifficulty);
                blockInfo.put("difficulty", difficulty);
                blockInfo.put("blockReward", blockReward);
                blockInfo.put("minerAddress", minerAddress);

                queryBlockInfoResp.setRet(ResultCode.SUCCESS);
                queryBlockInfoResp.setData(blockInfo);
            } catch (IOException e) {
                queryBlockInfoResp.setData("Failed to connect to the blockchain node.");
            }
            System.out.println("-------ETH区块信息查询完毕-------");
        } else if (portNumber.equals(chainmakername)) {
            String logs = "";
            try {
                SSHConfig.connect(ipAddress);
                String cmd = "cd " + cmcpath
                        + " && " + cmcexepath + " query block-by-height "
                        + blockheightReq.getBlockHEIGHT()
                        + " --chain-id=chain1 --sdk-conf-path=" + sdktype;
                logs = SSHConfig.executeCMD(cmd, "UTF-8");
            } catch (Exception e) {
                // System.out.println("SSH ERROR");
                queryBlockInfoResp.setData("Failed to connect to the blockchain node.");
            }

            // System.out.println("ChainMaker查询结束...");
            JsonObject headerJsonObj = JsonParser.parseString(logs).getAsJsonObject().getAsJsonObject("block")
                    .getAsJsonObject("header");
            BigInteger blockHeight = headerJsonObj.get("block_height").getAsBigInteger();
            String blockHash = headerJsonObj.get("block_hash").getAsString();
            BigInteger timeStamp = headerJsonObj.get("block_timestamp").getAsBigInteger();
            String dagHash = headerJsonObj.get("dag_hash").getAsString();
            BigInteger transactionCount = headerJsonObj.get("tx_count").getAsBigInteger();
            String previousBlockHash = headerJsonObj.get("pre_block_hash").getAsString();
            String rwSetRoot = headerJsonObj.get("rw_set_root").getAsString();
            String proposerMemberInfo = headerJsonObj.getAsJsonObject("proposer").get("member_info").getAsString();
            String signature = headerJsonObj.get("signature").getAsString();
            String txRoot = headerJsonObj.get("tx_root").getAsString();

            JSONObject blockInfo = new JSONObject();
            blockInfo.put("blockHeight", blockHeight);
            blockInfo.put("blockHash", blockHash);
            blockInfo.put("timeStamp", timeStamp);
            blockInfo.put("dagHash", dagHash);
            blockInfo.put("transactionCount", transactionCount);
            blockInfo.put("previousBlockHash", previousBlockHash);
            blockInfo.put("rwSetRoot", rwSetRoot);
            blockInfo.put("proposerMemberInfo", proposerMemberInfo);
            blockInfo.put("signature", signature);
            blockInfo.put("txRoot", txRoot);

            queryBlockInfoResp.setRet(ResultCode.SUCCESS);
            queryBlockInfoResp.setData(blockInfo);

            System.out.println("-------ChainMaker区块信息查询完毕-------");
        } else if (portNumber.equals(h2Chainname)) {
            String targetUrl = "http://" + ipAddress + ":8000/api/blockChain/blockByHeight?blockHeight="
                    + blockheightReq.getBlockHEIGHT() + "&includeTransactions=true";
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            // System.out.println("H2hain查询结束...");
            JsonObject headerJsonObj = JsonParser.parseString(logs).getAsJsonObject().getAsJsonObject("Header");
            BigInteger blockHeight = headerJsonObj.get("Height").getAsBigInteger();
            String blockHash = JsonParser.parseString(logs).getAsJsonObject().get("BlockHash").getAsString();
            String timeStamp = headerJsonObj.get("Time").getAsString();
            BigInteger blockSize = JsonParser.parseString(logs).getAsJsonObject().get("BlockSize").getAsBigInteger();
            BigInteger transactionCount = JsonParser.parseString(logs).getAsJsonObject().getAsJsonObject("Body")
                    .get("TransactionsCount").getAsBigInteger();
            String previousBlockHash = headerJsonObj.get("PreviousBlockHash").getAsString();
            String merkleTreeRootOfWorldState = headerJsonObj.get("MerkleTreeRootOfWorldState").getAsString();
            String merkleTreeRootOfTransactions = headerJsonObj.get("MerkleTreeRootOfTransactions").getAsString();
            String merkleTreeRootOfTransactionState = headerJsonObj.get("MerkleTreeRootOfTransactionState")
                    .getAsString();
            String signerPubkey = headerJsonObj.get("SignerPubkey").getAsString();

            JSONObject blockInfo = new JSONObject();
            blockInfo.put("blockHeight", blockHeight);
            blockInfo.put("blockHash", blockHash);
            blockInfo.put("timeStamp", timeStamp);
            blockInfo.put("blockSize", blockSize);
            blockInfo.put("transactionCount", transactionCount);
            blockInfo.put("previousBlockHash", previousBlockHash);
            blockInfo.put("merkleTreeRootOfWorldState", merkleTreeRootOfWorldState);
            blockInfo.put("merkleTreeRootOfTransactions", merkleTreeRootOfTransactions);
            blockInfo.put("merkleTreeRootOfTransactionState", merkleTreeRootOfTransactionState);
            blockInfo.put("signerPubkey", signerPubkey);

            queryBlockInfoResp.setRet(ResultCode.SUCCESS);
            queryBlockInfoResp.setData(blockInfo);

            System.out.println("-------H2CHain区块信息查询完毕-------");
        } else if (portNumber.equals(bubiname)) {
            String targetUrl = "http://" + ipAddress + ":19333/getLedger?seq=" + blockheightReq.getBlockHEIGHT();
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
                JsonObject headerJsonObj = JsonParser.parseString(response.toString()).getAsJsonObject()
                        .getAsJsonObject("result")
                        .getAsJsonObject("header");

                BigInteger height = headerJsonObj.get("seq").getAsBigInteger();
                String accountTreeHash = headerJsonObj.get("account_tree_hash").getAsString();
                String closeTime = headerJsonObj.get("close_time").getAsString();
                String consensusValueHash = headerJsonObj.get("consensus_value_hash").getAsString();
                String feesHash = headerJsonObj.get("fees_hash").getAsString();
                String hash = headerJsonObj.get("hash").getAsString();
                String previousHash = headerJsonObj.get("previous_hash").getAsString();
                String validatorsHash = headerJsonObj.get("validators_hash").getAsString();
                String version = headerJsonObj.get("version").getAsString();

                JSONObject blockInfo = new JSONObject();
                blockInfo.put("height", height);
                blockInfo.put("accountTreeHash", accountTreeHash);
                blockInfo.put("closeTime", closeTime);
                blockInfo.put("consensusValueHash", consensusValueHash);
                blockInfo.put("feesHash", feesHash);
                blockInfo.put("hash", hash);
                blockInfo.put("previousHash", previousHash);
                blockInfo.put("validatorsHash", validatorsHash);
                blockInfo.put("version", version);

                queryBlockInfoResp.setRet(ResultCode.SUCCESS);
                queryBlockInfoResp.setData(blockInfo);

                System.out.println("-------BuBi区块信息查询完毕-------");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (portNumber.equals(liantongname)) {
            String targetUrl = "https://121.37.119.118:8443/api/baas/explorer/unicom/blockchains/4e8776c142f845e589bd00db6448d449/channels/ch1/blocks?start_block_num="
                    + blockheightReq.getBlockHEIGHT() + "&block_count=1";
            String authorizationToken = blockheightReq.getAuthorizationToken();
            String logs = "";
            try {
                // 禁用 SSL 验证
                disableSSLVerification();

                // 创建URL对象
                URL url = new URL(targetUrl);
                // 打开连接
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // 设置Authorization头部
                connection.setRequestProperty("authorization", authorizationToken);

                // 读取响应数据
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                logs = response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JsonObject rawBlock = JsonParser.parseString(logs).getAsJsonObject().getAsJsonArray("blocks").get(0)
                    .getAsJsonObject();
            String channelName = rawBlock.get("channel_name").getAsString();
            String hash = rawBlock.get("hash").getAsString();
            BigInteger number = rawBlock.get("number").getAsBigInteger();
            String dataHash = rawBlock.get("data_hash").getAsString();
            String previousHash = rawBlock.get("previous_hash").getAsString();
            String nextHash = rawBlock.get("next_hash").getAsString();
            if (nextHash.equals(""))
                nextHash = "null";
            BigInteger lastConfigIndex = rawBlock.get("last_config_index").getAsBigInteger();
            BigInteger txCount = rawBlock.get("tx_count").getAsBigInteger();
            String createdAt = rawBlock.get("created_at").getAsString();

            JSONObject blockInfo = new JSONObject();
            blockInfo.put("channelName", channelName);
            blockInfo.put("hash", hash);
            blockInfo.put("number", number);
            blockInfo.put("dataHash", dataHash);
            blockInfo.put("previousHash", previousHash);
            blockInfo.put("nextHash", nextHash);
            blockInfo.put("lastConfigIndex", lastConfigIndex);
            blockInfo.put("txCount", txCount);
            blockInfo.put("createdAt", createdAt);
            queryBlockInfoResp.setRet(ResultCode.SUCCESS);
            queryBlockInfoResp.setData(blockInfo);
            System.out.println("-------联通链区块信息查询完毕-------");
        }

        return queryBlockInfoResp;
    }
    /*
        zzy fake data added
     */
    public CommonResp checkNewBlock(ChainReq chainreq) {
        CommonResp queryNewBlock = new CommonResp();
        int colonIndex = chainreq.getChainIP().indexOf(":");
        int topN = chainreq.getTopN();
        String ipAddress = chainreq.getChainIP().substring(0, colonIndex);
        String portNumber = chainreq.getChainIP().substring(colonIndex + 1);

        // QueryWrapper<Chain> wrapper = new QueryWrapper<>();
        // wrapper.select("chain_type");
        // wrapper.eq("ip_address", ipAddress);
        // wrapper.eq("port", portNumber);
        // Chain chain = chainMapper.selectOne(wrapper);
        // String chainType = chain.getChainType();
        JSONArray blocks = new JSONArray();
        if (portNumber.equals(CrosschainInfo.ETH.ChainPort)) {
            BigInteger blockHeight = new BigInteger("0");
            // Fake data
            if (!BlockchainConfig.do_update_blockchain){
                for (int i = 1000;i<1000+topN;i++){
                    JSONObject blockInfo = new JSONObject();
                    blockInfo.put("blockHeight", i);
                    blockInfo.put("timestamp", 1704358265);
                    blockInfo.put("blockSize", 610);
                    blockInfo.put("transactionCount", 0);
                    blockInfo.put("previousBlockAddress", "0x685968c1ba6b5cff8ce01e38eae130234bf6256c9cdbfdab3d31231f53ca9304");
                    blockInfo.put("gasLimit", "26771921328");
                    blockInfo.put("gasUsed", "0");
                    blockInfo.put("stateRoot", "0xccad9fe6165ff1175b4a9d0daafb459d4a180371960ffd418a064a0ba6ee452c");
                    blockInfo.put("totalDifficulty", "5256880");
                    blockInfo.put("difficulty", "2");
                    blockInfo.put("blockReward", "0");
                    blockInfo.put("minerAddress", "0x0000000000000000000000000000000000000000");
                    blocks.add(blockInfo);
                }
                System.out.println("-------ETH最新十个区块信息查询完毕-------");
                JSONObject tenblocks = new JSONObject();
                tenblocks.put("tenBlocksInfo", blocks);
                queryNewBlock.setRet(ResultCode.SUCCESS);
                queryNewBlock.setData(tenblocks);
                return queryNewBlock;
            }
            WebSocketService webSocketService = null;
            // WebSocket 地址
            String wsUrl = "ws://" + ipAddress + ":" + ethWsPort;

            // 创建 WebSocketService
            webSocketService = new WebSocketService(wsUrl, true);
            try {
                webSocketService.connect(); // 连接 WebSocket
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 创建 Web3j 实例
            Web3j web3j = Web3j.build(webSocketService);
            try {
                // // 创建 OkHttpClient 实例，并设置超时时间
                // OkHttpClient.Builder builder = new OkHttpClient.Builder()
                // .connectTimeout(1000, TimeUnit.SECONDS)
                // .readTimeout(1000, TimeUnit.SECONDS);

                // // 创建自定义的 HttpService，并传入 OkHttpClient 实例
                // HttpService httpService = new HttpService("http://" + chainreq.getChainIP(),
                // builder.build());
                // // HttpService httpService = new
                // // HttpService(String.valueOf("http://116.204.36.31:10012"),
                // builder.build());
                // // 创建 Web3j 实例
                // Web3j web3j = Web3j.build(httpService);

                // // 查询该链的块高
                // blockHeight = web3j.ethBlockNumber().send().getBlockNumber();

                // 查询当前块高
                blockHeight = web3j.ethBlockNumber().send().getBlockNumber();

            } catch (IOException e) {
                e.printStackTrace();
            }
            int j = 0;
            for (BigInteger i = blockHeight; i.compareTo(BigInteger.ZERO) >= 0; i = i.subtract(new BigInteger("1"))) {
                // Web3j web3j = Web3j.build(new HttpService("http://" +
                // chainreq.getChainIP())); // 替换为你的节点地址
                try {
                    // DefaultBlockParameter blockParameter = new DefaultBlockParameterNumber(
                    // Long.parseLong(i.toString()));
                    // EthBlock.Block block = web3j.ethGetBlockByNumber(blockParameter, false)
                    // .send()
                    // .getBlock();

                    DefaultBlockParameter blockParameter = new DefaultBlockParameterNumber(i);
                    EthBlock.Block block = web3j.ethGetBlockByNumber(blockParameter, false)
                            .send()
                            .getBlock();

                    BigInteger timestamp = block.getTimestamp();
                    BigInteger blockSize = block.getSize();
                    String previousBlockAddress = block.getParentHash();
                    BigInteger gasLimit = block.getGasLimit();
                    BigInteger gasUsed = block.getGasUsed();
                    String stateRoot = block.getStateRoot();
                    BigInteger totalDifficulty = block.getTotalDifficulty();
                    BigInteger difficulty = block.getDifficulty();
                    BigInteger blockReward = block.getGasUsed().multiply(block.getGasLimit());
                    String minerAddress = block.getMiner();
                    int transactionCount = block.getTransactions().size();

                    JSONObject blockInfo = new JSONObject();
                    blockInfo.put("blockHeight", i);
                    blockInfo.put("timestamp", timestamp);
                    blockInfo.put("blockSize", blockSize);
                    blockInfo.put("transactionCount", transactionCount);
                    blockInfo.put("previousBlockAddress", previousBlockAddress);
                    blockInfo.put("gasLimit", gasLimit);
                    blockInfo.put("gasUsed", gasUsed);
                    blockInfo.put("stateRoot", stateRoot);
                    blockInfo.put("totalDifficulty", totalDifficulty);
                    blockInfo.put("difficulty", difficulty);
                    blockInfo.put("blockReward", blockReward);
                    blockInfo.put("minerAddress", minerAddress);

                    j++;
                    blocks.add(blockInfo);

                    if (j == 10)
                        break;
                } catch (IOException e) {
                    queryNewBlock.setData("Failed to connect to the blockchain node.");
                }
            }
            System.out.println("-------ETH最新十个区块信息查询完毕-------");
        } else if (portNumber.equals(CrosschainInfo.CMK.ChainPort)) {
            String logs = "";

            if (!BlockchainConfig.do_update_blockchain){
                for (int i = 1000;i<1000+topN;i++){
                    JSONObject blockInfo = new JSONObject();
                    blockInfo.put("blockHeight", i);
                    blockInfo.put("blockHash", "G1AO3fU44Yh+O3SaE5Ltyw4+8KC0n3COaRLaiznT2Ag=");
                    blockInfo.put("timeStamp", 1716795134);
                    blockInfo.put("dagHash", "CNp8RcsgQ3fn5CJJzaVxP6hlEW3btMtaGUmy5bQ4pqs=");
                    blockInfo.put("transactionCount", 1);
                    blockInfo.put("previousBlockHash", "8pV7EzTlL8diFPKYFXUA0TR4n1tKYYtqnp8hQ4HrV4w=");
                    blockInfo.put("rwSetRoot", "zBfmFxBQMw0/tNxGsIfpL66Af1rX+4pBN8v4JSOFqZc=");
                    blockInfo.put("proposerMemberInfo", "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNmakNDQWlTZ0F3SUJBZ0lERHkvNk1Bb0dDQ3FHU000OUJBTUNNSUdLTVFzd0NRWURWUVFHRXdKRFRqRVEKTUE0R0ExVUVDQk1IUW1WcGFtbHVaekVRTUE0R0ExVUVCeE1IUW1WcGFtbHVaekVmTUIwR0ExVUVDaE1XZDNndApiM0puTkM1amFHRnBibTFoYTJWeUxtOXlaekVTTUJBR0ExVUVDeE1KY205dmRDMWpaWEowTVNJd0lBWURWUVFECkV4bGpZUzUzZUMxdmNtYzBMbU5vWVdsdWJXRnJaWEl1YjNKbk1CNFhEVEl6TURjeE5EQXlNek14TjFvWERUSTQKTURjeE1qQXlNek14TjFvd2daY3hDekFKQmdOVkJBWVRBa05PTVJBd0RnWURWUVFJRXdkQ1pXbHFhVzVuTVJBdwpEZ1lEVlFRSEV3ZENaV2xxYVc1bk1SOHdIUVlEVlFRS0V4WjNlQzF2Y21jMExtTm9ZV2x1YldGclpYSXViM0puCk1SSXdFQVlEVlFRTEV3bGpiMjV6Wlc1emRYTXhMekF0QmdOVkJBTVRKbU52Ym5ObGJuTjFjekV1YzJsbmJpNTMKZUMxdmNtYzBMbU5vWVdsdWJXRnJaWEl1YjNKbk1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRQpDaVQ5VnQ3RGpLN0U5aEphSmVZNi9TV1dzZ01rQlR4VHM4Y2pvWUt1WGlpRENCcjErNS9VcWN4bHova292MGFOCnIxaVFxTExCRlJvUzA5VGJ2azNvSnFOcU1HZ3dEZ1lEVlIwUEFRSC9CQVFEQWdiQU1Da0dBMVVkRGdRaUJDQysKODFjeDB1Rk1QQUxoZ0xMMndnL1lvdnh6SDVud3JnUlhLTjVKWXh4TDJqQXJCZ05WSFNNRUpEQWlnQ0JrcWZUSwpGTjlRdzRCVW1SQWtmZXBJd0RrYTZYV0lUcHh4UGY1b2lqRVR2akFLQmdncWhrak9QUVFEQWdOSUFEQkZBaUJ6CkVJVGJ0SnQyOGw2Qzd1Rk9oK2hsY0NUYTd6dDNGRkdtSDlXMitISU5zUUloQU10YjNQdEh0VGtRWFBrRXdGZmsKRjZZK1pTQnRyc0UzQkpDU3VCUk8zbWh5Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K");
                    blockInfo.put("signature", "MEYCIQCVeUfSpXt0uLExeI7hlIXYc1gXntDCq2kUjTqcWxWEhAIhAMgeV/E3xfb86F5sK6J9vwO9Ow6SBt/l/ZUz2Lwakt5g");
                    blockInfo.put("txRoot", "C2N9i0LSm49vwEyyHWTu9G5zeHm0tpXXwVNn9PN+/2w=");
                    blocks.add(blockInfo);
                }
                System.out.println("-------ChainMaker最新十个区块信息查询完毕-------");
                JSONObject tenblocks = new JSONObject();
                tenblocks.put("tenBlocksInfo", blocks);
                queryNewBlock.setRet(ResultCode.SUCCESS);
                queryNewBlock.setData(tenblocks);
                return queryNewBlock;
            }
            try {
                SSHConfig.connect(ipAddress);
                logs = SSHConfig.executeCMD(
                        "cd " + cmcpath
                                + " && " + cmcexepath + " query block-by-height --chain-id=chain1 --sdk-conf-path="
                                + sdktype,
                        "UTF-8");
            } catch (Exception e) {
                System.out.println("SSH ERROR");
            }
            BigInteger height = JsonParser.parseString(logs).getAsJsonObject().getAsJsonObject("block")
                    .getAsJsonObject("header")
                    .get("block_height").getAsBigInteger();
            int j = 0;
            for (BigInteger i = height; i.compareTo(BigInteger.ZERO) >= 0; i = i.subtract(new BigInteger("1"))) {
                String blocklog = "";
                try {
                    SSHConfig.connect(ipAddress);
                    String cmd = "cd " + cmcpath
                            + " && " + cmcexepath + " query block-by-height "
                            + i.toString()
                            + " --chain-id=chain1 --sdk-conf-path=" + sdktype;
                    blocklog = SSHConfig.executeCMD(cmd, "UTF-8");
                } catch (Exception e) {
                    // System.out.println("SSH ERROR");
                    queryNewBlock.setData("Failed to connect to the blockchain node.");
                }

                // System.out.println("ChainMaker查询结束...");
                JsonObject headerJsonObj = JsonParser.parseString(blocklog).getAsJsonObject().getAsJsonObject("block")
                        .getAsJsonObject("header");
                BigInteger blockHeight = headerJsonObj.get("block_height").getAsBigInteger();
                String blockHash = headerJsonObj.get("block_hash").getAsString();
                BigInteger timeStamp = headerJsonObj.get("block_timestamp").getAsBigInteger();
                String dagHash = headerJsonObj.get("dag_hash").getAsString();
                BigInteger transactionCount = headerJsonObj.get("tx_count").getAsBigInteger();
                String previousBlockHash = headerJsonObj.get("pre_block_hash").getAsString();
                String rwSetRoot = headerJsonObj.get("rw_set_root").getAsString();
                String proposerMemberInfo = headerJsonObj.getAsJsonObject("proposer").get("member_info").getAsString();
                String signature = headerJsonObj.get("signature").getAsString();
                String txRoot = headerJsonObj.get("tx_root").getAsString();

                JSONObject blockInfo = new JSONObject();
                blockInfo.put("blockHeight", blockHeight);
                blockInfo.put("blockHash", blockHash);
                blockInfo.put("timeStamp", timeStamp);
                blockInfo.put("dagHash", dagHash);
                blockInfo.put("transactionCount", transactionCount);
                blockInfo.put("previousBlockHash", previousBlockHash);
                blockInfo.put("rwSetRoot", rwSetRoot);
                blockInfo.put("proposerMemberInfo", proposerMemberInfo);
                blockInfo.put("signature", signature);
                blockInfo.put("txRoot", txRoot);

                j++;
                blocks.add(blockInfo);

                if (j == 10)
                    break;
            }
            System.out.println("-------ChainMaker最新十个区块信息查询完毕-------");
        } else if (portNumber.equals(CrosschainInfo.H2C.ChainPort)) {

            if (!BlockchainConfig.do_update_blockchain){
                for (int i = 1000;i<1000+topN;i++){
                    JSONObject blockInfo = new JSONObject();
                    blockInfo.put("blockHeight", i);
                    blockInfo.put("blockHash", "7b3c09a47df76ff4038f5e0eeadf2c297523be60e1297fe95ff49f2ebb6e93c7");
                    blockInfo.put("timeStamp", "2024-06-03T06:38:04.6022327Z");
                    blockInfo.put("blockSize", 1000);
                    blockInfo.put("transactionCount", 2);
                    blockInfo.put("previousBlockHash", "18c4d913ead7141b7e384e839e73af589a646320ce36bde2d3f64494a453fa31");
                    blockInfo.put("merkleTreeRootOfWorldState", "1480da1a74d0e1e2c12dfe3ca5d1340dedb3b1da7c12144bdfb180dda2ea7a09");
                    blockInfo.put("merkleTreeRootOfTransactions", "0ce2bd745a9272b1fcf74f93af9d37c403a4fe7d0d134170e673f030ce1dfa18");
                    blockInfo.put("merkleTreeRootOfTransactionState", "a06f9852bc672980fd197ac424eac1116e51cdf891d11fa5dc472827d1521d1b");
                    blockInfo.put("signerPubkey", "0485fa02af56f8f47b8e69197b8d7493e680fc5c64f34fe205e297754ac854656c7609644e1dad02858222c3d043f519cd6bbb065c8547566caeb80f79c087ba18");
                    blocks.add(blockInfo);
                }
                System.out.println("-------H2Chain最新十个区块信息查询完毕-------");
                JSONObject tenblocks = new JSONObject();
                tenblocks.put("tenBlocksInfo", blocks);
                queryNewBlock.setRet(ResultCode.SUCCESS);
                queryNewBlock.setData(tenblocks);
                return queryNewBlock;
            }

            // String targetUrl = "http://116.204.36.31:8000/api/blockChain/blockHeight";
            BigInteger height = new BigInteger("0");
            try {
                URL url = new URL("http://" + ipAddress + ":8000/api/blockChain/blockHeight");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                height = BigInteger.valueOf(Long.parseLong(response.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            int j = 0;
            for (BigInteger i = height; i.compareTo(BigInteger.ZERO) >= 0; i = i.subtract(new BigInteger("1"))) {
                String targetUrl = "http://" + ipAddress + ":8000/api/blockChain/blockByHeight?blockHeight="
                        + i.toString() + "&includeTransactions=true";
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
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // System.out.println("H2hain查询结束...");
                JsonObject headerJsonObj = JsonParser.parseString(logs).getAsJsonObject().getAsJsonObject("Header");
                BigInteger blockHeight = headerJsonObj.get("Height").getAsBigInteger();
                String blockHash = JsonParser.parseString(logs).getAsJsonObject().get("BlockHash").getAsString();
                String timeStamp = headerJsonObj.get("Time").getAsString();
                BigInteger blockSize = JsonParser.parseString(logs).getAsJsonObject().get("BlockSize")
                        .getAsBigInteger();
                BigInteger transactionCount = JsonParser.parseString(logs).getAsJsonObject().getAsJsonObject("Body")
                        .get("TransactionsCount").getAsBigInteger();
                String previousBlockHash = headerJsonObj.get("PreviousBlockHash").getAsString();
                String merkleTreeRootOfWorldState = headerJsonObj.get("MerkleTreeRootOfWorldState").getAsString();
                String merkleTreeRootOfTransactions = headerJsonObj.get("MerkleTreeRootOfTransactions").getAsString();
                String merkleTreeRootOfTransactionState = headerJsonObj.get("MerkleTreeRootOfTransactionState")
                        .getAsString();
                String signerPubkey = headerJsonObj.get("SignerPubkey").getAsString();

                JSONObject blockInfo = new JSONObject();
                blockInfo.put("blockHeight", blockHeight);
                blockInfo.put("blockHash", blockHash);
                blockInfo.put("timeStamp", timeStamp);
                blockInfo.put("blockSize", blockSize);
                blockInfo.put("transactionCount", transactionCount);
                blockInfo.put("previousBlockHash", previousBlockHash);
                blockInfo.put("merkleTreeRootOfWorldState", merkleTreeRootOfWorldState);
                blockInfo.put("merkleTreeRootOfTransactions", merkleTreeRootOfTransactions);
                blockInfo.put("merkleTreeRootOfTransactionState", merkleTreeRootOfTransactionState);
                blockInfo.put("signerPubkey", signerPubkey);

                j++;
                blocks.add(blockInfo);
                if (j == 10)
                    break;
            }
            System.out.println("-------H2Chain最新十个区块信息查询完毕-------");
        } else if (portNumber.equals(CrosschainInfo.BuB.ChainPort)) {
            BigInteger height = new BigInteger("0");
            if (!BlockchainConfig.do_update_blockchain) {
                for (int i = 1000;i<1000+topN;i++) {
                    JSONObject blockInfo = new JSONObject();
                    blockInfo.put("blockHeight", i);
                    blockInfo.put("accountTreeHash", "5ac1e2a07e9b307bae593ea25304ef5bc9e0194d029166209081fed636a11c99");
                    blockInfo.put("closeTime", "1741055502664394");
                    blockInfo.put("consensusValueHash", "ec53a7a19defe45807bf64d293f27e5f08af26bf938b8080a0ac4a9113a03a24");
                    blockInfo.put("feesHash", "916daa78d264b3e2d9cff8aac84c943a834f49a62b7354d4fa228dab65515313");
                    blockInfo.put("hash", "acf3b87e82406ec8a83a5da8f57d49e2bbd43d0656794e601af9075c909b9327");
                    blockInfo.put("previousHash", "504ca3084b68bbdcfd29821a9d9df6a23a86fd3ddf0770bbbb90caf0ee657e79");
                    blockInfo.put("validatorsHash", "ebdafd9f939fdea9b87bb9696fcac909db1ce1e581a222b0b7f07f9e1ff03d11");
                    blockInfo.put("version", "4003");
                    blocks.add(blockInfo);
                }
                System.out.println("-------Bubi最新十个区块信息查询完毕-------");
                JSONObject tenblocks = new JSONObject();
                tenblocks.put("tenBlocksInfo", blocks);
                queryNewBlock.setRet(ResultCode.SUCCESS);
                queryNewBlock.setData(tenblocks);
                return queryNewBlock;
            }
            try {
                URL url = new URL("http://" + ipAddress + ":19333/getLedger");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JsonObject headerJsonObj = JsonParser.parseString(response.toString()).getAsJsonObject()
                        .getAsJsonObject("result")
                        .getAsJsonObject("header");
                height = headerJsonObj.get("seq").getAsBigInteger();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int j = 0;
            for (BigInteger i = height; i.compareTo(BigInteger.ZERO) >= 0; i = i.subtract(new BigInteger("1"))) {
                String targetUrl = "http://" + ipAddress + ":19333/getLedger?seq=" + i.toString();
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
                    JsonObject headerJsonObj = JsonParser.parseString(response.toString()).getAsJsonObject()
                            .getAsJsonObject("result")
                            .getAsJsonObject("header");

                    BigInteger blockHeight = headerJsonObj.get("seq").getAsBigInteger();
                    String accountTreeHash = headerJsonObj.get("account_tree_hash").getAsString();
                    String closeTime = headerJsonObj.get("close_time").getAsString();
                    String consensusValueHash = headerJsonObj.get("consensus_value_hash").getAsString();
                    String feesHash = headerJsonObj.get("fees_hash").getAsString();
                    String hash = headerJsonObj.get("hash").getAsString();
                    String previousHash = headerJsonObj.get("previous_hash").getAsString();
                    String validatorsHash = headerJsonObj.get("validators_hash").getAsString();
                    String version = headerJsonObj.get("version").getAsString();

                    JSONObject blockInfo = new JSONObject();
                    blockInfo.put("blockHeight", blockHeight);
                    blockInfo.put("accountTreeHash", accountTreeHash);
                    blockInfo.put("closeTime", closeTime);
                    blockInfo.put("consensusValueHash", consensusValueHash);
                    blockInfo.put("feesHash", feesHash);
                    blockInfo.put("hash", hash);
                    blockInfo.put("previousHash", previousHash);
                    blockInfo.put("validatorsHash", validatorsHash);
                    blockInfo.put("version", version);

                    j++;
                    blocks.add(blockInfo);
                    if (j == 10)
                        break;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("-------BuBi最新十个区块信息查询完毕-------");
        } else if (portNumber.equals(liantongname)) {
//            String targetUrl = "https://121.37.119.118:8443/api/baas/explorer/unicom/blockchains/4e8776c142f845e589bd00db6448d449/channels/ch1/blocks";
//            String authorizationToken = chainreq.getAuthorizationToken();
//
//            String logs = "";
//
//            BigInteger height = new BigInteger("0");
//            try {
//                // 禁用 SSL 验证
//                disableSSLVerification();
//
//                // 创建URL对象
//                URL url = new URL(targetUrl);
//                // 打开连接
//                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//
//                // 设置Authorization头部
//                connection.setRequestProperty("authorization", authorizationToken);
//
//                // 读取响应数据
//                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//
//                height = JsonParser.parseString(response.toString()).getAsJsonObject()
//                        .get("total_block_count").getAsBigInteger().subtract(BigInteger.valueOf(1));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            targetUrl = "https://121.37.119.118:8443/api/baas/explorer/unicom/blockchains/4e8776c142f845e589bd00db6448d449/channels/ch1/blocks?start_block_num="
//                    + height + "&block_count=10";
//            try {
//                // 禁用 SSL 验证
//                disableSSLVerification();
//
//                // 创建URL对象
//                URL url = new URL(targetUrl);
//                // 打开连接
//                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//
//                // 设置Authorization头部
//                connection.setRequestProperty("authorization", authorizationToken);
//
//                // 读取响应数据
//                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//
//                logs = response.toString();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            BigInteger totalNumber = JsonParser.parseString(logs).getAsJsonObject().get("block_count")
//                    .getAsBigInteger();
//            for (BigInteger i = BigInteger.valueOf(0); i.compareTo(totalNumber) < 0; i = i.add(BigInteger.ONE)) {
//                JsonObject rawBlock = JsonParser.parseString(logs).getAsJsonObject().getAsJsonArray("blocks")
//                        .get(i.intValue())
//                        .getAsJsonObject();
//                String channelName = rawBlock.get("channel_name").getAsString();
//                String hash = rawBlock.get("hash").getAsString();
//                BigInteger number = rawBlock.get("number").getAsBigInteger();
//                String dataHash = rawBlock.get("data_hash").getAsString();
//                String previousHash = rawBlock.get("previous_hash").getAsString();
//                String nextHash = rawBlock.get("next_hash").getAsString();
//                if (nextHash.equals(""))
//                    nextHash = "null";
//                BigInteger lastConfigIndex = rawBlock.get("last_config_index").getAsBigInteger();
//                BigInteger txCount = rawBlock.get("tx_count").getAsBigInteger();
//                String createdAt = rawBlock.get("created_at").getAsString();
//
//                JSONObject blockInfo = new JSONObject();
//                blockInfo.put("channelName", channelName);
//                blockInfo.put("hash", hash);
//                blockInfo.put("number", number);
//                blockInfo.put("dataHash", dataHash);
//                blockInfo.put("previousHash", previousHash);
//                blockInfo.put("nextHash", nextHash);
//                blockInfo.put("lastConfigIndex", lastConfigIndex);
//                blockInfo.put("txCount", txCount);
//                blockInfo.put("createdAt", createdAt);
//                blocks.add(blockInfo);
//            }
//
//            System.out.println("-------联通链最新十个区块信息查询完毕-------");
        } else if (portNumber.equals(CrosschainInfo.FBC.ChainPort)){
            if (!BlockchainConfig.do_update_blockchain) {
                for (int i = 1000;i<1000+topN;i++) {
                    JSONObject blockInfo = new JSONObject();
                    blockInfo.put("gasLimit", "0x0");
                    blockInfo.put("gasUsed", "0x0");
                    blockInfo.put("hash", "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
                    blockInfo.put("number", i);
                    blockInfo.put("parentHash", "0xeccad5274949b9d25996f7a96b89c0ac5c099eb9b72cc00d65bc6ef09f7bd10b");
                    blockInfo.put("sealer", "0x0");

                    String[] stringArray = {"0471101bcf033cd9e0cbd6eef76c144e6eff90a7a0b1847b5976f8ba32b2516c0528338060a4599fc5e3bafee188bca8ccc529fbd92a760ef57ec9a14e9e4278",
                            "2b08375e6f876241b2a1d495cd560bd8e43265f57dc9ed07254616ea88e371dfa6d40d9a702eadfd5e025180f9d966a67f861da214dd36237b58d72aaec2e108",
                            "cf93054cf524f51c9fe4e9a76a50218aaa7a2ca6e58f6f5634f9c2884d2e972486c7fe1d244d4b49c6148c1cb524bcc1c99ee838bb9dd77eb42f557687310ebd",
                            "ed1c85b815164b31e895d3f4fc0b6e3f0a0622561ec58a10cc8f3757a73621292d88072bf853ac52f0a9a9bbb10a54bdeef03c3a8a42885fe2467b9d13da9dec"};
                    List<String> stringList = Arrays.asList(stringArray);
                    JSONArray s = new JSONArray(stringList);

                    blockInfo.put("sealerList", s);
                    blockInfo.put("stateRoot", "0x9711819153f7397ec66a78b02624f70a343b49c60bc2f21a77b977b0ed91cef9");
                    blockInfo.put("timestamp", "0x1692f119c84");
                    blockInfo.put("transactionsRoot","0x516787f85980a86fd04b0e9ce82a1a75950db866e8cdf543c2cae3e4a51d91b7");

                    String[] txArray = {"0xa14638d47cc679cf6eeb7f36a6d2a30ea56cb8dcf0938719ff45023a7a8edb5d",
                            "0xa14638d47cc679cf6eeb7f36a6d2a30ea56cb8dcf0938719ff45023a7a8edb5d"
                    };
                    List<String> txList = Arrays.asList(txArray);
                    JSONArray txs = new JSONArray(txList);

                    blockInfo.put("transactions",txs);
                    blocks.add(blockInfo);
                }
                System.out.println("-------Fisco Bcos最新十个区块信息查询完毕-------");
                JSONObject tenblocks = new JSONObject();
                tenblocks.put("tenBlocksInfo", blocks);
                queryNewBlock.setRet(ResultCode.SUCCESS);
                queryNewBlock.setData(tenblocks);
                return queryNewBlock;
            }
        }else if(portNumber.equals(CrosschainInfo.FAB.ChainPort)){
            if (!BlockchainConfig.do_update_blockchain) {
                for (int i = 1000;i<1000+topN;i++) {
                    JSONObject blockInfo = new JSONObject();
                    blockInfo.put("BlockNumber", i);
                    blockInfo.put("BlockHash", "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b");
                    blockInfo.put("PreviousHash", "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363a");
                    blockInfo.put("TransactionCount", 2);
                    JSONArray txs = new JSONArray();
                    for (int j = 0; j < 2; j++) {
                        JSONObject t = new JSONObject();
                        t.put("TransactionID", "tx123456");
                        t.put("Type", "ENDORSER_TRANSACTION");
                        t.put("Timestamp", "2025-07-06T12:34:56Z");
                        txs.add(t);
                    }
                    blockInfo.put("transactions", txs);
                    blocks.add(blockInfo);
                }
                System.out.println("-------Fabric最新十个区块信息查询完毕-------");
                JSONObject tenblocks = new JSONObject();
                tenblocks.put("tenBlocksInfo", blocks);
                queryNewBlock.setRet(ResultCode.SUCCESS);
                queryNewBlock.setData(tenblocks);
                return queryNewBlock;
            }
        }
        JSONObject tenblocks = new JSONObject();
        tenblocks.put("tenBlocksInfo", blocks);
        queryNewBlock.setRet(ResultCode.SUCCESS);
        queryNewBlock.setData(tenblocks);

        return queryNewBlock;
    }

    private static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // 设置SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

            // 设置默认的SSLSocketFactory
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            // 设置默认的HostnameVerifier，跳过hostname验证
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CommonResp checkTxInfo(TxhashReq txhashreq) { ///////////////
        CommonResp queryTxInfoResp = new CommonResp();
        int colonIndex = txhashreq.getChainIP().indexOf(":");
        String ipAddress = txhashreq.getChainIP().substring(0, colonIndex);
        String portNumber = txhashreq.getChainIP().substring(colonIndex + 1);

        QueryWrapper<Chain> wrapper = new QueryWrapper<>();
        wrapper.select("chain_type");
        wrapper.eq("ip_address", ipAddress);
        wrapper.eq("port", portNumber);
        Chain chain = chainMapper.selectOne(wrapper);
        String chainType = chain.getChainType();

        if (chainType.equals(ethname)) {
            Web3j web3j = Web3j.build(new HttpService("http://" + txhashreq.getChainIP()));
            try {

                EthTransaction transaction = web3j.ethGetTransactionByHash(txhashreq.getTxHASH()).sendAsync().get();

                String txHash = txhashreq.getTxHASH();
                String blockHash = transaction.getTransaction().get().getBlockHash();
                BigInteger blockNumber = transaction.getTransaction().get().getBlockNumber();
                String from = transaction.getTransaction().get().getFrom();
                BigInteger gas = transaction.getTransaction().get().getGas();
                BigInteger gasPrice = transaction.getTransaction().get().getGasPrice();
                BigInteger nonce = transaction.getTransaction().get().getNonce();
                String to = transaction.getTransaction().get().getTo();
                BigInteger value = transaction.getTransaction().get().getValue();
                String input = transaction.getTransaction().get().getInput();

                JSONObject txInfo = new JSONObject();
                txInfo.put("txHash", txHash);
                txInfo.put("blockHash", blockHash);
                txInfo.put("blockNumber", blockNumber);
                txInfo.put("from", from);
                txInfo.put("gas", gas);
                txInfo.put("gasPrice", gasPrice);
                txInfo.put("nonce", nonce);
                txInfo.put("to", to);
                txInfo.put("value", value);
                txInfo.put("input", input);

                queryTxInfoResp.setRet(ResultCode.SUCCESS);
                queryTxInfoResp.setData(txInfo);
            } catch (InterruptedException | ExecutionException e) {
                queryTxInfoResp.setData("Failed to connect to the blockchain node.");
            }
            System.out.println("-------ETH交易信息查询完毕-------");
        } else if (chainType.equals(chainmakername)) {
            String logs = "";
            try {
                SSHConfig.connect(ipAddress);
                String cmd = "cd " + cmcpath
                        + " && " + cmcexepath + " query tx " + txhashreq.getTxHASH()
                        + " --chain-id=chain1 --sdk-conf-path=" + sdktype;
                logs = SSHConfig.executeCMD(cmd, "UTF-8");
            } catch (Exception e) {
                System.out.println("SSH ERROR");
            }

            // System.out.println("ChainMaker查询结束...");
            JsonObject tx = JsonParser.parseString(logs).getAsJsonObject().get("transaction").getAsJsonObject();
            JsonObject result = tx.getAsJsonObject("result");
            JsonObject sender = tx.getAsJsonObject("sender");

            BigInteger blockHeight = JsonParser.parseString(logs).getAsJsonObject().get("block_height")
                    .getAsBigInteger();
            BigInteger blockTimestamp = JsonParser.parseString(logs).getAsJsonObject().get("block_timestamp")
                    .getAsBigInteger();
            BigInteger timestamp = tx.getAsJsonObject("payload").get("timestamp").getAsBigInteger();
            String rwSetHash = result.get("rw_set_hash").getAsString();
            String signature = sender.get("signature").getAsString();
            String signer = sender.getAsJsonObject("signer").get("member_info").getAsString();
            String contractName = tx.getAsJsonObject("payload").get("contract_name").getAsString();
            String method = tx.getAsJsonObject("payload").get("method").getAsString();

            JSONObject txInfo = new JSONObject();
            txInfo.put("txHash", txhashreq.getTxHASH());
            txInfo.put("blockHeight", blockHeight);
            txInfo.put("blockTimestamp", blockTimestamp);
            txInfo.put("timestamp", timestamp);
            txInfo.put("rwSetHash", rwSetHash);
            txInfo.put("signature", signature);
            txInfo.put("signer", signer);
            txInfo.put("contractName", contractName);
            txInfo.put("method", method);

            queryTxInfoResp.setRet(ResultCode.SUCCESS);
            queryTxInfoResp.setData(txInfo);

            System.out.println("-------ChainMaker交易信息查询完毕-------");
        } else {
            String targetUrl = "http://" + ipAddress + ":8000/api/blockChain/transactionResult?transactionId="
                    + txhashreq.getTxHASH();

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
            } catch (IOException e) {
                e.printStackTrace();
            }

            // System.out.println("H2hain查询结束...");
            JsonObject jsonObj = JsonParser.parseString(logs).getAsJsonObject();
            JsonObject txObj = jsonObj.getAsJsonObject("Transaction");

            // BigInteger transactionId = jsonObj.get("TransactionId").getAsBigInteger();
            String status = jsonObj.get("Status").getAsString();
            BigInteger blockNumber = jsonObj.get("BlockNumber").getAsBigInteger();
            // String blockHash = jsonObj.get("BlockHash").getAsString();
            String returnValue = jsonObj.get("ReturnValue").getAsString();
            // String error = jsonObj.get("Error").getAsString();
            BigInteger transactionSize = jsonObj.get("TransactionSize").getAsBigInteger();
            String from = txObj.get("From").getAsString();
            String to = txObj.get("To").getAsString();
            BigInteger refBlockNumber = txObj.get("RefBlockNumber").getAsBigInteger();
            String refBlockPrefix = txObj.get("RefBlockPrefix").getAsString();
            String methodName = txObj.get("MethodName").getAsString();
            String params = txObj.get("Params").getAsString();
            String signature = txObj.get("Signature").getAsString();

            JSONObject txInfo = new JSONObject();
            txInfo.put("txHash", txhashreq.getTxHASH());
            txInfo.put("status", status);
            txInfo.put("blockNumber", blockNumber);
            txInfo.put("returnValue", returnValue);
            txInfo.put("transactionSize", transactionSize);
            txInfo.put("from", from);
            txInfo.put("to", to);
            txInfo.put("refBlockNumber", refBlockNumber);
            txInfo.put("refBlockPrefix", refBlockPrefix);
            txInfo.put("methodName", methodName);
            txInfo.put("params", params);
            txInfo.put("signature", signature);

            queryTxInfoResp.setRet(ResultCode.SUCCESS);
            queryTxInfoResp.setData(txInfo);

            System.out.println("-------H2Chain交易信息查询完毕-------");
        }

        return queryTxInfoResp;
    }

    //
    // /**
    // * 写一个结构体包括两个参数，blochhash和chainip
    // **/
    // public CommonResp checkBlockInfo(BlockhashReq blockhashreq) {
    // CommonResp queryBlockInfoResp = new CommonResp();
    //// 用哈希查询
    // // 查询指定区块哈希的区块信息
    // Web3j web3j = Web3j.build(new HttpService(blockhashreq.getChainIP())); //
    // 替换为你的节点地址
    // try {
    // EthBlock.Block block = web3j.ethGetBlockByHash(blockhashreq.getBlockHASH(),
    // false)
    // .send()
    // .getBlock();
    //
    // BigInteger timestamp = block.getTimestamp();
    // BigInteger blockSize = block.getSize();
    // //BigInteger transactionCount =
    // BigInteger.valueOf(block.getTransactions().size());
    // String previousBlockAddress = block.getParentHash();
    // BigInteger gasLimit = block.getGasLimit();
    // // 获取Gas消耗
    // BigInteger gasUsed = block.getGasUsed();
    // // 获取状态哈希
    // String stateRoot = block.getStateRoot();
    // // 获取总难度
    // BigInteger totalDifficulty = block.getTotalDifficulty();
    // // 获取难度
    // BigInteger difficulty = block.getDifficulty();
    // // 获取奖励
    // BigInteger blockReward = block.getGasUsed().multiply(block.getGasLimit());
    // // 获取矿工
    // String minerAddress = block.getMiner();
    //
    // // 获取交易数量
    // int transactionCount = block.getTransactions().size();
    //
    // JSONObject blockInfo = new JSONObject();
    // blockInfo.put("blockHash", blockhashreq.getBlockHASH());
    // blockInfo.put("timestamp", timestamp);
    // blockInfo.put("blockSize", blockSize);
    // blockInfo.put("transactionCount", transactionCount);//
    // blockInfo.put("previousBlockAddress", previousBlockAddress);
    // blockInfo.put("gasLimit", gasLimit);
    // blockInfo.put("gasUsed", gasUsed);
    // blockInfo.put("stateRoot", stateRoot);
    // blockInfo.put("totalDifficulty", totalDifficulty);
    // blockInfo.put("difficulty", difficulty);
    // blockInfo.put("blockReward", blockReward);
    // blockInfo.put("minerAddress", minerAddress);
    //
    // queryBlockInfoResp.setRet(ResultCode.SUCCESS);
    // queryBlockInfoResp.setData(blockInfo);
    // } catch (IOException e) {
    // // queryBlockInfoResp.setRet(ResultCode.INTERNAL_SERVER_ERROR);
    // queryBlockInfoResp.setData("Failed to connect to the blockchain node.");
    // }
    //
    // return queryBlockInfoResp;
    // }

    // public CommonResp addChain(AddChainReq addchainreq) { //暂时不需要
    // CommonResp addchainresp = new CommonResp();
    // // 将填入的信息添加到新的对象中
    // Chain chain = new Chain()
    // .setChainIp(addchainreq.getUploadchainIP())
    // .setChainType(addchainreq.getChainTYPE())
    // .setOwnerId(addchainreq.getOwnerID());
    // //.setIsProcessed(false);
    // // 查询自增id下一个是多少
    // QueryWrapper<Chain> wrapper = new QueryWrapper<>();
    // wrapper.select("chain_id");
    // List<Chain> blockchain = chainMapper.selectList(wrapper);
    // Long maxId = Long.valueOf(0);
    // for (int i = 0; i < blockchain.size(); i++) {
    // Long perId = blockchain.get(i).getChainId();
    // if (perId > maxId) maxId = perId;
    // }
    // maxId = maxId + 1;
    //
    // // 构造json
    // JSONObject chaininfo = new JSONObject();
    // chaininfo.put("UploadchainIP", chain.getChainIp());
    // chaininfo.put("getChainTYPE", chain.getChainType());
    // chaininfo.put("getOwnerID", chain.getOwnerId());
    //
    //
    // chaininfo.put("chainId", maxId);
    //
    // int insert = this.chainMapper.insert(chain);
    //
    // addchainresp.setRet(ResultCode.SUCCESS);
    // addchainresp.setData("add successfully!");
    // return addchainresp;
    // }

    /*
     * try {
     * 
     * QueryWrapper<Chain> wrapper = new QueryWrapper<>();
     * wrapper.eq("chain_id", chainId);
     * List<Chain> chains = chainMapper.selectList(wrapper);
     * Web3j web3j = Web3j.build(new HttpService("http://localhost:8545"));
     * 
     * /**先把链url与web3j相连接——在通过对创造的web3j实例对私连接点进行交互
     * 
     * 
     * } catch (Exception e) {
     * System.out.println("SSH ERROR");
     * }
     */

    /*
     * public CommonResp checkChain(){
     * ChainClient chainClient = new ChainClient();
     * CommonResp querychainresp = new CommonResp();
     * QueryWrapper<Chain> wrapper = new QueryWrapper<>();
     * wrapper.select("chain_ip");
     * List<Chain> chain = chainMapper.selectList(wrapper);
     * 
     * 
     * Discovery.ChainInfo chaininfo = chainClient.getChainInfo(10000);
     * querychainresp.setRet(ResultCode.SUCCESS);
     * querychainresp.setData(chaininfo);
     * return querychainresp;
     * }
     */
    //
    // public CommonResp checkChainmaker(ChainMakerReq chainmakerreq)
    // {
    //
    // ChainClient chainClient =new ChainClient();
    // ChainConfigOuterClass.ChainConfig chainConfig = null;
    // CommonResp chainmakerkInfoResp = new CommonResp();
    // try {
    // chainConfig = chainClient.getChainConfig(20000);
    //
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // chainmakerkInfoResp.setData(chainConfig);
    // return chainmakerkInfoResp;
    // }
    /// **链跨链尝试**/
    //
    // public CommonResp ChainmakerTxInfo(ChainMakerReq chainmakerreq)
    // {
    // ChainClient chainClient =new ChainClient();
    //
    // ChainmakerTransaction.TransactionInfo chainmakertx=null;
    // CommonResp chainmakerkTxResp = new CommonResp();
    //
    // try {
    // chainmakertx = chainClient.getTxByTxId(chainmakerreq.getTxId(),20000);
    //
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // chainmakerkTxResp.setData(chainmakertx);
    // return chainmakerkTxResp;
    // }
    // public CommonResp ChainmakerBlockInfo(ChainMakerReq chainmakerreq)
    // {
    // ChainClient chainClient =new ChainClient();
    //
    // long chainmakerblockhash = 0;
    // CommonResp chainmakerkTxResp = new CommonResp();
    // try {
    // chainmakerblockhash=chainClient.getCurrentBlockHeight(20000);
    // }catch (Exception e) {
    // e.printStackTrace();
    // }
    // chainmakerkTxResp.setData(chainmakerblockhash);
    // return chainmakerkTxResp;
    // }
    // public CommonResp ChainmakerNewBlock(ChainMakerReq chainmakerreq)
    // {
    // ChainClient chainClient =new ChainClient();
    //
    // //long chainmakerblockheight = 0;
    // CommonResp chainmakerkTxResp = new CommonResp();
    // try {
    // //chainmakerblockheight=chainClient.getBlockHeightByBlockHash(chainmakerreq.getBlockHash(),20000);
    // chainmakerkTxResp.setData(chainClient.getBlockHeightByBlockHash(chainmakerreq.getBlockHash(),20000));
    // }catch (Exception e) {
    // e.printStackTrace();
    // }
    // //chainmakerkTxResp.setData(chainmakerblockheight);
    // return chainmakerkTxResp;
    // }

    // 清空数据库中的内容

}
