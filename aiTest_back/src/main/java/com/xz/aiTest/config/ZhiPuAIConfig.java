package com.xz.aiTest.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class ZhiPuAIConfig {
    @Value("${ai.key}")
    private String key;

    //spring中注册了ClientV4的bean可以直接调用
    @Bean
    public ClientV4 ZhiPuClient() {
        return new ClientV4.Builder(key).build();
    }
}
