package com.ebbinghaus.vocab.controller;

import com.ebbinghaus.vocab.common.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResult<Map<String, Object>> health() {
        return ApiResult.success(Map.of(
                "status", "UP",
                "system", "艾宾浩斯单词背诵学习系统",
                "time", LocalDateTime.now()
        ));
    }
}
