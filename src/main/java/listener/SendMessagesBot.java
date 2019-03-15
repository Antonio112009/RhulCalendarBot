/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package listener;

import Entity.Event;
import Entity.GetDay;
import Entity.Subject;
import database.Database;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class SendMessagesBot{

    public void sendDefault(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        message.setParseMode(ParseMode.MARKDOWN);
        try {
            new EventListener().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendEvent(String chatId, Event eventData, String notifMinutes) {
        String text = "At " + eventData.getTime()[0]  + ": " + eventData.getSubject() + " at " + eventData.getLocation() + "\n\n" +
                "Time:\n*" + eventData.getTime()[0] + " - " + eventData.getTime()[1] + "*\n" +
                "Subject:\n*" + eventData.getCourseCode() + "\n"+ eventData.getSubject() + "*\n" +
                "Type:\n*" + eventData.getType() + "*\n" +
                "Course Leader/Tutor:\n*" + eventData.getLecturer() + "*\n" +
                "Location:\n*" + eventData.getLocation() + "*";
        sendToChat(chatId, eventData, text);
    }

    public void sendEvent(String chatId, Event eventData) {
        String text = "Time:\n*" + eventData.getTime()[0] + " - " + eventData.getTime()[1] + "*\n" +
                "Subject:\n*" + eventData.getCourseCode() + "\n"+ eventData.getSubject() + "*\n" +
                "Type:\n*" + eventData.getType() + "*\n" +
                "Course Leader/Tutor:\n*" + eventData.getLecturer() + "*\n" +
                "Location:\n*" + eventData.getLocation() + "*";
        sendToChat(chatId, eventData, text);
    }

    private void sendToChat(String chatId, Event eventData, String text) {
        SendLocation sendLocation = new SendLocation();
        float[] coordinates = new Database().getCoordinates(eventData.getLocation().split("-")[0]);

        sendLocation.setLatitude(coordinates[0]);
        sendLocation.setLongitude(coordinates[1]);

        sendLocation.setChatId(Long.parseLong(chatId));

        SendMessage message = new SendMessage();
        message.setText(text);
        message.setParseMode(ParseMode.MARKDOWN);
        message.setChatId(Long.parseLong(chatId));
        try {
            new EventListener().execute(message);
            new EventListener().execute(sendLocation);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    void SendWholeDay(String chatId, List<Subject[]> eventMap, GetDay getDay){

        Subject[] subjects = eventMap.get(0);
        String text = "";

        if(getDay == GetDay.TODAY)
            text = "Todays timetable:\n\n";
        if(getDay == GetDay.TOMORROW)
            text = "Timetable for tomorrow:\n\n";

        for(int i = 9; i < 17; i++){
            if(subjects[i] != null){
                if(subjects[i] != subjects[i-1]) {
                    Subject eventData = subjects[i];
                    text += "Time:\n*" + eventData.getTime()[0] + " - " + eventData.getTime()[1] + "*\n" +
                            "Subject:\n*" + eventData.getSubject() + "*\n" +
                            "Type:\n*" + eventData.getType() + "*\n" +
                            "Location:\n*" + eventData.getLocation() + "*\n\n";
                }
            }
        }

        SendMessage message = new SendMessage();
        message.setText(text);
        message.setParseMode(ParseMode.MARKDOWN);
        message.setChatId(Long.parseLong(chatId));
        try {
            new EventListener().execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
