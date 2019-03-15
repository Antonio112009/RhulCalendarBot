/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package runable;

import Entity.Event;
import Entity.GetDay;
import actions.Cutter;
import database.Database;
import actions.General;
import listener.SendMessagesBot;
import webServer.Webpage;

import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class Task{

    private General general = new General();
    private Database database = new Database();

    void downloadCalendars(){
        LocalTime time = general.getCurrentTime();
        database = new Database();

        if(time.getHour() == 0 && time.getMinute() == 1) {
            try {
                ResultSet resultSet = database.getCalendars();
                while (resultSet.next()) {
                    String telegramId = resultSet.getString("telegram_id");
                    String url = resultSet.getString("filename");
                    new Thread(() -> {
                        try {
                            general.downloadCalendar(url, telegramId);
                            general.cutNotImportantInfo(telegramId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void cutEvents(){
        LocalTime time = general.getCurrentTime();

        if(time.getHour() == 0 && time.getMinute() == 3) {
            try {
                ResultSet resultSet = database.getCalendars();
                while (resultSet.next()) {
                    String telegramId = resultSet.getString("telegram_id");
                    new Cutter().cutterThread(telegramId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sendNotificationEveryHour() {
        String minutes = "20";
        LocalTime localTime = general.timeToNotificator(minutes);
        Database database = new Database();

        if (localTime.getMinute() == 0 && (localTime.getHour() > 8 && localTime.getHour() < 22)) {
            try {
                ResultSet resultSet = database.getCalendars();
                while (resultSet.next()) {
                    if(resultSet.getBoolean("notifications")) {
                        String telegramId = resultSet.getString("telegram_id");

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
                        String time = localTime.format(formatter) + "00";
                        //TODO: Возможно не нужно
                        System.out.println(time);

                        Event eventData = general.searchEventByTime(time, telegramId);

                        if (eventData != null) {
                            SendMessagesBot messagesBot = new SendMessagesBot();
                            messagesBot.sendEvent(telegramId, eventData, minutes);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
