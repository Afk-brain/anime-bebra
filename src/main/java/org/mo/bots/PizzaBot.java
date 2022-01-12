package org.mo.bots;

import org.mo.bots.data.DataProvider;
import org.mo.bots.data.PosterProvider;
import org.mo.bots.data.objects.Category;
import org.mo.bots.data.objects.Product;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PizzaBot extends CommandBot{

    private DataProvider provider;

    public PizzaBot() {
        provider = new PosterProvider();
    }

    //region<Main methods>
    @BotCommand("/start")
    public void start(Message message) {
        ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboardRow(createKeyboardRow("\uD83D\uDC3CPANDA PIZZA\uD83D\uDC3C"))
                .keyboardRow(createKeyboardRow("\uD83C\uDF55Меню", "\uD83D\uDC3EАкції"))
                .keyboardRow(createKeyboardRow("\uD83D\uDCE6Кошик"))
                .build();
        sendMessage(message.getChatId().toString(), "Повідомлення 1", keyboard);
    }

    @BotCommand("\uD83D\uDC3CPANDA PIZZA\uD83D\uDC3C")
    public void advertisement(Message message) {
        sendMessage(message.getChatId().toString(), "Повідомлення 2");
    }

    @BotCommand("\uD83C\uDF55Меню")
    public void menu(Message message) {
        Category[] categories = provider.getCategories();
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for(Category category : categories) {
            builder.keyboardRow(createInlineKeyboardRow(category.name, "showgroup_" + category.id));
        }
        sendMessage(message.getChatId().toString(), "Оберіть категорію", builder.build());
    }

    private void showGroup(Message message, String groupId) {
        Product[] products = provider.getProductsByCategory(groupId);
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for(Product product : products) {
            builder.keyboardRow(createInlineKeyboardRow(product.name + " " + product.getPrice(), "showproduct_" + product.id));
        }
        builder.keyboardRow(createInlineKeyboardRow("<<Назад>>", "menu"));
        sendMessage(message.getChatId().toString(), "Оберіть категорію", builder.build());
    }

    @Override
    void processCallbackQuery(CallbackQuery query) {
        String data = query.getData();
        if(data.startsWith("showgroup_")) {
            String id = data.split("_")[1];
            showGroup(query.getMessage(), id);
        } else if(data.equals("menu")) {
            menu(query.getMessage());
        }
    }
    //endregion
    //region<Support methods>
    private KeyboardRow createKeyboardRow(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        for(String button : buttons) {
            row.add(button);
        }
        return row;
    }

    private List<InlineKeyboardButton> createInlineKeyboardRow(String... textAndData) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        for(int i = 0;i < textAndData.length;) {
            InlineKeyboardButton button = InlineKeyboardButton.builder().text(textAndData[i++]).callbackData(textAndData[i++]).build();
            row.add(button);
        }
        return row;
    }
    //endregion
    //region<Settings methods>
    @Override
    public String getBotUsername() {
        return System.getenv("BotUsername");
    }

    @Override
    public String getBotToken() {
        return System.getenv("BotToken");
    }
    //endregion
}
