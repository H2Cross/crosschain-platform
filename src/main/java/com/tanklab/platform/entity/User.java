package com.tanklab.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户个人信息表
 * </p>
 *
 * @author Bochen Hou
 * @since 2024-03-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="User对象", description="用户个人信息表")
public class User implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "用户唯一注册序号")
      @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @ApiModelProperty(value = "手机号")
    private String userMobile;

    @ApiModelProperty(value = "邮箱")
    private String userEmail;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "所在链")
    private String linkUrl;

    @ApiModelProperty(value = "注册时间")
    private Date registerDate;

    @ApiModelProperty(value = "密码")
    private String userPswd;

    @ApiModelProperty(value = "用户登陆token")
    private String token;

    @ApiModelProperty(value = "用户权限，0为普通开发者，1为管理员")
    private Integer authority;


}
