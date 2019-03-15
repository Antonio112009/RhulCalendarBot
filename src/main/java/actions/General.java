/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package actions;

import Entity.Event;
import Entity.GetDay;
import Entity.Subject;
import database.Database;
import listener.SendMessagesBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class General {

    private Database database = new Database();


    public String start(){
        return "Welcome to unofficial RHUL Calendar bot\n\n" +
                "I'm a regular student at RHUL who decided to add a web timetable to Telegram.\n\n" +
                "*ATTENTION!* For the best performance of the bot, I'm keeping data in my bot's database. What I keep?\n" +
                "- Id of this chat\n" +
                "- link to your iCalendar\n\n" +
                "*For your safety, I, as the only developer of this bot, promise not to view the links you left in the bot*\n" +
                "\n" +
                "If you agree with the permission of the bot to store the data presented above, you can continue to use " +
                "the bot. To do this, send a link to your timetable.\n" +
                "\n" +
                "How to find link?\n" +
                "1 - Log into https://webtimetables.royalholloway.ac.uk\n" +
                "2 - Go to _\"My Timetables\"_\n" +
                "3 - Select _\"Calendar Download\"_\n" +
                "4 - Press _\"View Timetable\"_\n" +
                "5 - Press _\"Android(tm) and others\"_\n" +
                "6 - Copy bold link which starts with *https://webtimetables.royalholloway.ac.uk/ical/default.aspx?...*\n" +
                "7 - Send that link here\n" +
                "\n" +
                "P.S. Thank you for at least finding my bot at Telegram. I'm really appreciate it!\n\n" +
                "Tony.\n";
    }


    public String getTodayDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate today = LocalDate.now();
        return today.format(formatter);
    }

    public String getTomorrowDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        switch (tomorrow.getDayOfWeek().getValue()){
            case 6:
                tomorrow = tomorrow.plusDays(2L);
                break;
            case 7:
                tomorrow = tomorrow.plusDays(1L);
                break;
        }

        return tomorrow.format(formatter);
    }

    public LocalTime getCurrentTime(){
        return LocalTime.now();

    }

    public LocalTime timeToNotificator(String plus){
        long minutes = Long.parseLong(plus);
        LocalTime time = getCurrentTime();
        return time.plus(Duration.of(minutes, ChronoUnit.MINUTES));
    }


    public String timeConverterTimerString(String timeInit){
        LocalTime time = timeConverterLocalTime(timeInit);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return time.format(formatter);
    }

    public LocalTime timeConverterLocalTime(String timeInit){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        return LocalTime.parse(timeInit, formatter);
    }


    public Event arrayToEvenObject(List<String> arrayData){
        Event event = new Event();
        String[] timer = new String[2];
        timer[0] = new General().timeConverterTimerString(arrayData.get(0).substring(16));
        timer[1] = new General().timeConverterTimerString(arrayData.get(1).substring(14));
        event.setTime(timer);
        event.setCourseCode(arrayData.get(2).split("::")[1].split(" ")[0]);
        event.setSubject(arrayData.get(3).split("\\|")[1].split("\\\\")[0].substring(1));
        event.setType(arrayData.get(2).split(" ")[2]);
        event.setLecturer(arrayData.get(3).split("\\\\n")[2]);
        event.setLocation(arrayData.get(4).split("::")[1]);
        return event;
    }

    public Subject arrayToSubjectObject(List<String> arrayData){
        Subject subject = new Subject();
        String[] time = new String[2];
        time[0] = new General().timeConverterTimerString(arrayData.get(0).substring(16));
        time[1] = new General().timeConverterTimerString(arrayData.get(1).substring(14));
        subject.setTime(time);
        subject.setType(arrayData.get(2).split(" ")[2]);
        subject.setSubject(arrayData.get(3).split("\\|")[1].split("\\\\")[0].substring(1));
        subject.setLocation(arrayData.get(4).split("::")[1]);
        return subject;
    }


    public void downloadCalendar(String url, String telegramId) throws SQLException {
        try {
            System.out.println(telegramId);
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream("calendars/calendars-initial/ICS_" + telegramId + ".txt");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            rbc.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    //Создает нового юзера в календаре или обновляет данные ссылки на календарь
    public String receivedCalendar(Update update){

        String telegramId = String.valueOf(update.getMessage().getFrom().getId());
        String text = update.getMessage().getText();

        if(database.userExist(telegramId)) {
            database.updateCalendar(text, telegramId);
            return "I successfully updated calendar!";
        }
        else {
            database.insertUser(telegramId, text);
            return "I successfully added you to my calendar!";
        }
    }


    //Удалить ненужную информацию из calendars-initial
    public void cutNotImportantInfo(String telegramId){
            try {
                File inputFile = new File("calendars/calendars-initial/ICS_" + telegramId + ".txt");
                File tempFile = new File("calendars/calendars-initial/ICS_" + telegramId + "_templ.txt");

                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    if(currentLine.contains("X-LIC-LOCATION")){
                        continue;
                    }

                    if (currentLine.contains("LOCATION")) {
                        writer.write("location::" + currentLine.substring(9) + System.getProperty("line.separator") + System.getProperty("line.separator"));
                    }

                    if(currentLine.contains("DTEND;TZID=Europe/London"))
                        writer.write("end::" + currentLine.split(":")[1] + System.getProperty("line.separator"));

                    if(currentLine.contains("DTSTART;TZID=Europe/London"))
                        writer.write("start::" + currentLine.split(":")[1] + System.getProperty("line.separator"));

                    if (currentLine.contains("SUMMARY"))
                        writer.write("title::" + currentLine.split(":")[1] + System.getProperty("line.separator"));

                    if(currentLine.contains("DESCRIPTION"))
                        writer.write("description::" + currentLine.split(":")[1] + System.getProperty("line.separator"));
                }
                writer.close();
                reader.close();
                boolean successful = tempFile.renameTo(inputFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    public Event searchEventByTime(String time, String telegramId){
        try {
            File inputFile = new File("calendars/calendars-today/ICS_" + telegramId + ".txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            String currentLine;
            boolean read = false;
            List<String> arrayData = new ArrayList<>();

            while ((currentLine = reader.readLine()) != null) {
                if(currentLine.startsWith("start::")) {
                    if (currentLine.split("T")[1].equals(time)) {
                        read = true;
                    }
                }
                if(read){
                    arrayData.add(currentLine);
                }

                if(currentLine.equals("") && read){
                    return new General().arrayToEvenObject(arrayData);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public void showCurrentEvent(String telegramId){
        try {
            File inputFile = new File("calendars/calendars-today/ICS_" + telegramId + ".txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            String currentLine;
            boolean read = false;
            List<String> arrayData = new ArrayList<>();

            while ((currentLine = reader.readLine()) != null) {
                LocalTime timeStart;
                LocalTime timeEnd;
                LocalTime timeNow;

                String timeEndSting = "";

                if(currentLine.startsWith("start::")){
                    timeStart = timeConverterLocalTime(currentLine.substring(16));

                    timeEndSting = reader.readLine();
                    timeEnd = timeConverterLocalTime(timeEndSting.substring(14));

                    timeNow = timeConverterLocalTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));

                    if((timeNow.isBefore(timeEnd)) && (timeNow.isAfter(timeStart))){
                        read = true;
                    }
                }

                if(read){
                    arrayData.add(currentLine);
                    if(currentLine.startsWith("start::"))
                        arrayData.add(timeEndSting);
                }

                if(currentLine.equals("") && read){
                    break;
                }
            }

            for (String arrayDatum : arrayData) {
                System.out.println(arrayDatum);
            }

            if(arrayData.size() != 0) {
                new SendMessagesBot().sendEvent(telegramId, arrayToEvenObject(arrayData));

            } else {
                new SendMessagesBot().sendDefault(telegramId, "You have no events at the moment.");
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }



    public LocalDate getMondayDate(){
        LocalDate today = LocalDate.now();
        switch (today.getDayOfWeek().getValue()){
            case 2:
                today = today.minusDays(1L);
                break;
            case 3:
                today = today.minusDays(2L);
                break;
            case 4:
                today = today.minusDays(3L);
                break;
            case 5:
                today = today.minusDays(4L);
                break;
            case 6:
                today = today.plusDays(2L);
                break;
            case 7:
                today = today.plusDays(1L);
                break;
        }
        return today;
    }


    public LocalDate getFridayDate(){
        LocalDate localDate = getMondayDate();
        return localDate.plusDays(4L);
    }


    public String getDayString(GetDay getDay) {
        if (getDay == GetDay.TODAY){
            return "today";
        }
        if (getDay == GetDay.TOMORROW){
            return "tomorrow";
        }
        if (getDay == GetDay.WEEK){
            return "week";
        }
        return null;
    }


    String getDayDateString(GetDay getDay) {
        if (getDay == GetDay.TODAY){
            return getTodayDate();
        }
        if (getDay == GetDay.TOMORROW){
            return getTomorrowDate();
        }
        return null;
    }
}
