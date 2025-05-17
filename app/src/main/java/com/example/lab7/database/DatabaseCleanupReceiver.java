package com.example.lab7.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseCleanupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Запрос на удаление данных из базы данных
        Connection con = null;
        PreparedStatement statement = null;
        try {
            ConnectionClass connectionClass = new ConnectionClass();
            con = connectionClass.CONN();

            if (con != null) {
                String sql = "DELETE FROM daily_nutrition WHERE date < CURDATE()";
                statement = con.prepareStatement(sql);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
