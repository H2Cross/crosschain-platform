package com.tanklab.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanklab.platform.common.ResultCode;
import com.tanklab.platform.ds.req.*;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.entity.User;
import com.tanklab.platform.mapper.UserMapper;
import com.tanklab.platform.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户个人信息表 服务实现类
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * @author ZZY
     * @date 2025/6/5 19:12
    **/
    @Override
    public CommonResp login(UserLoginReq userloginreq){
        CommonResp userloginresp = new CommonResp();

        String userAccount = userloginreq.getUser_account();
        String userPswd = userloginreq.getUser_pswd();

        QueryWrapper<User> wrapper = new QueryWrapper<>();


        wrapper.eq("user_pswd", userPswd)
                        .and(wq -> wq.eq("user_mobile",userAccount).or().eq("user_email",userAccount));

        User user = userMapper.selectOne(wrapper);

        System.out.println(user);
        if (user == null){
            userloginresp.setRet(ResultCode.LOGIN_FAIL);
        }else{
            userloginresp.setRet(ResultCode.SUCCESS);
            userloginresp.setData(user.getToken());
        }
        return userloginresp;
    }

    /**
     * @author ZZY
     * @date 2025/6/5 18:08
    **/
    @Override
    public CommonResp register(UserRegisterReq userRegisterReq) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("user_mobile", "user_email");
        List<User> users = userMapper.selectList(wrapper);

        CommonResp userregisterresp = new CommonResp<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (userRegisterReq.getUser_mobile().equals(user.getUserMobile()) ||
            userRegisterReq.getUser_email().equals(user.getUserEmail())){
                userregisterresp.setRet(ResultCode.USER_EXIST);
                return userregisterresp;
            }
        }
        User newUser = new User()
                .setUserEmail(userRegisterReq.getUser_email())
                .setUserMobile(userRegisterReq.getUser_mobile())
                .setUserPswd(userRegisterReq.getUser_pswd())
                .setLinkUrl(userRegisterReq.getLink_url())
                .setNickName("新用户")
                .setAuthority(1)
                .setRegisterDate(new Date(Timestamp.valueOf(LocalDateTime.now()).getTime()));

        String token = RandomStringUtils.random(16, "0123456789abcdef");
        newUser.setToken(token);

        int insert = this.userMapper.insert(newUser);
        userregisterresp.setRet(ResultCode.SUCCESS);
        userregisterresp.setMessage(token);

        return userregisterresp;
    }

    @Override
    public CommonResp getInfo(String token) {
        CommonResp userinforesp = new CommonResp<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("user_mobile","user_email","nick_name","link_url","register_date","token");
        wrapper.eq("token",token);
        User user = userMapper.selectOne(wrapper);

        if (user == null){
            userinforesp.setRet(ResultCode.NOT_MATCH_ERROR);
        }else{
            userinforesp.setRet(ResultCode.SUCCESS);
            JSONObject info = new JSONObject();
            info.put("user_mobile",user.getUserMobile());
            info.put("user_email",user.getUserEmail());
            info.put("nick_name",user.getNickName());
            info.put("link_url",user.getLinkUrl());
            info.put("register_date",user.getRegisterDate());
            userinforesp.setData(info);
        }
        return userinforesp;
    }

    @Override
    public CommonResp setNickName(String token,UserSetNickNameReq userSetNickNameReq) {
        CommonResp nicknameresp = new CommonResp<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("token",token);
        User user = userMapper.selectOne(wrapper);

        if (user == null){
            nicknameresp.setRet(ResultCode.NOT_MATCH_ERROR);
        }else{
            user.setNickName(userSetNickNameReq.getNew_nick_name());
            userMapper.update(user,wrapper);
            nicknameresp.setRet(ResultCode.SUCCESS);
        }
        return nicknameresp;
    }

    @Override
    public CommonResp setPswd(String token,UserSetPswdReq userSetPswdReq) {
        CommonResp setpswdresp = new CommonResp<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("token",token).eq("user_pswd",userSetPswdReq.getOld_pswd());
        User user = userMapper.selectOne(wrapper);

        if (user == null){
            setpswdresp.setRet(ResultCode.PSWD_ERROR);
        }else{
            user.setUserPswd(userSetPswdReq.getNew_pswd());
            user.setToken(RandomStringUtils.random(16, "0123456789abcdef"));
            userMapper.update(user,wrapper);
            setpswdresp.setRet(ResultCode.SUCCESS);
        }
        return setpswdresp;
    }
    @Override
    public CommonResp logout(String token){
        CommonResp logoutresp = new CommonResp<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("token",token);
        User user = userMapper.selectOne(wrapper);

        if (user == null){
            logoutresp.setRet(ResultCode.NOT_MATCH_ERROR);
        }else{
            String new_token = RandomStringUtils.random(16, "0123456789abcdef");
            user.setToken(new_token);
            userMapper.update(user,wrapper);
            logoutresp.setRet(ResultCode.SUCCESS);
        }
        return logoutresp;
    }
}
