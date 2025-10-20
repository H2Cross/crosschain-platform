package com.tanklab.platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanklab.platform.util.LogCapture;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public GlobalResponseWrapper(HttpServletRequest request, ObjectMapper objectMapper) {
        this.request = request;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 拦截所有接口响应
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  org.springframework.http.server.ServerHttpRequest req,
                                  org.springframework.http.server.ServerHttpResponse res) {

        // ✅ 1️⃣ 计算接口耗时（输出到控制台并捕获进日志）
        Object startTimeObj = request.getAttribute("startTime");
        if (startTimeObj != null) {
            long startTime = (long) startTimeObj;
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
            LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

            System.out.printf("接口调用 - 路径: %s, 开始时间: %s, 结束时间: %s, 耗时: %dms%n",
                    request.getRequestURI(),
                    startDateTime.format(formatter),
                    endDateTime.format(formatter),
                    duration);
        }

        // ✅ 2️⃣ 停止日志捕获（其他接口即便不返回 result，也会停止捕获）
        String logs = LogCapture.stop(request);
        if (logs == null) logs = "";
        logs = logs.trim();

        // ✅ 3️⃣ 默认：不改变原接口结构
        Object finalBody = body;

        try {
            // ✅ 仅在指定接口生效
            String path = request.getRequestURI();
            if (path != null && path.equals("/platform/crosschain/execute/full")) {
                // 转为Map结构以便插入字段
                Map<String, Object> root;
                if (body instanceof Map) {
                    root = new LinkedHashMap<>();
                    Map<?, ?> raw = (Map<?, ?>) body;
                    for (Map.Entry<?, ?> e : raw.entrySet()) {
                        root.put(String.valueOf(e.getKey()), e.getValue());
                    }
                } else {
                    root = objectMapper.convertValue(body, new TypeReference<Map<String, Object>>() {});
                }

                // 获取 data 字段并写入 result
                Object dataObj = root.get("data");
                if (dataObj instanceof Map) {
                    Map<String, Object> dataMap = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> e : ((Map<?, ?>) dataObj).entrySet()) {
                        dataMap.put(String.valueOf(e.getKey()), e.getValue());
                    }
                    dataMap.put("result", logs);
                    root.put("data", dataMap);
                } else {
                    // data 不是 Map（极少见），则包装成 Map
                    Map<String, Object> dataMap = new LinkedHashMap<>();
                    dataMap.put("value", dataObj);
                    dataMap.put("result", logs);
                    root.put("data", dataMap);
                }

                finalBody = root; // 替换返回体
            }
        } catch (Exception e) {
            e.printStackTrace(); // 避免吞异常
        }

        return finalBody;
    }
}
