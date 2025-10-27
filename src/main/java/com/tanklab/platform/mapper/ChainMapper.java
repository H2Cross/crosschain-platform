package com.tanklab.platform.mapper;

import com.tanklab.platform.entity.Chain;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
/**
 * <p>
 * 链信息表 Mapper 接口
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Mapper
public interface ChainMapper extends BaseMapper<Chain> {

}
