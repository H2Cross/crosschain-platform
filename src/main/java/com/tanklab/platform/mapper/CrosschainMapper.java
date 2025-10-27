package com.tanklab.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanklab.platform.entity.Crosschain;

import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 跨链信息表 Mapper 接口
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Mapper
public interface CrosschainMapper extends BaseMapper<Crosschain> {

}
