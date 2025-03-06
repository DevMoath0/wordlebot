package com.moath.app.service.impl;

import com.moath.app.service.BotService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Getter
@Setter
@Slf4j
@Component
@RequiredArgsConstructor
public class BotServiceImpl extends TelegramLongPollingBot implements BotService {

    private final TrackerServiceImpl trackerService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.groupChatId}")
    private String groupChatId;

    @Override
    public void onUpdateReceived(Update update) {

        log.debug("Received update: {}", update);

        if(!update.hasMessage()) {
            log.debug("Update has no message");
            return;
        }
        if (!update.getMessage().hasText()) {
            log.debug("Message has no text");
            return;
        }

        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        String username = getUsername(update.getMessage());

        log.debug("Message details - Text: '{}', ChatId: {}, Username: {}, Expected GroupChatId: {}",
                messageText, chatId, username, groupChatId);

        // Only process messages from the specified groupChatId
        if (!chatId.equals(groupChatId)) {
            log.debug("Message is from unauthorized chat. Expected: {}, Got: {}", groupChatId, chatId);
            return;
        }

        try {
            log.debug("Processing message from authorized chat");
            processMessage(messageText, chatId, username);
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            sendMessage(chatId, "Sorry, there was an error processing your message. Please try again.");
        }
    }

    @Override
    public void processMessage(String messageText, String chatId, String username) {
        log.debug("Processing message: '{}' from user: {} in chat: {}", messageText, username, chatId);

        String firstLine = messageText.split("\n")[0].trim();

        if (firstLine.matches("Wordle \\d+(?:,\\d+)? [1-6X]/6\\**")) {
            log.debug("Valid Wordle result detected: {}", firstLine);

            char charBeforeSlash = firstLine.charAt(firstLine.indexOf('/') - 1);
            int tries;
            boolean isLoss = false;

            if (charBeforeSlash == 'X' || charBeforeSlash == 'x') {
                tries = 7;
                isLoss = true;
            } else {
                tries = Character.getNumericValue(charBeforeSlash);
            }

            try {
                boolean recorded = trackerService.recordResult(username, username, tries);

                if (!recorded) {
                    sendMessage(chatId, "❌ @" + username + ", you have already submitted your result for today!");
                } else {
                    String emoji = getEmojiForTries(tries, isLoss);
                    String message = String.format("%s @%s's result recorded (%d tries)!",
                            emoji, username, tries);
                    sendMessage(chatId, message);
                }
            } catch (Exception e) {
                log.error("Error recording result: {}", e.getMessage(), e);
                sendMessage(chatId, "❌ Error recording result: " + e.getMessage());
            }

        } else if (messageText.equalsIgnoreCase("/leaderboard")) {
            log.debug("Leaderboard request detected");
            String leaderboard = trackerService.getLeaderboard();
            sendMessage(chatId, leaderboard);
        } else if (messageText.equalsIgnoreCase("/help")) {
            log.debug("Help request detected");
            String help = trackerService.getHelp();
            sendMessage(chatId, help);
        } else if (messageText.equalsIgnoreCase("/nafar")) {
            log.debug("Nafar request detected");
            String nafar = trackerService.getNafar();
            sendMessage(chatId, nafar);
        }
        else {
            log.debug("Message did not match any known commands: '{}'", messageText);
        }
    }

    @Override
    public String getEmojiForTries(int tries, boolean isLoss) {
        if (isLoss) return "😢";

        return switch (tries) {
            case 1 -> "🏆"; // Exceptional
            case 2 -> "🌟"; // Excellent
            case 3 -> "✨"; // Very good
            case 4 -> "👏"; // Good
            case 5 -> "👍"; // Ok
            case 6 -> "😅"; // Close call
            case 7 -> "😭"; // Didn't get the answer
            default -> "📝"; // Fallback
        };
    }

    @Override
    public String getUsername(Message message) {
        if (message.getFrom().getUserName() != null) {
            return message.getFrom().getUserName();
        }
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName() != null ? message.getFrom().getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    @Override
    public void sendMessage(String chatId, String text) {
        log.debug("Attempting to send message to chat {}: {}", chatId, text);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        try {
            execute(sendMessage);
            log.debug("Message sent successfully to chat: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    @Override
    public void onClosing() {
        log.info("Bot is shutting down");
        super.onClosing();
    }
}
