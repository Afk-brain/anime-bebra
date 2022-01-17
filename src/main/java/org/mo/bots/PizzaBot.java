package org.mo.bots;

import org.mo.bots.data.DataProvider;
import org.mo.bots.data.PosterProvider;
import org.mo.bots.data.cart.Cart;
import org.mo.bots.data.cart.CartStore;
import org.mo.bots.data.cart.RuntimeCartStore;
import org.mo.bots.data.objects.Category;
import org.mo.bots.data.objects.Product;
import org.mo.bots.utils.Pair;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PizzaBot extends CommandBot{

    private static final int PAGE_SIZE = 5;

    private DataProvider provider;
    private CartStore cartStore;

    public PizzaBot() {
        provider = new PosterProvider();
        cartStore = new RuntimeCartStore();
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

    @BotCommand("\uD83D\uDCE6Кошик")
    public void cart(Message message) {
        Cart cart = cartStore.getCart(message.getChatId().toString());
        String text = "Кошик:\nТовари:";
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for(Pair<String, Integer> item : cart.data) {
            Product product = provider.getProductById(item.key);
            text += product.name + " ";
            builder.keyboardRow(createInlineKeyboardRow(product.name + " " + item.value, product.name));
            builder.keyboardRow(createInlineKeyboardRow("-", "W", "+", "wdw"));
        }
        builder.keyboardRow(createInlineKeyboardRow("Оформити замовлення", "wdw"));
        sendMessage(message.getChatId().toString(), text,builder.build());
    }


    @BotCommand("\uD83C\uDF55Меню")
    public void menu(Message message) {
        sendMessage(message.getChatId().toString(), "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀Оберіть категорію\uD83C\uDF55\uD83C\uDF7A⠀⠀⠀⠀⠀⠀⠀", createMenuKeyboard());
    }

    private InlineKeyboardMarkup createMenuKeyboard() {
        Category[] categories = provider.getCategories();
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for(Category category : categories) {
            builder.keyboardRow(createInlineKeyboardRow(category.name + "\uD83C\uDF55", "showgroup_" + category.id));
        }
        return builder.build();
    }

    private void showGroup(Message message, String groupId, int page) {
        editMessage(message.getChatId().toString(), message.getMessageId(), "Оберіть товар", createGroupPageKeyboard(groupId, page));
    }

    private InlineKeyboardMarkup createGroupPageKeyboard(String groupId, int page) {
        Product[] products = provider.getProductsByCategory(groupId);
        int first = page * PAGE_SIZE;
        int last = Math.min((page + 1) * PAGE_SIZE - 1, products.length - 1);
        int pages = (int)Math.ceil(products.length / (float)PAGE_SIZE);
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for(int i = first;i <= last;i++) {
            Product product = products[i];
            String text = product.name + " " + formatPrice(product.getPrice());
            builder.keyboardRow(createInlineKeyboardRow(text, "showproduct_" + product.id));
        }
        String pageData = "grouppage_" + groupId + "_";
        List<String> pageButtons = new ArrayList<>();
        if(page != 0) {
            pageButtons.add("<<");
            pageButtons.add(pageData + (page - 1));
        } else {
            pageButtons.add("x");
            pageButtons.add("--");
        }
        pageButtons.add((page + 1) + "/" + pages);
        pageButtons.add("-");
        if(page < pages - 1) {
            pageButtons.add("➡");
            pageButtons.add(pageData + (page + 1));
        } else {
            pageButtons.add("x");
            pageButtons.add("---");
        }
        builder.keyboardRow(createInlineKeyboardRow(listToArray(pageButtons)));
        builder.keyboardRow(createInlineKeyboardRow("<<Назад>>", "menu"));
        return builder.build();
    }

    private void showProduct(Message message, String id) {
        Product product = provider.getProductById(id);
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        builder.keyboardRow(createInlineKeyboardRow("В кошик", "tocart_" + product.id));
        builder.keyboardRow(createInlineKeyboardRow("<<Назад>>", "showgroup_" + product.categoryId));
        editMessage(message.getChatId().toString(), message.getMessageId(), product.name + "\nЦіна: " + formatPrice(product.getPrice()) + "\n/\n/\n/\n/\n/\n/\n/\n/", builder.build());
    }

    @Override
    void processCallbackQuery(CallbackQuery query) {
        String data = query.getData();
        if(data.startsWith("showgroup_")) {
            String id = data.split("_")[1];
            showGroup(query.getMessage(), id, 0);
        } else if(data.equals("menu")) {
            Message message = query.getMessage();
            editMessage(message.getChatId().toString(), message.getMessageId(), "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀Оберіть категорію\uD83C\uDF55\uD83C\uDF7A⠀⠀⠀⠀⠀⠀⠀⠀⠀", createMenuKeyboard());
        } else if(data.startsWith("grouppage")) {
            String[] parts = data.split("_");
            showGroup(query.getMessage(), parts[1], Integer.parseInt(parts[2]));
        } else if(data.startsWith("showproduct_")) {
            String id = data.split("_")[1];
            showProduct(query.getMessage(), id);
        } else if(data.startsWith("tocart_")) {
            String[] ids = data.split("_");
            try {
                cartStore.addItem(query.getMessage().getChatId().toString(), ids[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            answerCallbackQuery(query.getId(), "Товар додано в кошик", false);
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

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(price / 100);
    }

    private String[] listToArray(List<String> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
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
