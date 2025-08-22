package com.tanklab.platform.service;

import com.tanklab.platform.ds.req.*;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户个人信息表 服务类
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
public interface UserService extends IService<User> {
    CommonResp login(UserLoginReq userloginreq);

    CommonResp register(UserRegisterReq userRegisterReq);

    CommonResp getInfo(String token);

    CommonResp setNickName(String token,UserSetNickNameReq userSetNickNameReq);

    CommonResp setPswd(String token,UserSetPswdReq userSetPswdReq);

    CommonResp logout(String token);
}
