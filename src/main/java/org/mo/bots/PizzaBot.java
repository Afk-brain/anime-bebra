package org.mo.bots;

import org.mo.bots.data.DataProvider;
import org.mo.bots.data.PosterProvider;
import org.mo.bots.data.cart.Cart;
import org.mo.bots.data.cart.CartStore;
import org.mo.bots.data.cart.RuntimeCartStore;
import org.mo.bots.data.objects.Category;
import org.mo.bots.data.objects.Product;
import org.mo.bots.data.session.RuntimeSessionStore;
import org.mo.bots.data.session.SessionStore;
import org.mo.bots.utils.Pair;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PizzaBot extends CommandBot{

    private static final int PAGE_SIZE = 5;
    private static final String MENU_TEXT = "⠀⠀⠀⠀Оберіть категорію\uD83C\uDF55\uD83C\uDF70\uD83C\uDF7A\uD83C\uDF75";

    private DataProvider provider;
    private CartStore cartStore;
    private SessionStore sessionStore;

    public PizzaBot() {
        provider = new PosterProvider();
        cartStore = new RuntimeCartStore();
        sessionStore = new RuntimeSessionStore();
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
        Pair<String, InlineKeyboardMarkup> menu = creteMenu(message);
        sendMessage(message.getChatId().toString(), menu.key, menu.value);
    }

    private Pair<String, InlineKeyboardMarkup> creteMenu(Message message) {
        Cart cart = cartStore.getCart(message.getChatId().toString());
        if(cart.isEmpty()) {
            return new Pair<>("Кошик порожній", null);
        }
        String text = "Кошик\nТовари: ";
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        int price = 0;
        for(Pair<String, Integer> item : cart.data) {
            Product product = provider.getProductById(item.key);
            text += product.name + ", ";
            price += product.getPrice() * item.value;
            builder.keyboardRow(createInlineKeyboardRow(product.name + " " + item.value + "x" + formatPrice(product.getPrice()) + " = " + formatPrice(item.value * product.getPrice()), product.name));
            String userId = message.getChatId().toString();
            builder.keyboardRow(createInlineKeyboardRow("-", "cartremove_" + userId + "_" + item.key, "+", "cartadd_" + userId + "_" + item.key));
        }
        text += "\nЦіна: " + formatPrice(price);
        builder.keyboardRow(createInlineKeyboardRow("Оформити замовлення", "finish"));
        return new Pair<>(text, builder.build());
    }

    @BotCommand("\uD83C\uDF55Меню\uD83C\uDF55")
    public void menu(Message message) {
        sendMessage(message.getChatId().toString(), MENU_TEXT, createMenuKeyboard());
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
        builder.keyboardRow(createInlineKeyboardRow("Додати в кошик " + formatAmount(amountToShow) + " за " + formatPrice(amountToShow*product.getPrice()), "tocart_" + product.id + "_" + amountToShow));
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
            editMessageText(message.getChatId().toString(), message.getMessageId(), MENU_TEXT, createMenuKeyboard());
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
            cartStore.addItem(query.getMessage().getChatId().toString(), ids[1], Integer.parseInt(ids[2]));
            answerCallbackQuery(query.getId(), "Додано в кошик", false);
        } else if(data.startsWith("cartremove_")) {
            String[] info = data.split("_");
            cartStore.removeItem(info[1], info[2]);
            Pair<String, InlineKeyboardMarkup> menu = creteMenu(query.getMessage());
            editMessageText(query.getMessage().getChatId().toString(), query.getMessage().getMessageId(), menu.key, menu.value);
        } else if(data.startsWith("cartadd_")) {
            String[] info = data.split("_");
            cartStore.addItem(info[1], info[2], 1);
            Pair<String, InlineKeyboardMarkup> menu = creteMenu(query.getMessage());
            editMessageText(query.getMessage().getChatId().toString(), query.getMessage().getMessageId(), menu.key, menu.value);
        } else if (data.equals("finish")) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton button = KeyboardButton.builder().text("Мій номер телефону").requestContact(true).build();
            row.add(button);
            ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder().resizeKeyboard(true).keyboardRow(row).build();
            sessionStore.putValue(query.getMessage().getChatId().toString(), "status", "phone_sent");
            sendMessage(query.getMessage().getChatId().toString(), "Введіть номер телефону, або натисніть кнопку нижче",keyboard);
        }
    }
    //endregion
    //region<Finish methods>
    @Override
    public void processPlainText(Message message) {
        String id = message.getChatId().toString();
        String status = (String)sessionStore.getValue(id, "status");
        System.out.println("Status: " + status);
        String text = message.getText();
        if(status.equals("order_type_sent")) {
            if(text.equals("Самовивіз")) {
                sendMessage(message.getChatId().toString(), "Дякуємо за замовлення!!!\nОчікуйте дзвінка від менеджера для підтвердження замовлення.");
            } else if(text.equals("Доставка")) {
                sessionStore.putValue(message.getChatId().toString(), "status", "address_sent");
                sendMessage(message.getChatId().toString(), "Вкажіть адресу доставки");
            }
        } else if(status.equals("address_sent")) {
            System.out.println(text);
            sessionStore.putValue(id, "status", "pay_type_sent");
            ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder().resizeKeyboard(true)
                    .keyboardRow(createKeyboardRow("Готівка", "Карта")).build();
            sendMessage(id, "Вкажіть тип оплати", keyboard);
        } else if(status.equals("pay_type_sent")) {
            if(text.equals("Готівка")) {
                sendMessage(message.getChatId().toString(), "Дякуємо за замовлення!!!\nОчікуйте дзвінка від менеджера для підтвердження замовлення.");
            } else if(text.equals("Карта")) {
                try {
                LabeledPrice price = LabeledPrice.builder()
                        .label("Позиція").amount(20000).build();
                SendInvoice sendInvoice = SendInvoice.builder()
                        .chatId(message.getChatId().toString())
                        .title("Замовлення №")
                        .description("Опис замовлення")
                        .providerToken("284685063:TEST:NDIwYTM4N2UyOGJh")
                        .currency("UAH")
                        .price(price)
                        .photoUrl("https://e0.edimdoma.ru/data/posts/0002/1429/21429-ed4_wide.jpg?1628275808")
                        .needName(true)
                        .needPhoneNumber(true)
                        .needShippingAddress(true)
                        .payload("2")
                        .startParameter("").build();

                    System.out.println("Аніме");
                    execute(sendInvoice);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void processContact(Message message) {
        String id = message.getChatId().toString();
        String status = (String)sessionStore.getValue(id, "status");
        System.out.println("Status: " + status);
        if(status.equals("phone_sent")) {
            String phone = message.getContact().getPhoneNumber();
            System.out.println("Телефон: " + phone);
            sessionStore.putValue(id, "status", "order_type_sent");
            ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder().resizeKeyboard(true)
                    .keyboardRow(createKeyboardRow("Доставка", "Самовивіз")).build();
            sendMessage(id, "Вкажіть тип доставки", keyboard);
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
            String text = normalizeSize(textAndData[i++]);
            InlineKeyboardButton button = InlineKeyboardButton.builder().text(text).callbackData(textAndData[i++]).build();
            row.add(button);
        }
        return row;
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        return formatter.format(price / 100);
    }

    private String normalizeSize(String text) {
        return text;
    }

    private String formatAmount(int amount) {
        if(amount == 1) {
            return "1 штуку";
        } else if(amount >= 2 && amount <= 4) {
            return amount + " штуки";
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
