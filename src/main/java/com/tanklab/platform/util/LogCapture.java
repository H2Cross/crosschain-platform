package com.tanklab.platform.util;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class LogCapture {

    // ✅ 双输出流，既写控制台又写缓存
    private static class TeeOutputStream extends OutputStream {
        private final OutputStream out1;
        private final OutputStream out2;
        TeeOutputStream(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }
        @Override
        public void write(int b) {
            try {
                out1.write(b);
                out2.write(b);
            } catch (Exception ignored) {}
        }
        @Override
        public void flush() {
            try {
                out1.flush();
                out2.flush();
            } catch (Exception ignored) {}
        }
    }

    /** 启动捕获，绑定到 request */
    public static void start(HttpServletRequest request) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        TeeOutputStream tee = new TeeOutputStream(original, baos);
        PrintStream ps = new PrintStream(tee, true);

        request.setAttribute("originalOut", original);
        request.setAttribute("captureStream", baos);

        System.setOut(ps);
    }

    /** 停止捕获并返回日志 */
    public static String stop(HttpServletRequest request) {
        Object orig = request.getAttribute("originalOut");
        Object baosObj = request.getAttribute("captureStream");

        if (orig instanceof PrintStream) {
            System.setOut((PrintStream) orig); // 恢复标准输出
        }

        if (baosObj instanceof ByteArrayOutputStream) {
            return ((ByteArrayOutputStream) baosObj).toString();
        }
        return "";
    }
}
