package org.mo.bots;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandBot extends TelegramLongPollingBot {
    //region <Main methods>
    private final Map<String, Method> methods;

    public CommandBot() {
        methods = new HashMap<>();
        Class<?> botClass = getClass();
        for (Method method : botClass.getMethods()) {
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                if (annotation instanceof BotCommand) {
                    methods.put(((BotCommand) annotation).value(), method);
                }
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String input = update.getMessage().getText();
            methods.forEach((string, method) -> {
                if (input.equals(string)) {
                    try {
                        method.invoke(this, update.getMessage());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });
        }
    }
    //endregion
    //region<Support methods>
    protected void sendMessage(String chatId, String text, ReplyKeyboard keyboard) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }
    //endregion
}