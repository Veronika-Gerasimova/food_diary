package com.example.lab7.controller;

import android.content.Context;

import com.example.lab7.database.ConnectionClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NutritionistProfileController {
    public void saveProfile(Context context, String login, String name, String imagePath, NutritionistProfileCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection con = null;
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                con = connectionClass.CONN();
                if (con != null) {
                    String insertSql = "REPLACE INTO nutritionist_profiles (login, name, photo_path) VALUES (?, ?, ?)";
                    PreparedStatement stmt = con.prepareStatement(insertSql);
                    stmt.setString(1, login);
                    stmt.setString(2, name);
                    stmt.setString(3, imagePath);
                    stmt.executeUpdate();
                    stmt.close();
                    callback.onSuccess("Профиль сохранён!");
                } else {
                    callback.onError("Ошибка соединения с БД");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                callback.onError("Ошибка сохранения в БД");
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void loadProfileIfExists(Context context, String login, NutritionistProfileCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection con = null;
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                con = connectionClass.CONN();
                if (con != null) {
                    String query = "SELECT name, photo_path FROM nutritionist_profiles WHERE login = ?";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setString(1, login);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        String photoPath = rs.getString("photo_path");
                        callback.onProfileLoaded(name, photoPath);
                    } else {
                        callback.onError("Профиль не найден");
                    }
                    rs.close();
                    stmt.close();
                } else {
                    callback.onError("Ошибка соединения с БД");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                callback.onError("Ошибка загрузки профиля");
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
} 