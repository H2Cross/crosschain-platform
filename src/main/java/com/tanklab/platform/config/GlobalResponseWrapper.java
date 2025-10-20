package com.tanklab.platform.config;

import com.tanklab.platform.util.LogCapture;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    private final HttpServletRequest request;

    public GlobalResponseWrapper(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  org.springframework.http.server.ServerHttpRequest req,
                                  org.springframework.http.server.ServerHttpResponse res) {

        // ✅ 在写出响应体前，停止捕获
        String logs = LogCapture.stop(request);
        if (logs == null) logs = "";

        if (body instanceof Map) {
            Map<?, ?> rawMap = (Map<?, ?>) body;
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                map.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            map.put("resultLogs", logs.split("\\r?\\n"));
            return map;
        }

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("data", body);
        wrapper.put("resultLogs", logs.split("\\r?\\n"));
        return wrapper;
    }
}
