package com.tanklab.platform.common;

/**
 * @author ZZY
 * @date 2025/6/4 22:55
**/
public enum CrosschainInfo {
    ETH("ethereum",12001,"8086","以太坊"),
    H2C("h2chain",13001,"8087","海河智链"),
    CMK("chainmaker",11001,"8088","长安链"),
    BuB("bubi",14001,"8089","布比链"),
    FAB("fabric",15001,"8090","Fabric"),
    FBC("fisco",16001,"8091","金链盟")
    ;
    public String ChainName;
    public Integer ChainId;
    public String ChainPort;
    public String ChineseName;
    CrosschainInfo(String chainName, Integer ChainId,String Port,String ChineseName) {
        this.ChainName = chainName;
        this.ChainId = ChainId;
        this.ChainPort = Port;
        this.ChineseName = ChineseName;
    }
}
