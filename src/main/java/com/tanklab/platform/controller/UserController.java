package com.tanklab.platform.controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.tanklab.platform.ds.req.*;
import com.tanklab.platform.ds.resp.CommonResp;
import com.tanklab.platform.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * <p>
 * 用户个人信息表 前端控制器
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@RestController
@RequestMapping("/platform/user")
public class UserController {
    @Autowired
    UserService userService;
    
    /**
     * @author ZZY
     * @date 2025/6/5 00:27
    **/
    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public CommonResp Register(@RequestBody UserRegisterReq userRegisterReq){return userService.register(userRegisterReq);}
    
    
    @ApiOperation(value="用户登录")
    @PostMapping("/login")
    public CommonResp Login(@RequestBody UserLoginReq userloginreq) {
        return userService.login(userloginreq);
    }

    @ApiOperation(value = "查询用户信息")
    @GetMapping("/getInfo")
    public CommonResp GetInfo(@RequestHeader("Token") String token)
    {return  userService.getInfo(token);}

    @ApiOperation(value = "修改昵称")
    @PostMapping("/setNickName")
    public CommonResp SetNickName(@RequestHeader("Token") String token,
                                  @RequestBody UserSetNickNameReq userSetNickNameReq)
    {return  userService.setNickName(token,userSetNickNameReq);}

    @ApiOperation(value = "修改密码")
    @PostMapping("/setPswd")
    public CommonResp setPswd(@RequestHeader("Token") String token,
                              @RequestBody UserSetPswdReq userSetPswdReq)
    {return  userService.setPswd(token,userSetPswdReq);}

    @ApiOperation(value = "退出登录（刷新token）")
    @GetMapping("/logout")
    public CommonResp logout(@RequestHeader("Token") String token)
    {return  userService.logout(token);}
}

