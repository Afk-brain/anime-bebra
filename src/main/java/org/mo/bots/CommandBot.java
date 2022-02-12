package org.mo.bots;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
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
            for(Map.Entry<String, Method> method : methods.entrySet()) {
                if (input.equals(method.getKey())) {
                    try {
                        method.getValue().invoke(this, update.getMessage());
                        return;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
            processPlainText(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update.getCallbackQuery());
        } else if(update.hasMessage() &&  update.getMessage().hasContact()) {
            processContact(update.getMessage());
        } else if(update.hasPreCheckoutQuery()) {
            processPreCheckoutQuery(update.getPreCheckoutQuery());
        } else if(update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {
            processSuccessfulPayment(update.getMessage());
        }
    }
    //endregion
    //region<Action methods>
    abstract void processPlainText(Message message);
    abstract void processCallbackQuery(CallbackQuery query);
    abstract void processContact(Message message);
    abstract void processSuccessfulPayment(Message message);
    abstract void processPreCheckoutQuery(PreCheckoutQuery query);
    //endregion
    //region<Support methods>
    protected void sendMessage(String chatId, String text) {
        sendMessage(chatId, text, null);
    }

    protected void sendMessage(String chatId, String text, ReplyKeyboard keyboard) {
        sendMessage(chatId, text, keyboard, null);
    }

    protected void sendMessage(String chatId, String text, ReplyKeyboard keyboard, String markdown) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode(markdown)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

    protected void deleteMessage(String chatId, Integer messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
    }

    protected void editMessageText(String chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        editMessageText(chatId, messageId, text, keyboard, null);
    }

    protected void editMessageText(String chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard, String parseMode) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .parseMode(parseMode)
                .replyMarkup(keyboard).build();
        try {
            execute(editMessage);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

    protected void editMessageMedia(String chatId, Integer messageId, String url) {
        EditMessageMedia editMessage = EditMessageMedia.builder()
                .chatId(chatId)
                .messageId(messageId)
                .media(InputMediaPhoto.builder().media(url).build()).build();
        try {
            execute(editMessage);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

    protected void answerCallbackQuery(String queryId, String text, boolean alert) {
        AnswerCallbackQuery answerQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(queryId)
                .text(text)
                .showAlert(alert).build();
        try {
            execute(answerQuery);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }
    //endregion
}