package com.tanklab.platform.service.impl;

import com.tanklab.platform.entity.Address;
import com.tanklab.platform.mapper.AddressMapper;
import com.tanklab.platform.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户地址表 服务实现类
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

}
