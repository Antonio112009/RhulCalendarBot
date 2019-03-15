/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package listener;

import Entity.GetDay;
import Entity.Subject;
import actions.Cutter;
import actions.General;
import config.BotConfig;
import database.Database;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import webServer.Webpage;

import java.sql.ResultSet;
import java.util.List;


public class EventListener extends TelegramLongPollingBot {

    private General general = new General();

    public void onUpdateReceived(Update update) {

        User user = update.getMessage().getFrom();

        if(user.getBot())
            return;

        if(update.getMessage().getText().contains("webtimetables.royalholloway.ac.uk")){
            onDefaultSend(update, general.receivedCalendar(update));
            downloadUpdateCalendars(update);
        }

        String command = update.getMessage().getText();

        if(update.getMessage().isCommand()){

            System.out.println(command);
            if(command.equals("/start")) {
                onDefaultSend(update, general.start());
                SendMessage  message = new SendMessage();
                message.setParseMode(ParseMode.MARKDOWN);
                message.setChatId(Long.valueOf(BotConfig.SPECIAL_ID));
                String date = "";
                date += "Language: " + update.getMessage().getFrom().getLanguageCode();
                date += "\nUsername: " + update.getMessage().getFrom().getUserName();
                date += "\nId: " + update.getMessage().getFrom().getId();
                date += "\nChatId: " + update.getMessage().getChatId();
                try {
                    date += "\nName: " + update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
                } catch (Exception e){
                    e.printStackTrace();
                }
                message.setText(date);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            if(command.equals("/currentevent"))
                general.showCurrentEvent(String.valueOf(update.getMessage().getChatId()));

            if(command.equals("/todayevents")){
                List<Subject[]> eventMap = new Cutter().showTimetable(update.getMessage().getChatId().toString(), GetDay.TODAY);
                new SendMessagesBot().SendWholeDay(String.valueOf(update.getMessage().getChatId()),eventMap, GetDay.TODAY);
                onDefaultSend(update, "weblink:\nhttp://" + BotConfig.SERVER_IP_PORT + "/calendars-today/web-" + update.getMessage().getChatId() + ".html");
            }

            if(command.equals("/tomorrowevents")){
                List<Subject[]> eventMap = new Cutter().showTimetable(update.getMessage().getChatId().toString(), GetDay.TOMORROW);
                new SendMessagesBot().SendWholeDay(String.valueOf(update.getMessage().getChatId()),eventMap, GetDay.TOMORROW);
                onDefaultSend(update, "weblink:\nhttp://" + BotConfig.SERVER_IP_PORT + "/calendars-tomorrow/web-" + update.getMessage().getChatId() + ".html");
            }


            if(command.equals("/weekevents"))
                onDefaultSend(update, "Here's a link to your week timetable\n\n" +
                        "http://" + BotConfig.SERVER_IP_PORT + "/calendars-week/web-" + update.getMessage().getChatId() + ".html");


            if(command.equals("/notifoff")){
                String text = "Notifications are turned off";
                new Database().updateNotifications(String.valueOf(update.getMessage().getChatId()), 0);
                new SendMessagesBot().sendDefault(String.valueOf(update.getMessage().getChatId()), text);
            }

            if(command.equals("/notifon")){
                String text = "Notifications are turned on";
                new Database().updateNotifications(String.valueOf(update.getMessage().getChatId()), 1);
                new SendMessagesBot().sendDefault(String.valueOf(update.getMessage().getChatId()), text);
            }

            // Персональный "стоп-бот"
            if((update.getMessage().getChatId().equals(Long.valueOf(BotConfig.SPECIAL_ID)))) {

                if (command.equals("/exitbot")) {
                    onDefaultSend(update, "Bot is exiting, Mr.Tony!");
                    System.exit(0);
                }

                if (command.startsWith("/upd")) {
                    if (update.getMessage().getText().split(" ").length > 3) {
                        String text = update.getMessage().getText();
                        try {
                            Database database = new Database();
                            ResultSet resultSet = database.getCalendars();
                            while (resultSet.next()) {
                                String telegramId = resultSet.getString("telegram_id");
                                new SendMessagesBot().sendDefault(telegramId, text.substring(5));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        new SendMessagesBot().sendDefault(String.valueOf(update.getMessage().getChatId()), "Need more words");
                    }
                }


                if(command.equals("/refresh")){
                    try {
                        ResultSet resultSet = new Database().getCalendars();
                        while (resultSet.next()) {
                            String telegramId = resultSet.getString("telegram_id");
                            String url = resultSet.getString("filename");
                            new Thread(() -> {
                                try {
                                    new General().downloadCalendar(url, telegramId);
                                    new General().cutNotImportantInfo(telegramId);
                                    new Cutter().cutterThread(telegramId);
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                        onDefaultSend(update, "Refresh finished successfully");
                    } catch (Exception e){
                        onDefaultSend(update, "Refresh finished unsuccessfully");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void downloadUpdateCalendars(Update update) {
        try {
            general.downloadCalendar(update.getMessage().getText(), String.valueOf(update.getMessage().getChatId()));
            general.cutNotImportantInfo(String.valueOf(update.getMessage().getChatId()));
            new Thread(() ->{
                new Cutter().cutEvents(String.valueOf(update.getMessage().getChatId()), GetDay.TODAY);
                new Webpage().generateWebpage(String.valueOf(update.getMessage().getChatId()), GetDay.TODAY);
            }).start();
            new Thread(() ->{
                new Cutter().cutEvents(String.valueOf(update.getMessage().getChatId()), GetDay.TOMORROW);
                new Webpage().generateWebpage(String.valueOf(update.getMessage().getChatId()), GetDay.TOMORROW);
            }).start();
            new Thread(() ->{
                new Cutter().cutEvents(String.valueOf(update.getMessage().getChatId()), GetDay.WEEK);
                new Webpage().generateWebpage(String.valueOf(update.getMessage().getChatId()), GetDay.WEEK);
            }).start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private void onDefaultSend(Update update, String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(update.getMessage().getChatId());
        message.setParseMode(ParseMode.MARKDOWN);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public String getBotUsername() {
        return BotConfig.USER;
    }

    public String getBotToken() {
        return BotConfig.TOKEN;
    }
}
