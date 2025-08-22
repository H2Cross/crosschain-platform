package com.tanklab.platform.common;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

public class SSHConfig {

    public static boolean do_crosschain = true;// 是否进行跨链，目前测试阶段填否
    public static String ssh_username = "root";
    // public static String ssh_ip = "116.204.36.31";
    public static int ssh_port = 22;
    public static String ssh_password = "Admin1q2w3e@123!";  
    //server服务器密码  Admin1q2w3e@123!
    //学弟服务器密码  ABCabc123

    private static Session session = null;
    private static int timeout = 60000;
    private static ChannelExec channelExec;
    private static final int MAX_RETRIES = 3;  // 最大重试次数
    private static final int RETRY_DELAY = 5000;  // 重试延迟(ms)

    public static void connect(String ip) throws Exception {
        connect(ip, ssh_username, ssh_password);
    }

    public static void connect(String ip, String username, String password) throws Exception {
        System.out.println("正在连接服务器: " + ip);
        
        // 如果已有会话且仍然活跃，先断开
        if (session != null && session.isConnected()) {
            session.disconnect();
        }

        JSch jsch = new JSch();
        session = jsch.getSession(username, ip, ssh_port);

        if (session == null) {
            throw new Exception("创建SSH会话失败");
        }

        session.setPassword(password);
        Properties sshConfig = new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        session.setConfig(sshConfig);
        session.setTimeout(timeout);

        // 添加重试机制
        Exception lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                System.out.println("尝试第" + (i + 1) + "次连接...");
                session.connect();
                System.out.println("成功连接到服务器: " + ip);
                return;
            } catch (Exception e) {
                lastException = e;
                System.out.println("连接失败: " + e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    System.out.println("等待" + (RETRY_DELAY/1000) + "秒后重试...");
                    Thread.sleep(RETRY_DELAY);
                }
            }
        }
        throw new Exception("连接失败，已重试" + MAX_RETRIES + "次: " + lastException.getMessage());
    }

    /**
     * 在远程服务器上执行命令
     * 
     * @param cmd     要执行的命令字符串
     * @param charset 编码
     * @throws Exception
     */
    public static String executeCMD(String cmd, String charset) throws Exception {
        System.out.println("执行命令: " + cmd);
        String logs = "";

        try {
            // 确保session是连接的
            if (session == null || !session.isConnected()) {
                throw new Exception("SSH会话未连接");
            }

            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            channelExec.connect();

            InputStream in = channelExec.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
            String buf = "";
            while ((buf = reader.readLine()) != null) {
                logs += buf + "\n";
                System.out.println("命令输出: " + buf);
            }
            reader.close();
            
            // 检查命令执行状态
            if (channelExec.getExitStatus() != 0) {
                System.out.println("命令执行失败，退出码: " + channelExec.getExitStatus());
            }

            return logs;
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    public static void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /*
     * public static void main(String[] args) throws Exception {
     * // Date date = new Date();
     * // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     * // String formattedDate = sdf.format(date);
     * // System.out.println(formattedDate);
     * connect();
     * // String log =
     * executeCMD("source ~/.profile && cd CIPS-ETHEREUM-private && go run *.go -c eth -r true -ap beefAccess -ag "
     * + "abcde121aecde12" +" -cd 12","UTF-8");
     * String log =
     * executeCMD("source ~/.profile && cd /root/CIPS-ETHEREUM-private && go run *.go -c eth2chainmaker -r true -ap beefAccess -ag tj11213w1a"
     * ,"UTF-8");
     * //
     * // Date date1 = new Date();
     * // SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     * // String formattedDate1 = sdf.format(date1);
     * // System.out.println(formattedDate1);
     * //
     * //
     * //
     * //
     * // long delta = date1.getTime()-date.getTime();
     * // System.out.println(delta);
     * // connect();
     * // String log =
     * executeCMD("source ~/.profile && cd /root/CIPS-ETHEREUM-private && go run chainmakercontract/contractInvoke.go -cn BeefKing -cm sellbeef -ag 111222+content"
     * ,"utf-8");
     * 
     * 
     * // String log = executeCMD("netstat -ntlp","utf-8");
     * // System.out.println();
     * // String log1 = executeCMD("pwd && ls","UTF-8");
     * // System.out.println("--------");
     * // String[] l = log1.split("\n");
     * // System.out.println(l[l.length-1]);
     * // System.out.println("--------");
     * // execute("go run *.go -t 2 -i 0 -n [Tanklab,tanklab] -c SafeMath -m add"
     * ,"UTF-8");
     * }
     */
}
