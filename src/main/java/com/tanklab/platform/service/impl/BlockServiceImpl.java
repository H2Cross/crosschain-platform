package com.tanklab.platform.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanklab.platform.ds.req.ChainReq;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Chain;
import com.tanklab.platform.mapper.BlockMapper;
import com.tanklab.platform.mapper.ChainMapper;
import com.tanklab.platform.service.BlockService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 块信息表 服务实现类，目的是获取前十个区块然后写入数据库，并实时刷新，不确定需不需要
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */

@Service
public class BlockServiceImpl extends ServiceImpl<BlockMapper, Chain> implements BlockService {

    @Autowired
    private ChainMapper chainMapper;
    @Autowired
    public BlockServiceImpl BlockService;

public class Block {
        private String blockNumber;
        private String blockHash;

        public Block(String blockNumber, String blockHash) {
            this.blockNumber = blockNumber;
            this.blockHash = blockHash;
        }
        public String getBlockNumber() {
            return blockNumber;
        }

        public String getBlockHash() {
            return blockHash;
        }
        // Getters and setters (根据需要自行添加)
    }




    public CommonResp blockInfo(ChainReq chainreq){

        CommonResp blockresp = new CommonResp();
        List<Block> blocks = new ArrayList<>();
        String DB_URL = "jdbc:mysql://localhost:3306/chain?serverTimezone=GMT%2B8&characterEncoding=utf-8";
        String DB_USERNAME = "hou";
        String DB_PASSWORD = "123456";
        try {
            // 创建 OkHttpClient 实例，并设置超时时间
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS);

            // 创建自定义的 HttpService，并传入 OkHttpClient 实例
            HttpService httpService = new HttpService(chainreq.getChainIP(), builder.build());

            // 创建 Web3j 实例
            Web3j web3j = Web3j.build(httpService);

            // 查询最新十个块
            BigInteger latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            BigInteger startBlockNumber = latestBlockNumber.subtract(BigInteger.valueOf(9));

            for (BigInteger i = startBlockNumber; i.compareTo(latestBlockNumber) <= 0; i = i.add(BigInteger.ONE)) {
                EthBlock.Block block = web3j.ethGetBlockByNumber((DefaultBlockParameter) i, true).send().getBlock();
                String blockNumber = block.getNumber().toString();
                String blockHash = block.getHash();
                Block blockInfo = new Block(blockNumber, blockHash);
                blocks.add(blockInfo);
            }

            blockresp.setCode(String.valueOf(200));
            blockresp.setMessage("Success");
            blockresp.setData(blocks);
        } catch (IOException e) {
            blockresp.setCode(String.valueOf(500));
            blockresp.setMessage("Failed to connect to the private chain.");
            blockresp.setData(null);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // 清空原有内容
            clearBlocksTable(conn);

            // 将 blocks 列表的十条数据写入数据库
            writeBlocksToDatabase(conn, blocks);

            blockresp.setCode(String.valueOf(200));
            blockresp.setMessage("Success");
            blockresp.setData(blocks);
        } catch (SQLException e) {
            blockresp.setCode(String.valueOf(500));
            blockresp.setMessage("Failed to connect to the database.");
            blockresp.setData(null);
        }


        return blockresp;
    }

    private void clearBlocksTable(Connection conn) throws SQLException {
        String deleteQuery = "DELETE FROM blocks";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.executeUpdate();
        }
    }
    private void writeBlocksToDatabase(Connection conn, List<Block> blocks) throws SQLException {
        String insertQuery = "INSERT INTO blocks (blockNumber, blockHash) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            for (Block block : blocks) {
                stmt.setString(1, block.getBlockNumber());
                stmt.setString(2, block.getBlockHash());
                stmt.executeUpdate();
            }
        }
    }

    /*@Scheduled(fixedRate = 1000*60*60)
    public void updatebolcks(){


    }*/
}
