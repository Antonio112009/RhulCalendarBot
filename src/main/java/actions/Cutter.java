/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package actions;

import Entity.GetDay;
import Entity.Subject;
import webServer.Webpage;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Cutter {

    private General general = new General();


    public void cutEvents(String telegramId, GetDay getDay) {
        if(getDay == GetDay.WEEK){
            cutWeek(telegramId);
            return;
        }

        String folder = general.getDayString(getDay);
        String date = general.getDayDateString(getDay);

        boolean read = false;
        try {
            File inputFile = new File("calendars/calendars-initial/ICS_" + telegramId + ".txt");
            File tempFile = new File("calendars/calendars-" + folder + "/ICS_" + telegramId + ".txt");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.startsWith("start::" + date))
                    read = true;

                if (currentLine.equals("") && read)
                    read = false;

                if (read) {
                    writer.write(currentLine + System.getProperty("line.separator"));
                    if (currentLine.startsWith("location::"))
                        writer.write(System.getProperty("line.separator"));
                }
            }
            writer.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void cutWeek(String telegramId) {
        LocalDate weekStart = general.getMondayDate();

        boolean read = false;
        try {
            File inputFile = new File("calendars/calendars-initial/ICS_" + telegramId + ".txt");
            File tempFile = new File("calendars/calendars-week/ICS_" + telegramId + ".txt");

            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            String currentLine;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            for (int i = 0; i <= 4; i++) {
                String date = weekStart.plusDays(i).format(formatter);
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.startsWith("start::" + date))
                        read = true;

                    if (currentLine.equals("") && read)
                        read = false;

                    if (read) {
                        writer.write(currentLine + System.getProperty("line.separator"));
                        if (currentLine.startsWith("location::"))
                            writer.write(System.getProperty("line.separator"));
                    }
                }
                reader.close();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void cutterThread(String telegramId){
        new Thread(() ->{
            new Cutter().cutEvents(telegramId, GetDay.TODAY);
            new Webpage().generateWebpage(telegramId, GetDay.TODAY);
        }).start();
        new Thread(() ->{
            new Cutter().cutEvents(telegramId, GetDay.TOMORROW);
            new Webpage().generateWebpage(telegramId, GetDay.TOMORROW);
        }).start();
        new Thread(() ->{
            new Cutter().cutEvents(telegramId, GetDay.WEEK);
            new Webpage().generateWebpage(telegramId, GetDay.WEEK);
        }).start();
    }


    private String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};

    public List<Subject[]> showTimetable(String telegramId, GetDay getDay){
        try {
            BufferedReader reader = null;
            Subject day = new Subject();
            String currentLine;
            boolean read = false;

            int numberRows = 0;

            if(getDay == GetDay.TODAY) {
                reader = new BufferedReader(new FileReader(new File("calendars/calendars-today/ICS_" + telegramId + ".txt")));
                day.setDayOfWeek(LocalDate.now().getDayOfWeek().name());
                numberRows = 0;
            }
            if(getDay == GetDay.TOMORROW) {
                reader = new BufferedReader(new FileReader(new File("calendars/calendars-tomorrow/ICS_" + telegramId + ".txt")));
                day.setDayOfWeek(LocalDate.now().plusDays(1).getDayOfWeek().name());
                numberRows = 0;
            }
            if(getDay == GetDay.WEEK) {
                numberRows = 4;
            }


            List<String> arrayData = new ArrayList<>();
            Subject[] subjects;
            List<Subject[]> finArray = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");


            for (int i = 0; i <= numberRows; i++){

                String date = "";
                subjects = new Subject[20];

                int time = 0;
                int timeEnd = 0;

                if(getDay == GetDay.WEEK){
                    date = general.getMondayDate().plusDays(i).format(formatter);
                    reader = new BufferedReader(new FileReader(new File("calendars/calendars-week/ICS_" + telegramId + ".txt")));
                    Subject dayWeek = new Subject();
                    dayWeek.setDayOfWeek(days[i]);
                    subjects[2] = dayWeek;
                } else {
                    subjects[2] = day;
                }

                while ((currentLine = reader.readLine()) != null) {
                    if(currentLine.startsWith("start::")) {
                        if(getDay == GetDay.WEEK) {
                            if (currentLine.split("::")[1].split("T")[0].equals(date)){
                                arrayData = new ArrayList<>();
                                time = Integer.valueOf(currentLine.split("T")[1].substring(0,2));
                                read = true;
                            }
                        } else {
                            arrayData = new ArrayList<>();
                            time = Integer.valueOf(currentLine.split("T")[1].substring(0,2));
                            read = true;
                        }
                    }

                    if(currentLine.startsWith("end::")) {
                        timeEnd = Integer.valueOf(currentLine.split("T")[1].substring(0,2));
                    }

                    if(read){
                        arrayData.add(currentLine);
                    }

                    if(currentLine.equals("") && read){
                        int diff = timeEnd - time;
                        Subject subject = general.arrayToSubjectObject(arrayData);
                        for (int j = 0; j < diff; j++){
                            subjects[time + j] = subject;
                        }
                        read = false;
                    }
                }
                finArray.add(subjects);
            }

            return finArray;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
