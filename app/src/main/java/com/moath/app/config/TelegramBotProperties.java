package com.moath.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {
    private String username;
    private String token;
    private String groupChatId;
}