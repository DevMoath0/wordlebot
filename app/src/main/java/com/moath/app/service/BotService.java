package com.moath.app.service;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotService {

    void processMessage(String messageText, String chatId, String username);

    String getEmojiForTries(int tries, boolean isLoss);

    String getUsername(Message message);

    void sendMessage(String chatId, String text);

}
