package com.tanklab.platform.service;

import com.tanklab.platform.ds.req.*;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.Chain;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 链信息表 服务类
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
public interface BlockService extends IService<Chain> {
CommonResp blockInfo(ChainReq chainreq);

    //void updateblocks();
}
