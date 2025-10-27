package com.tanklab.platform.mapper;

import com.tanklab.platform.entity.Address;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
/**
 * <p>
 * 用户地址表 Mapper 接口
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

}
