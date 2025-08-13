package com.tanklab.platform.common;

/**
 * <p>
 * 区块链信息
 * TODO:存储区块链IP、钱包账户、合约地址等基本信息
 * </p>
 *
 * @author Zhiyuan Zheng
 * @since 2023-07-27
 */
public class BlockchainConfig {

    /* 因为测试时上链速度可能需要等待一会儿，所以是否信息上链可以填false，表示不进行上链操作 */
    public static boolean do_update_blockchain = false;//是否信息上链

    public static String c0_netWorkUrl = "http://123.249.32.83:10012";//国外链ip和端口（注意是和http请求对接的端口）

    public static String c1_netWorkUrl  = "http://123.249.32.83:10016";//国内链ip和端口（注意是和http请求对接的端口）

    public static String c0_walletKey = "0847b4b2f1fe3944bdb62be2f528d3a1a2cca2b957df7fba422830c1c7a14bf6";//通过metamask导入json获取

    public static String c1_walletKey  = "0847b4b2f1fe3944bdb62be2f528d3a1a2cca2b957df7fba422830c1c7a14bf6";//通过metamask导入json获取

    public static String ox_contractAddress = "0xB232e1c8bd3B68D70DA624C3F3F243AF25aa7817";//国外链合约部署地址
    public static String beef_contractAddress = "0xEcd93E2BE0BC310cDa53ad1eE4C362Ddf8300059";//国内链合约部署地址


    public static long c0_chainId = 15987;//国外链chainId

    public static long c1_chainId = 15988;//国内链chainId
}
