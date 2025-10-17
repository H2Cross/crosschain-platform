package com.tanklab.platform.config;

import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeLogInterceptor implements HandlerInterceptor {

    // 请求处理前调用（记录开始时间）
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        // 将开始时间存入请求属性中，供后续使用
        request.setAttribute("startTime", startTime);
        return true; // 放行请求
    }

    // 定义日期格式（线程安全，可全局复用）
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 将时间戳转换为LocalDateTime（默认时区，可根据需求指定，如ZoneId.of("Asia/Shanghai")）
        LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

        // 格式化输出
        System.out.printf(
                "接口调用 - 路径: %s, 开始时间: %s, 结束时间: %s, 耗时: %dms%n",
                request.getRequestURI(),
                startDateTime.format(formatter),
                endDateTime.format(formatter),
                duration
        );
    }
}