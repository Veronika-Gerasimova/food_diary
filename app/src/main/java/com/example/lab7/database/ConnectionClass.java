package com.example.lab7.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    private Connection connection;
    String url = "jdbc:mysql://10.0.2.2:3306/newdb";
    String username = "root1";
    String password = "root";

    public Connection CONN (){
        try {
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}