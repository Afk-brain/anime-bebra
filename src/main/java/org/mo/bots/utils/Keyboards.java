package org.mo.bots.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class Keyboards {

    public static final ReplyKeyboardMarkup MAIN = ReplyKeyboardMarkup.builder()
            .resizeKeyboard(true)
            .keyboardRow(createKeyboardRow("\uD83D\uDC3CPANDA PIZZA\uD83D\uDC3C"))
            .keyboardRow(createKeyboardRow("\uD83C\uDF55Меню\uD83C\uDF55", "\uD83D\uDC3EАкції\uD83D\uDC3E"))
            .keyboardRow(createKeyboardRow("\uD83D\uDCE6Кошик\uD83D\uDCE6"))
            .build();

    public static final ReplyKeyboardMarkup DELIVERY = ReplyKeyboardMarkup.builder().resizeKeyboard(true)
            .keyboardRow(createKeyboardRow("Доставка", "Самовивіз")).build();

    public static final ReplyKeyboardMarkup PAY = ReplyKeyboardMarkup.builder().resizeKeyboard(true)
            .keyboardRow(createKeyboardRow("Готівка", "Карта")).build();

    private static KeyboardRow createKeyboardRow(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        for(String button : buttons) {
            row.add(button);
        }
        return row;
    }

}
