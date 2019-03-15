/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package database;

import config.BotConfig;

import java.sql.*;

public class DatabaseConfig {

    public Connection connect(){
        try {

            String url = BotConfig.DATABASE_URL;
            String username = BotConfig.DATABASE_USER;
            String password = BotConfig.DATABASE_PASSWORD;
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {

            e.printStackTrace();
            return null;
        }
    }

    public void newConnection(){
        try {
            String query = "create table users" +
                    "(" +
                    "telegram_id VARCHAR(255) not null," +
                    "notifications BOOLEAN not null," +
                    "filename TEXT," +
                    "primary key (telegram_id))";

            Statement statement = connect().createStatement();

            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
