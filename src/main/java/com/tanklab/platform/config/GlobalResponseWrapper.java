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
    private final ObjectMapper objectMapper; // 用于把任意对象转成Map
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public GlobalResponseWrapper(HttpServletRequest request, ObjectMapper objectMapper) {
        this.request = request;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // 拦截所有返回
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  org.springframework.http.server.ServerHttpRequest req,
                                  org.springframework.http.server.ServerHttpResponse res) {

        // 1) 先把耗时打印出来（这样会被日志捕获到）
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

        // 2) 停止捕获，得到多行日志字符串
        String logs = LogCapture.stop(request);
        if (logs == null) logs = "";
        logs = logs.trim();

        // 3) 统一把返回体转成 Map，再在同一层级增加 result 字段
        try {
            Map<String, Object> flat = new LinkedHashMap<>();

            if (body instanceof Map) {
                // 3a) 如果本身就是Map，安全复制一份
                Map<?, ?> raw = (Map<?, ?>) body;
                for (Map.Entry<?, ?> e : raw.entrySet()) {
                    flat.put(String.valueOf(e.getKey()), e.getValue());
                }
            } else if (body != null && !(body instanceof String)) {
                // 3b) 如果是POJO，转成Map
                Map<String, Object> converted = objectMapper.convertValue(body, new TypeReference<Map<String, Object>>() {});
                if (converted != null) {
                    flat.putAll(converted);
                }
            } else {
                // 3c) 如果是String（极少数接口可能返回纯字符串），包装成扁平结构
                flat.put("code", "0");
                flat.put("msg", "success");
                flat.put("data", body);
            }

            // 同级追加 result 字段
            flat.put("result", logs);
            return flat;

        } catch (Exception ex) {
            // 兜底：转换失败时，仍保证返回扁平结构
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("code", "0");
            fallback.put("msg", "success");
            fallback.put("data", body);
            fallback.put("result", logs);
            return fallback;
        }
    }
}
