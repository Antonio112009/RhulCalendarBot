/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

import listener.EventListener;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import runable.ThreadConfig;

public class Launch {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new EventListener());
            new ThreadConfig().start();
            System.out.println("Bot is operating, Mr.Tony");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
