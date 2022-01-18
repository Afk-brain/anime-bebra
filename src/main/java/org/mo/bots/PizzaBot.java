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
                .keyboardRow(createKeyboardRow("\uD83C\uDF55Меню\uD83C\uDF55", "\uD83D\uDC3EАкції\uD83D\uDC3E"))
                .keyboardRow(createKeyboardRow("\uD83D\uDCE6Кошик\uD83D\uDCE6"))
                .build();
        sendMessage(message.getChatId().toString(), "Повідомлення 1", keyboard);
    }

    @BotCommand("\uD83D\uDC3CPANDA PIZZA\uD83D\uDC3C")
    public void advertisement(Message message) {
        sendMessage(message.getChatId().toString(), "Повідомлення 2");
    }

    @BotCommand("\uD83D\uDCE6Кошик\uD83D\uDCE6")
    public void cart(Message message) {
        Cart cart = cartStore.getCart(message.getChatId().toString());
        String text = "Кошик\nТовари: ";
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        int price = 0;
        for(Pair<String, Integer> item : cart.data) {
            Product product = provider.getProductById(item.key);
            text += product.name + ", ";
            price += product.getPrice();
            builder.keyboardRow(createInlineKeyboardRow(product.name + " " + item.value + "x" + formatPrice(product.getPrice()) + " = " + formatPrice(item.value * product.getPrice()), product.name));
            builder.keyboardRow(createInlineKeyboardRow("-", "W", "+", "wdw"));
        }
        text += "\nЦіна: " + formatPrice(price);
        builder.keyboardRow(createInlineKeyboardRow("Оформити замовлення", "wdw"));
        sendMessage(message.getChatId().toString(), text,builder.build());
    }


    @BotCommand("\uD83C\uDF55Меню\uD83C\uDF55")
    public void menu(Message message) {
        sendMessage(message.getChatId().toString(), "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀Оберіть категорію\uD83C\uDF55\uD83C\uDF70\uD83C\uDF7A\uD83C\uDF75⠀⠀⠀⠀", createMenuKeyboard());
    }

    private InlineKeyboardMarkup createMenuKeyboard() {
        Category[] categories = provider.getCategories();
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        for(Category category : categories) {
            builder.keyboardRow(createInlineKeyboardRow(category.name, "showgroup_" + category.id));
        }
        return builder.build();
    }

    private void showGroup(Message message, String groupId, int page) {
        editMessageText(message.getChatId().toString(), message.getMessageId(), "Оберіть товар", createGroupPageKeyboard(groupId, page));
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
            builder.keyboardRow(createInlineKeyboardRow(text, "showproduct_" + product.id + "_" + 1));
        }
        String pageData = "grouppage_" + groupId + "_";
        List<String> pageButtons = new ArrayList<>();
        if(page != 0) {
            pageButtons.add("⬅");
            pageButtons.add(pageData + (page - 1));
        } else {
            pageButtons.add("❌");
            pageButtons.add("--");
        }
        pageButtons.add((page + 1) + "/" + pages);
        pageButtons.add("-");
        if(page < pages - 1) {
            pageButtons.add("➡");
            pageButtons.add(pageData + (page + 1));
        } else {
            pageButtons.add("❌");
            pageButtons.add("---");
        }
        builder.keyboardRow(createInlineKeyboardRow(listToArray(pageButtons)));
        builder.keyboardRow(createInlineKeyboardRow("<<Назад>>", "menu"));
        return builder.build();
    }

    private void showProduct(Message message, String id, int amountToShow) {
        Product product = provider.getProductById(id);
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        builder.keyboardRow(createInlineKeyboardRow("Додати в кошик " + formatAmount(amountToShow) + " " + formatPrice(amountToShow*product.getPrice()), "tocart_" + product.id));
        builder.keyboardRow(createInlineKeyboardRow("-", "showproduct_" + product.id + "_" + (amountToShow - 1), "+", "showproduct_" + product.id + "_" + (amountToShow + 1)));
        builder.keyboardRow(createInlineKeyboardRow("<<Назад>>", "showgroup_" + product.categoryId));
        editMessageText(message.getChatId().toString(), message.getMessageId(), product.name + "\n\nЦіна: " + formatPrice(product.getPrice()) + "[⠀⠀](https://craft-tower.joinposter.com/" + product.photo + ")", builder.build(), "MarkdownV2");
    }

    @Override
    void processCallbackQuery(CallbackQuery query) {
        String data = query.getData();
        if(data.startsWith("showgroup_")) {
            String id = data.split("_")[1];
            showGroup(query.getMessage(), id, 0);
        } else if(data.equals("menu")) {
            Message message = query.getMessage();
            editMessageText(message.getChatId().toString(), message.getMessageId(), "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀Оберіть категорію\uD83C\uDF55\uD83C\uDF7A⠀⠀⠀⠀⠀⠀⠀⠀⠀", createMenuKeyboard());
        } else if(data.startsWith("grouppage")) {
            String[] parts = data.split("_");
            showGroup(query.getMessage(), parts[1], Integer.parseInt(parts[2]));
        } else if(data.startsWith("showproduct_")) {
            String[] params = data.split("_");
            int ammount = Integer.parseInt(params[2]);
            if(ammount < 1) ammount = 1;
            else if(ammount > 5) ammount = 5;
            showProduct(query.getMessage(), params[1], ammount);
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

    private String formatAmount(int amount) {
        if(amount == 1) {
            return "1 штуку";
        } else if(amount == 2) {
            return "2 штуки";
        } else {
            return amount + " штук";
        }
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
