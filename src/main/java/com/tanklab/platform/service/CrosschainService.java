package com.tanklab.platform.service;

import com.tanklab.platform.ds.req.CommandReq;
import com.tanklab.platform.ds.req.CrossReq;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Crosschain;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 跨链信息表 服务类
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
public interface CrosschainService extends IService<Crosschain> {


    //CommonResp doCross(int param, int crosstype, int crossFrom, int crossTo);

    CommonResp queryAllCrossTx();

    CommonResp queryTxInfo(String txHash, Integer txId);

    CommonResp addCrossTx(CrossReq crossReq);

    CommonResp cmdExecute(CommandReq req);

    /**
     * 启动跨链网关
     * @param srcIp 源链服务器IP
     * @param srcChainType 源链类型 (ethereum/chainmaker/h2chain)
     * @param dstIp 目标链服务器IP
     * @param dstChainType 目标链类型 (ethereum/chainmaker/h2chain)
     * @param relayIp 中继链服务器IP
     * @return 启动结果
     */
    CommonResp startGateways(String srcIp, String srcChainType, String dstIp, String dstChainType, String relayIp);

    /**
     * 执行跨链操作
     * @param srcIp 源链服务器IP
     * @param srcChainType 源链类型 (ethereum/chainmaker/h2chain)
     * @param dstIp 目标链服务器IP
     * @param dstChainType 目标链类型 (ethereum/chainmaker/h2chain)
     * @return 跨链操作结果
     */
    CommonResp executeCrossChain(String srcIp, String srcChainType, String dstIp, String dstChainType,String srcappId,String dstappId,String appArgs);

    /**
     * 执行完整的跨链操作（包括启动网关和执行跨链）
     * @param srcIp 源链服务器IP
     * @param srcChainType 源链类型 (ethereum/chainmaker/h2chain)
     * @param dstIp 目标链服务器IP
     * @param dstChainType 目标链类型 (ethereum/chainmaker/h2chain)
     * @param relayIp 中继链服务器IP
     * @return 跨链操作结果
     */
    CommonResp executeFullCrossChain(String srcIp, String srcChainType, String dstIp, String dstChainType, String relayIp,String srcappId,String dstappId,String appArgs);
}
