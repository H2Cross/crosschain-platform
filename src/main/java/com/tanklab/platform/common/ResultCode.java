package com.tanklab.platform.common;

public enum ResultCode {
    TEST("0","测试消息"),
    FAILURE("1009","No datas found with the provided criteria."),
    ERROR("1000","未知错误"),
    SUCCESS("1001","请求成功"),
    USER_EXIST("1002","用户已存在"),
    LOGIN_FAIL("1003","登录失败"),
    PRIPSWD_ERROR("1004","PrivateKey Password Error!"),
    PSWD_ERROR("1005","密码错误"),
    TOKEN_ERROR("1006","Token Error!"),
    NOT_MATCH_ERROR("1007","存在不匹配问题"),

    PROCESS_ERROR("1008","The Ox Cannot Process Again!")
    ;
    public String Code;
    public String Msg;
    ResultCode(String code, String msg) {
        this.Code = code;
        this.Msg = msg;
    }
}
