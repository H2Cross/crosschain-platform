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

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;

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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.SSLContext;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import java.nio.file.Path;
import java.nio.file.Files;
// import org.hyperledger.fabric.gateway.Identities;

import java.io.File;
import java.nio.file.Paths;
// import org.hyperledger.fabric.gateway.Gateway;
// import org.hyperledger.fabric.gateway.Network;
// import org.hyperledger.fabric.gateway.Wallet;
// import org.hyperledger.fabric.gateway.Wallets;

import org.apache.commons.codec.binary.Hex;
// import org.hyperledger.fabric.sdk.*;

/* ==========  gRPC / Fabric-Gateway SDK  ========== */
import io.grpc.Grpc;
import io.grpc.TlsChannelCredentials;
// import org.hyperledger.fabric.client.*;
// import org.hyperledger.fabric.client.identity.Identities;
// import org.hyperledger.fabric.client.identity.Identity;
// import org.hyperledger.fabric.client.identity.Signer;
// import org.hyperledger.fabric.client.identity.Signers;
// import org.hyperledger.fabric.client.identity.X509Identity;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

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

    private static String cmcpath = "~/CIPS-Gemini-v1/CIPS-Gemini-ChainMaker/chainmaker/chainmaker-go/tools/cmc";
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

        QueryWrapper<Chain> wrapper = new QueryWrapper<>();
        wrapper.select("chain_id", "ip_address", "port", "chain_type");
        List<Chain> chains = chainMapper.selectList(wrapper);

        JSONObject totalInfo = new JSONObject();
        JSONArray chainsInfo = new JSONArray();

        for (CrosschainInfo chain : CrosschainInfo.values()) {
            JSONObject singleChain = new JSONObject();
            // JsonObject singleChain = new JsonObject();
            singleChain.put("Id", chain.ChainName);
            singleChain.put("Name", chain.ChineseName);
            JSONArray children = new JSONArray();
            for (Chain c : chains) {
                if (c.getChainType().equals(chain.ChineseName)) {
                    JSONObject single = new JSONObject();
                    single.put("Id", c.getChainId());
                    single.put("Name", c.getIpAddress() + ":" + String.valueOf(c.getPort()));

                    JSONObject parent = new JSONObject();
                    parent.put("Id", chain.ChainName);
                    parent.put("Name", chain.ChineseName);
                    single.put("parent", parent);
                    children.add(single);
                }

            }
            singleChain.put("children", children);
            chainsInfo.add(singleChain);
        }

        // String ip = "192.168.0.";

        // int chainsize = 5;
        //
        //// JSONObject totalInfo = new JSONObject();
        //// JSONArray chainsInfo = new JSONArray();
        //
        // int x = 0;
        // for (CrosschainInfo chain : CrosschainInfo.values()) {
        // JSONObject singleChain = new JSONObject();
        // // JsonObject singleChain = new JsonObject();
        // singleChain.put("Id", chain.ChainName);
        // singleChain.put("Name", chain.ChineseName);
        // JSONArray children = new JSONArray();
        // for (int i = 0; i <= chainsize; i++) {
        // JSONObject single = new JSONObject();
        // single.put("Id", chain.ChainId + i);
        // if (i==chainsize){
        // single.put("Name", "1.92.88.254" + ":" + String.valueOf(chain.ChainPort));
        // } else {
        // single.put("Name", ip + String.valueOf(i + 1) + ":" +
        // String.valueOf(chain.ChainPort));
        // }
        //
        // JSONObject parent = new JSONObject();
        // parent.put("Id", chain.ChainName);
        // parent.put("Name", chain.ChineseName);
        // single.put("parent", parent);
        // children.add(single);
        // }
        // x++;
        // singleChain.put("children", children);
        // chainsInfo.add(singleChain);
        // }
        totalInfo.put("totalInfo", chainsInfo);
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
            // String targetUrl =
            // "https://121.37.119.118:8443/api/baas/explorer/unicom/blockchains/4e8776c142f845e589bd00db6448d449/channels/ch1/blocks";
            // String authorizationToken = chainreq.getAuthorizationToken();
            //
            // String logs = "";
            //
            // try {
            // // 禁用 SSL 验证
            // disableSSLVerification();
            //
            // // 创建URL对象
            // URL url = new URL(targetUrl);
            // // 打开连接
            // HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            // connection.setRequestMethod("GET");
            //
            // // 设置Authorization头部
            // connection.setRequestProperty("authorization", authorizationToken);
            //
            // // 读取响应数据
            // BufferedReader in = new BufferedReader(new
            // InputStreamReader(connection.getInputStream()));
            // String inputLine;
            // StringBuilder response = new StringBuilder();
            // while ((inputLine = in.readLine()) != null) {
            // response.append(inputLine);
            // }
            // in.close();
            //
            // BigInteger height =
            // JsonParser.parseString(response.toString()).getAsJsonObject()
            // .get("total_block_count").getAsBigInteger().subtract(BigInteger.valueOf(1));
            // JSONObject heightinfo = new JSONObject();
            // heightinfo.put("heightinfo", height);
            // chainresp.setData(heightinfo);
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            // } else if (portNumber.equals(fabricname)) {
            // try {
            // /* ====== 1. 证书路径（按你本机实际改） ====== */
            // Path cryptoPath =
            // Paths.get("D:\\桌面\\fabric-samples-main\\test-network\\organizations\\peerOrganizations\\org1.example.com");
            // Path certFile =
            // cryptoPath.resolve("users/User1@org1.example.com\\msp\\signcerts\\cert.pem");
            // Path keyFile =
            // cryptoPath.resolve("users/User1@org1.example.com\\msp\\keystore\\*_sk"); //
            // 真实文件名
            // Path tlsFile =
            // cryptoPath.resolve("peers\\peer0.org1.example.com\\tls\\ca.crt");

            // /* ====== 2. 构建 Identity（2.2.9 Wallet 入口） ====== */
            // X509Certificate certificate =
            // Identities.readX509Certificate(Files.newBufferedReader(certFile));
            // PrivateKey privateKey =
            // Identities.readPrivateKey(Files.newBufferedReader(keyFile));
            // Wallet wallet = Wallets.newInMemoryWallet(); // 2.2.9 唯一入口
            // wallet.put("user1", Identities.newX509Identity("Org1MSP", certificate,
            // privateKey)); // 三参重载

            // /* ====== 3. gRPC 连接 ====== */
            // ManagedChannel channel = NettyChannelBuilder.forTarget(ipAddress + ":" +
            // portNumber)
            // .sslContext(GrpcSslContexts.forClient()
            // .trustManager(tlsFile.toFile())
            // .build())
            // .overrideAuthority("peer0.org1.example.com")
            // .build();

            // /* ====== 4. 创建 Gateway（2.2.9 链式 Builder） ====== */
            // try (Gateway gateway = Gateway.create()
            // .wallet(wallet) // 2.2.9 推荐入口
            // .identity("user1") // wallet 里别名
            // .connection(channel)
            // .evaluateOptions(o -> o.withDeadlineAfter(5, TimeUnit.SECONDS))
            // .build()) {

            // /* ====== 5. 查询最新区块高度 ====== */
            // Network network = gateway.getNetwork("mychannel");
            // BigInteger blockNumber = network.getBlockNumber(); // SDK 已封装

            // JSONObject heightinfo = new JSONObject();
            // heightinfo.put("heightinfo", blockNumber);
            // chainresp.setData(heightinfo);

            // } finally {
            // channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            // }

            // } catch (Exception e) {
            // e.printStackTrace();
            // chainresp.setData("Error retrieving Fabric block height.");
            // }
            // }

        } else if (portNumber.equals(fabricname)) {
            // try {
            // /* 1. 基础配置 */
            // String cryptoRoot = System.getProperty("user.home")
            // + "/crosschain/fabric-samples-main/test-network";
            // String mspId = "Org1MSP";
            // String channelName = "mychannel";

            // /* 2. 路径 */
            // Path cryptoPath = Paths.get(cryptoRoot,
            // "organizations/peerOrganizations/org1.example.com");
            // Path certFile = cryptoPath.resolve(
            // "users/User1@org1.example.com/msp/signcerts/cert.pem");
            // Path keyDir = cryptoPath.resolve(
            // "users/User1@org1.example.com/msp/keystore");
            // Path tlsCert = cryptoPath.resolve(
            // "peers/peer0.org1.example.com/tls/ca.crt");

            // /* 3. identity & signer */
            // Identity identity = newIdentity(certFile, mspId);
            // Signer signer = newSigner(keyDir);

            // /* 4. gRPC 连接 */
            // ManagedChannel channel = newGrpcConnection(tlsCert);
            // try {
            // Gateway gateway = Gateway.newInstance()
            // .identity(identity)
            // .signer(signer)
            // .connection(channel)
            // .evaluateOptions(o -> o.withDeadlineAfter(5, TimeUnit.SECONDS))
            // .connect();

            // /* 5. 查链高 */
            // long height = gateway.getNetwork(channelName).getBlockNumber();

            // chainresp.setData(new JSONObject().put("height", height));

            // } finally {
            // channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            // }

            // } catch (Exception e) {
            // e.printStackTrace();
            // chainresp.setData("Error retrieving Fabric block height: " + e.getMessage());
            // }
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

        else if (portNumber.equals(fabricname)) {
            // String configPath = "D:\\桌面\\fabric-samples-main\\test-network";
            // String userName = "User1@org1.example.com";
            // String mspId = "Org1MSP";
            // String channelId = "mychannel";

            // Path cryptoDir = Paths.get(configPath,
            // "organizations", "peerOrganizations", "org1.example.com");
            // Path certFile = cryptoDir.resolve(
            // "users\\User1@org1.example.com\\msp\\signcerts\\cert.pem");
            // Path keyDir = cryptoDir.resolve(
            // "users\\User1@org1.example.com\\msp\\keystore");
            // Path keyFile = null;
            // try {
            // keyFile = Files.list(keyDir) // 可能抛 IOException
            // .findFirst()
            // .orElse(null);
            // } catch (IOException e) {
            // e.printStackTrace();
            // queryBlockInfoResp.setData("Fabric key file not found");
            // return queryBlockInfoResp;
            // }
            // if (keyFile == null) {
            // queryBlockInfoResp.setData("Fabric key file not found");
            // return queryBlockInfoResp;
            // }

            // try {
            // /* 1. 内存钱包 */
            // X509Certificate cert = Identities.readX509Certificate(
            // Files.newBufferedReader(certFile));
            // PrivateKey key = Identities.readPrivateKey(
            // Files.newBufferedReader(keyFile));
            // Wallet wallet = Wallets.newInMemoryWallet();
            // wallet.put(userName, Identities.newX509Identity(mspId, cert, key));

            // /* 2. 连接网关 */
            // Gateway gateway = Gateway.createBuilder()
            // .identity(wallet, userName)
            // .networkConfig(Paths.get(configPath, "connection.yaml"))
            // .discovery(true)
            // .connect();

            // /* 3. 拿到 Channel 对象（SDK 级别） */
            // Channel channel = gateway.getNetwork(channelId).getChannel();

            // /* 4. 按高度查区块（qscc） */
            // long blockNo = Long.parseLong(blockheightReq.getBlockHEIGHT());
            // BlockInfo block = channel.queryBlockByNumber(blockNo);
            // /* 5. 封装统一格式 */
            // JSONObject blockInfo = new JSONObject();
            // blockInfo.put("blockHeight", block.getBlockNumber());
            // blockInfo.put("blockHash", Hex.encodeHexString(block.getDataHash()));
            // blockInfo.put("previousHash", Hex.encodeHexString(block.getPreviousHash()));
            // blockInfo.put("transactionCount", block.getTransactionCount());
            // queryBlockInfoResp.setRet(ResultCode.SUCCESS);
            // queryBlockInfoResp.setData(blockInfo);

            // gateway.close();
            // System.out.println("-------Fabric区块信息查询完毕-------");

            // } catch (Exception e) {
            // e.printStackTrace();
            // queryBlockInfoResp.setData("Fabric query failed: " + e.getMessage());
            // }
        }
        return queryBlockInfoResp;
    }

    /*
     * zzy fake data added
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

        JSONObject heightRes = (JSONObject) checkChainnewblock(chainreq).getData();
        BigInteger blockHeight = (BigInteger) heightRes.get("heightinfo");
        if (blockHeight == null) {
            queryNewBlock.setData("Failed to connect to the blockchain node.");
            return queryNewBlock;
        }
        if (topN > blockHeight.intValue() + 1) {
            topN = blockHeight.intValue() + 1;
        }

        JSONArray blocks = new JSONArray();
        if (portNumber.equals(CrosschainInfo.ETH.ChainPort)) {

            for (int i = 0; i < topN; i++) {
                JSONObject blockInfo = new JSONObject();
                BlockheightReq blockheightReq = new BlockheightReq();
                blockheightReq.setChainIP(chainreq.getChainIP());
                blockheightReq.setBlockHEIGHT(blockHeight.subtract(BigInteger.valueOf(i)).toString());
                CommonResp commonResp = checkHeightInfo(blockheightReq);
                if (Objects.equals(commonResp.getCode(), ResultCode.SUCCESS.Code)) {
                    JSONObject tempBlockInfo = (JSONObject) commonResp.getData();
                    blockInfo.put("blockHeight", tempBlockInfo.get("blockHeight"));
                    blockInfo.put("timestamp", tempBlockInfo.get("timestamp"));
                    blockInfo.put("blockSize", tempBlockInfo.get("blockSize"));
                    blockInfo.put("transactionCount", tempBlockInfo.get("transactionCount"));
                    blockInfo.put("previousBlockAddress", tempBlockInfo.get("previousBlockAddress"));
                    blockInfo.put("gasLimit", tempBlockInfo.get("gasLimit"));
                    blockInfo.put("gasUsed", tempBlockInfo.get("gasUsed"));
                    blockInfo.put("stateRoot", tempBlockInfo.get("stateRoot"));
                    blockInfo.put("totalDifficulty", tempBlockInfo.get("totalDifficulty"));
                    blockInfo.put("difficulty", tempBlockInfo.get("difficulty"));
                    blockInfo.put("blockReward", tempBlockInfo.get("blockReward"));
                    blockInfo.put("minerAddress", tempBlockInfo.get("minerAddress"));
                }
                blocks.add(blockInfo);
            }
            System.out.println("-------ETH最新" + topN + "个区块信息查询完毕-------");
        } else if (portNumber.equals(CrosschainInfo.CMK.ChainPort)) {

            for (int i = 0; i < topN; i++) {
                JSONObject blockInfo = new JSONObject();
                BlockheightReq blockheightReq = new BlockheightReq();
                blockheightReq.setChainIP(chainreq.getChainIP());
                blockheightReq.setBlockHEIGHT(blockHeight.subtract(BigInteger.valueOf(i)).toString());
                CommonResp commonResp = checkHeightInfo(blockheightReq);
                if (Objects.equals(commonResp.getCode(), ResultCode.SUCCESS.Code)) {
                    JSONObject tempBlockInfo = (JSONObject) commonResp.getData();
                    blockInfo.put("blockHeight", tempBlockInfo.get("blockHeight"));
                    blockInfo.put("blockHash", tempBlockInfo.get("blockHash"));
                    blockInfo.put("timestamp", tempBlockInfo.get("timestamp"));
                    blockInfo.put("dagHash", tempBlockInfo.get("dagHash"));
                    blockInfo.put("transactionCount", tempBlockInfo.get("transactionCount"));
                    blockInfo.put("previousBlockHash", tempBlockInfo.get("previousBlockHash"));
                    blockInfo.put("rwSetRoot", tempBlockInfo.get("rwSetRoot"));
                    blockInfo.put("proposerMemberInfo", tempBlockInfo.get("proposerMemberInfo"));
                    blockInfo.put("signature", tempBlockInfo.get("signature"));
                    blockInfo.put("txRoot", tempBlockInfo.get("txRoot"));
                }
                blocks.add(blockInfo);
            }
            System.out.println("-------ChainMaker最新" + topN + "个区块信息查询完毕-------");
        } else if (portNumber.equals(CrosschainInfo.H2C.ChainPort)) {

            for (int i = 0; i < topN; i++) {
                JSONObject blockInfo = new JSONObject();
                BlockheightReq blockheightReq = new BlockheightReq();
                blockheightReq.setChainIP(chainreq.getChainIP());
                blockheightReq.setBlockHEIGHT(blockHeight.subtract(BigInteger.valueOf(i)).toString());
                CommonResp commonResp = checkHeightInfo(blockheightReq);
                if (Objects.equals(commonResp.getCode(), ResultCode.SUCCESS.Code)) {
                    JSONObject tempBlockInfo = (JSONObject) commonResp.getData();

                    blockInfo.put("blockHeight", tempBlockInfo.get("blockHeight"));
                    blockInfo.put("blockHash", tempBlockInfo.get("blockHash"));
                    blockInfo.put("timeStamp", tempBlockInfo.get("timeStamp"));
                    blockInfo.put("blockSize", tempBlockInfo.get("blockSize"));
                    blockInfo.put("transactionCount", tempBlockInfo.get("transactionCount"));
                    blockInfo.put("previousBlockHash", tempBlockInfo.get("previousBlockHash"));
                    blockInfo.put("merkleTreeRootOfWorldState", tempBlockInfo.get("merkleTreeRootOfWorldState"));
                    blockInfo.put("merkleTreeRootOfTransactions", tempBlockInfo.get("merkleTreeRootOfTransactions"));
                    blockInfo.put("merkleTreeRootOfTransactionState",
                            tempBlockInfo.get("merkleTreeRootOfTransactionState"));
                    blockInfo.put("signerPubkey", tempBlockInfo.get("signerPubkey"));
                }
                blocks.add(blockInfo);
            }
            System.out.println("-------H2Chain最新" + topN + "个区块信息查询完毕-------");
        } else if (portNumber.equals(CrosschainInfo.BuB.ChainPort)) {

            for (int i = 0; i < topN; i++) {
                JSONObject blockInfo = new JSONObject();
                BlockheightReq blockheightReq = new BlockheightReq();
                blockheightReq.setChainIP(chainreq.getChainIP());
                blockheightReq.setBlockHEIGHT(blockHeight.subtract(BigInteger.valueOf(i)).toString());
                CommonResp commonResp = checkHeightInfo(blockheightReq);
                if (Objects.equals(commonResp.getCode(), ResultCode.SUCCESS.Code)) {
                    JSONObject tempBlockInfo = (JSONObject) commonResp.getData();
                    blockInfo.put("blockHeight", tempBlockInfo.get("blockHeight"));
                    blockInfo.put("accountTreeHash", tempBlockInfo.get("accountTreeHash"));
                    blockInfo.put("closeTime", tempBlockInfo.get("closeTime"));
                    blockInfo.put("consensusValueHash", tempBlockInfo.get("consensusValueHash"));
                    blockInfo.put("feesHash", tempBlockInfo.get("feesHash"));
                    blockInfo.put("hash", tempBlockInfo.get("hash"));
                    blockInfo.put("previousHash", tempBlockInfo.get("previousHash"));
                    blockInfo.put("validatorsHash", tempBlockInfo.get("validatorsHash"));
                    blockInfo.put("version", tempBlockInfo.get("version"));
                }
                blocks.add(blockInfo);
            }
            System.out.println("-------BuBi最新" + topN + "个区块信息查询完毕-------");
        } else if (portNumber.equals(CrosschainInfo.FBC.ChainPort)) {
            // if (!BlockchainConfig.do_update_blockchain) {
            // for (int i = 1000; i < 1000 + topN; i++) {
            // JSONObject blockInfo = new JSONObject();
            // blockInfo.put("gasLimit", "0x0");
            // blockInfo.put("gasUsed", "0x0");
            // blockInfo.put("hash",
            // "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
            // blockInfo.put("number", i);
            // blockInfo.put("parentHash",
            // "0xeccad5274949b9d25996f7a96b89c0ac5c099eb9b72cc00d65bc6ef09f7bd10b");
            // blockInfo.put("sealer", "0x0");
            //
            // String[] stringArray = {
            // "0471101bcf033cd9e0cbd6eef76c144e6eff90a7a0b1847b5976f8ba32b2516c0528338060a4599fc5e3bafee188bca8ccc529fbd92a760ef57ec9a14e9e4278",
            // "2b08375e6f876241b2a1d495cd560bd8e43265f57dc9ed07254616ea88e371dfa6d40d9a702eadfd5e025180f9d966a67f861da214dd36237b58d72aaec2e108",
            // "cf93054cf524f51c9fe4e9a76a50218aaa7a2ca6e58f6f5634f9c2884d2e972486c7fe1d244d4b49c6148c1cb524bcc1c99ee838bb9dd77eb42f557687310ebd",
            // "ed1c85b815164b31e895d3f4fc0b6e3f0a0622561ec58a10cc8f3757a73621292d88072bf853ac52f0a9a9bbb10a54bdeef03c3a8a42885fe2467b9d13da9dec"
            // };
            // List<String> stringList = Arrays.asList(stringArray);
            // JSONArray s = new JSONArray(stringList);
            //
            // blockInfo.put("sealerList", s);
            // blockInfo.put("stateRoot",
            // "0x9711819153f7397ec66a78b02624f70a343b49c60bc2f21a77b977b0ed91cef9");
            // blockInfo.put("timestamp", "0x1692f119c84");
            // blockInfo.put("transactionsRoot",
            // "0x516787f85980a86fd04b0e9ce82a1a75950db866e8cdf543c2cae3e4a51d91b7");
            //
            // String[] txArray = {
            // "0xa14638d47cc679cf6eeb7f36a6d2a30ea56cb8dcf0938719ff45023a7a8edb5d",
            // "0xa14638d47cc679cf6eeb7f36a6d2a30ea56cb8dcf0938719ff45023a7a8edb5d"
            // };
            // List<String> txList = Arrays.asList(txArray);
            // JSONArray txs = new JSONArray(txList);
            //
            // blockInfo.put("transactions", txs);
            // blocks.add(blockInfo);
            // }
            // System.out.println("-------Fisco Bcos最新十个区块信息查询完毕-------");
            // JSONObject tenblocks = new JSONObject();
            // tenblocks.put("tenBlocksInfo", blocks);
            // queryNewBlock.setRet(ResultCode.SUCCESS);
            // queryNewBlock.setData(tenblocks);
            // return queryNewBlock;
            // }
            for (int i = 0; i < topN; i++) {
                JSONObject blockInfo = new JSONObject();
                BlockheightReq blockheightReq = new BlockheightReq();
                blockheightReq.setChainIP(chainreq.getChainIP());
                blockheightReq.setBlockHEIGHT(blockHeight.subtract(BigInteger.valueOf(i)).toString());
                CommonResp commonResp = checkHeightInfo(blockheightReq);
                if (Objects.equals(commonResp.getCode(), ResultCode.SUCCESS.Code)) {
                    JSONObject tempBlockInfo = (JSONObject) commonResp.getData();
                    blockInfo.put("gasLimit", tempBlockInfo.get("gasLimit"));
                    blockInfo.put("gasUsed", tempBlockInfo.get("gasUsed"));
                    blockInfo.put("hash", tempBlockInfo.get("hash"));
                    blockInfo.put("number", tempBlockInfo.get("number"));
                    blockInfo.put("parentHash", tempBlockInfo.get("parentHash"));
                    blockInfo.put("sealer", tempBlockInfo.get("sealer"));
                    blockInfo.put("sealerList", tempBlockInfo.get("sealerList"));
                    blockInfo.put("stateRoot", tempBlockInfo.get("stateRoot"));
                    blockInfo.put("timestamp", tempBlockInfo.get("timestamp"));
                    blockInfo.put("transactionsRoot", tempBlockInfo.get("transactionsRoot"));
                    blockInfo.put("transactions", tempBlockInfo.get("transactions"));
                }
                blocks.add(blockInfo);
            }
        } else if (portNumber.equals(CrosschainInfo.FAB.ChainPort)) {
            if (!BlockchainConfig.do_update_blockchain) {
                for (int i = 1000; i < 1000 + topN; i++) {
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

        // QueryWrapper<Chain> wrapper = new QueryWrapper<>();
        // wrapper.select("chain_type");
        // wrapper.eq("ip_address", ipAddress);
        // wrapper.eq("port", portNumber);
        // Chain chain = chainMapper.selectOne(wrapper);
        // String chainType = chain.getChainType();

        if (portNumber.equals(ethname)) {

            String wsUrl = "ws://" + ipAddress + ":" + ethWsPort;

            try {

                // 创建 WebSocketService
                WebSocketService webSocketService = new WebSocketService(wsUrl, true);
                webSocketService.connect(); // 连接 WebSocket

                // 创建 Web3j 实例
                Web3j web3j = Web3j.build(webSocketService);

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
            } catch (Exception e) {
                queryTxInfoResp.setData("Failed to connect to the blockchain node.");
            }
            System.out.println("-------ETH交易信息查询完毕-------");
        } else if (portNumber.equals(chainmakername)) {
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
        } else if (portNumber.equals(bubiname)) {
            String targetUrl = "http://" + ipAddress + ":19333/getTransactionHistory?hash=" + txhashreq.getTxHASH();
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

            try {
                JsonObject root = JsonParser.parseString(logs).getAsJsonObject();

                int errorCode = root.get("error_code").getAsInt();
                if (errorCode != 0) {
                    queryTxInfoResp.setRet(ResultCode.FAILURE);
                    queryTxInfoResp.setMsg("查询失败，error_code=" + errorCode);
                    return queryTxInfoResp;
                }

                JsonObject result = root.getAsJsonObject("result");
                JsonArray transactions = result.getAsJsonArray("transactions");
                if (transactions == null || transactions.size() == 0) {
                    queryTxInfoResp.setRet(ResultCode.FAILURE);
                    queryTxInfoResp.setMsg("未找到交易记录");
                    return queryTxInfoResp;
                }

                // 取第一条交易
                JsonObject txObj = transactions.get(0).getAsJsonObject();
                JsonObject transaction = txObj.getAsJsonObject("transaction");
                JsonArray operations = transaction.getAsJsonArray("operations");

                // ============= 基础字段 =============
                String txHash = txObj.has("hash") ? txObj.get("hash").getAsString() : "";
                String from = transaction.has("source_address") ? transaction.get("source_address").getAsString() : "";
                String to = "";
                String inputHex = "";
                if (operations != null && operations.size() > 0) {
                    JsonObject op = operations.get(0).getAsJsonObject();
                    JsonObject payCoin = op.getAsJsonObject("pay_coin");
                    if (payCoin != null) {
                        to = payCoin.has("dest_address") ? payCoin.get("dest_address").getAsString() : "";
                        inputHex = payCoin.has("input") ? payCoin.get("input").getAsString() : "";
                    }
                }

                // 状态
                String status = (txObj.get("error_code").getAsInt() == 0) ? "success" : "failed";
                BigInteger amount = BigInteger.ZERO;

                // ============= 扩展字段 =============
                long blockHeight = txObj.has("ledger_seq") ? txObj.get("ledger_seq").getAsLong() : -1;
                long closeTime = txObj.has("close_time") ? txObj.get("close_time").getAsLong() : 0L;
                long timestamp = closeTime / 1000; // 近似换算为秒级时间戳
                String rwSetHash = ""; // 布比没有返回，可置空
                String contractName = "";
                String method = "";
                String signer = "";
                String signature = "";

                // 取签名信息
                JsonArray sigs = txObj.getAsJsonArray("signatures");
                if (sigs != null && sigs.size() > 0) {
                    JsonObject sigObj = sigs.get(0).getAsJsonObject();
                    signer = sigObj.has("public_key") ? sigObj.get("public_key").getAsString() : "";
                    signature = sigObj.has("sign_data") ? sigObj.get("sign_data").getAsString() : "";
                }

                // 如果存在contract_tx_hashes，可推测这是合约调用
                if (txObj.has("contract_tx_hashes")) {
                    JsonArray contractHashes = txObj.getAsJsonArray("contract_tx_hashes");
                    if (contractHashes.size() > 0) {
                        contractName = "contract_" + contractHashes.get(0).getAsString().substring(0, 8);
                        method = "invoke";
                    }
                }

                // ============= 构造返回 JSON =============
                JSONObject txInfo = new JSONObject();
                txInfo.put("txHash", txHash);
                txInfo.put("txId", txHash); // 保留兼容字段
                txInfo.put("from", from);
                txInfo.put("to", to);
                txInfo.put("amount", amount);
                txInfo.put("status", status);
                txInfo.put("raw_input", inputHex);

                txInfo.put("blockHeight", blockHeight);
                txInfo.put("blockTimestamp", closeTime);
                txInfo.put("timestamp", timestamp);
                txInfo.put("rwSetHash", rwSetHash);
                txInfo.put("signature", signature);
                txInfo.put("signer", signer);
                txInfo.put("contractName", contractName);
                txInfo.put("method", method);

                // 也返回请求hash方便比对
                txInfo.put("requestTxHash", txhashreq.getTxHASH());

                queryTxInfoResp.setRet(ResultCode.SUCCESS);
                queryTxInfoResp.setData(txInfo);

                System.out.println("-------BuBi交易信息查询完毕-------");

            } catch (Exception e) {
                e.printStackTrace();
                queryTxInfoResp.setRet(ResultCode.FAILURE);
                queryTxInfoResp.setMsg("解析布比链交易返回数据失败");
            }
        }

        else if (portNumber.equals(fabricname)) {
            // /* ========== 0. 固定配置 ========== */
            // String configPath = "D:\\桌面\\fabric-samples-main\\test-network";
            // String userName = "User1@org1.example.com";
            // String mspId = "Org1MSP";
            // String channelId = "mychannel";

            // /* ========== 1. 证书/密钥路径 ========== */
            // Path cryptoDir = Paths.get(configPath,
            // "organizations", "peerOrganizations", "org1.example.com");
            // Path certFile = cryptoDir.resolve(
            // "users\\User1@org1.example.com\\msp\\signcerts\\cert.pem");
            // Path keyDir = cryptoDir.resolve(
            // "users\\User1@org1.example.com\\msp\\keystore");
            // Path keyFile;
            // try {
            // keyFile = Files.list(keyDir).findFirst().orElse(null);
            // } catch (IOException e) {
            // queryTxInfoResp.setData("Fabric key file not found");
            // return queryTxInfoResp;
            // }
            // if (keyFile == null) {
            // queryTxInfoResp.setData("Fabric key file not found");
            // return queryTxInfoResp;
            // }

            // try {
            // /* 2. 内存 Wallet */
            // X509Certificate cert = Identities.readX509Certificate(
            // Files.newBufferedReader(certFile));
            // PrivateKey key = Identities.readPrivateKey(
            // Files.newBufferedReader(keyFile));
            // Wallet wallet = Wallets.newInMemoryWallet();
            // wallet.put(userName, Identities.newX509Identity(mspId, cert, key));

            // /* 3. Gateway 连接 */
            // Gateway gateway = Gateway.createBuilder()
            // .identity(wallet, userName)
            // .networkConfig(Paths.get(configPath, "connection.yaml"))
            // .discovery(true)
            // .connect();

            // /* 4. 获取 Channel 对象（SDK 级） */
            // Channel channel = gateway.getNetwork(channelId).getChannel();

            // // 5. 用 qscc 查交易所在区块
            // String txId = txhashreq.getTxHASH();
            // long blockNo = channel.queryBlockByTransactionID(txId).getBlockNumber(); //
            // 2.2.9 有这个方法
            // BlockInfo block = channel.queryBlockByNumber(blockNo);

            // // 6. 封装（2.2.9 只能拿到这些）
            // JSONObject txInfo = new JSONObject();
            // txInfo.put("txHash", txId);
            // txInfo.put("blockNumber", BigInteger.valueOf(blockNo));
            // txInfo.put("blockHash", Hex.encodeHexString(block.getDataHash()));
            // txInfo.put("from", ""); // 2.2.9 拿不到 creator
            // txInfo.put("to", "");
            // txInfo.put("contractName", ""); // 2.2.9 拿不到链码名
            // txInfo.put("method", "");
            // txInfo.put("signature", ""); // 2.2.9 拿不到签名
            // txInfo.put("timeStamp", ""); // 2.2.9 拿不到时间戳
            // txInfo.put("status", "VALID"); // 2.2.9 也拿不到验证位，直接写 VALID

            // queryTxInfoResp.setRet(ResultCode.SUCCESS);
            // queryTxInfoResp.setData(txInfo);

            // gateway.close();
            // System.out.println("-------Fabric交易信息查询完毕-------");

            // } catch (Exception e) {
            // e.printStackTrace();
            // queryTxInfoResp.setData("Fabric query failed: " + e.getMessage());
            // }
        }

        else {
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
