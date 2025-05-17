package com.example.lab7.controller;

import android.content.Context;

import com.example.lab7.database.ConnectionClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileUserController {
    public void saveUserProfile(Context context, String login, int age, int height, int weight, boolean isMale, String activity, String goal, ProfileUserCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Connection con = null;
            PreparedStatement statement = null;
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                con = connectionClass.CONN();
                if (con != null) {
                    String gender = isMale ? "Мужской" : "Женский";
                    String sql = "INSERT INTO profileuser (login, age, gender, height, weight, activity_levels, goal) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE age = ?, gender = ?, height = ?, weight = ?, activity_levels = ?, goal = ?";
                    statement = con.prepareStatement(sql);
                    statement.setString(1, login);
                    statement.setInt(2, age);
                    statement.setString(3, gender);
                    statement.setInt(4, height);
                    statement.setInt(5, weight);
                    statement.setString(6, activity);
                    statement.setString(7, goal);
                    statement.setInt(8, age);
                    statement.setString(9, gender);
                    statement.setInt(10, height);
                    statement.setInt(11, weight);
                    statement.setString(12, activity);
                    statement.setString(13, goal);
                    int result = statement.executeUpdate();
                    if (result > 0) {
                        callback.onSuccess("Данные успешно сохранены");
                    } else {
                        callback.onError("Не удалось сохранить данные");
                    }
                } else {
                    callback.onError("Ошибка соединения с базой данных");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                callback.onError("Ошибка сохранения данных");
            } finally {
                try {
                    if (statement != null) statement.close();
                    if (con != null) con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadUserProfile(Context context, String login, ProfileUserCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Connection con = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                con = connectionClass.CONN();
                if (con == null) {
                    callback.onError("Ошибка соединения с базой данных");
                    return;
                }
                String sql = "SELECT * FROM profileuser WHERE login = ?";
                statement = con.prepareStatement(sql);
                statement.setString(1, login);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    Integer age = resultSet.getInt("age");
                    Integer height = resultSet.getInt("height");
                    Integer weight = resultSet.getInt("weight");
                    String gender = resultSet.getString("gender");
                    String activityLevel = resultSet.getString("activity_levels");
                    String goal = resultSet.getString("goal");
                    callback.onProfileLoaded(age, height, weight, gender, activityLevel, goal);
                } else {
                    callback.onError("Данные для пользователя не найдены");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                callback.onError("Ошибка загрузки данных");
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (con != null) con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void saveRecommendedKBZHU(Context context, String userLogin, double kcal, double protein, double fat, double carbs, ProfileUserCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String query = "INSERT INTO usersKBZHU (user_login, recommended_kcal, recommended_protein, recommended_fat, recommended_carbs) " +
                    "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                    "recommended_kcal = VALUES(recommended_kcal), " +
                    "recommended_protein = VALUES(recommended_protein), " +
                    "recommended_fat = VALUES(recommended_fat), " +
                    "recommended_carbs = VALUES(recommended_carbs)";
            try (Connection con = new ConnectionClass().CONN();
                 PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, userLogin);
                stmt.setDouble(2, kcal);
                stmt.setDouble(3, protein);
                stmt.setDouble(4, fat);
                stmt.setDouble(5, carbs);
                stmt.executeUpdate();
                callback.onSuccess("Рекомендованные КБЖУ сохранены");
            } catch (SQLException e) {
                e.printStackTrace();
                callback.onError("Ошибка сохранения КБЖУ");
            }
        });
    }
} 