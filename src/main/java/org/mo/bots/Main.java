package org.mo.bots;

import org.mo.bots.data.MySql;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    public static void main(String[] args) {
        try {
            MySql.execute("");

            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            PizzaBot pizzaBot = new PizzaBot();
            telegramBotsApi.registerBot(pizzaBot);
            System.out.println("PIZZA BOT INITIALIZED");
        } catch (Exception e) {
            System.out.println("BOT INITIALIZATION FAILED");
            e.printStackTrace();
        }
    }

}

