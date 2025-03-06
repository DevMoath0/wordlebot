package com.moath.app.config;

import com.moath.app.service.impl.BotServiceImpl;
import com.moath.app.service.impl.TrackerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordleBotInitializer {

    private final BotServiceImpl botService;

    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            log.info("Attempting to delete existing webhook...");
            DeleteWebhook deleteWebhook = new DeleteWebhook();
            boolean isWebhookDeleted = botService.execute(deleteWebhook);
            log.info("Webhook deletion status: {}", isWebhookDeleted);

            log.info("Initializing bot with long polling...");
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(botService);

            log.info("Bot successfully initialized and registered");
        } catch (TelegramApiException e) {
            log.error("Failed to initialize bot: {}", e.getMessage(), e);
            if (e.getMessage().contains("401 Unauthorized")) {
                log.error("Invalid bot token. Please verify the token and try again.");
            }
            throw new RuntimeException("Failed to initialize Telegram bot: " + e.getMessage(), e);
        }
    }
}