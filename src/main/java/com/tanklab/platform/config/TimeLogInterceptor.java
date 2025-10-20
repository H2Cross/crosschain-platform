package com.tanklab.platform.config;

import com.tanklab.platform.util.LogCapture;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeLogInterceptor implements HandlerInterceptor {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

   @Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    long startTime = System.currentTimeMillis();
    request.setAttribute("startTime", startTime);
    LogCapture.start(request);   // ✅ 只启动，不停止
    return true;
}

@Override
public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    // 只打印耗时信息，但不调用 stop()
    long startTime = (long) request.getAttribute("startTime");
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    System.out.printf(
        "接口调用 - 路径: %s, 耗时: %dms%n",
        request.getRequestURI(), duration
    );
}
}
