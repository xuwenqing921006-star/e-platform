package com.ruoyi.web.controller.system;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;

/**
 * 健康检查端点。
 */
@Anonymous
@RestController
public class HealthController
{
    @GetMapping("/health")
    public Map<String, Object> health()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }
}
