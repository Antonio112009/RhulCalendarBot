/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Database {

    private Connection connection;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;

    public void insertUser(String telegramId, String filename){
        try {
            connection = new DatabaseConfig().connect();
            String query = "INSERT INTO `users` (`telegram_id`, `notifications`, `filename`) VALUES ('"+ telegramId + "'," + true +", '"+ filename +"')";

            statement = connection.createStatement();
            statement.executeUpdate(query);
            connection.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean userExist(String telegramId){
        try {
            connection = new DatabaseConfig().connect();
            String query = "SELECT * FROM users WHERE telegram_id = '" + telegramId + "'";
            preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void updateCalendar(String url, String telegramId){
        try {
            connection = new DatabaseConfig().connect();
            String query = "UPDATE users SET filename = '" + url + "' WHERE telegram_id = '" + telegramId + "'";

            statement = connection.createStatement();
            statement.executeUpdate(query);
            connection.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public ResultSet getCalendars(){
        try {
            connection = new DatabaseConfig().connect();
            String query = "SELECT telegram_id, filename, notifications FROM users WHERE telegram_id ";
            preparedStatement = connection.prepareStatement(query);
            return preparedStatement.executeQuery();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public float[] getCoordinates(String buildingName){
        try {
            connection = new DatabaseConfig().connect();
            float[] coordinates = new float[2];

            String query = "SELECT Latitude, Longitude FROM buildings WHERE name_build= '" + buildingName +"'";
            preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                coordinates[0] = resultSet.getBigDecimal("Latitude").floatValue();
                coordinates[1] = resultSet.getBigDecimal("Longitude").floatValue();
            }
            connection.close();
            return coordinates;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void updateNotifications(String chatId, int answer){
        try {
            connection = new DatabaseConfig().connect();
            String query = "UPDATE users SET notifications = " + answer + " WHERE telegram_id = '" + chatId + "'";
            statement = connection.createStatement();
            statement.executeUpdate(query);
            connection.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
