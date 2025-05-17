package com.example.lab7.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lab7.database.ConnectionClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthController {
    private ConnectionClass connectionClass;
    private Connection con;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_FIRST_LOGIN = "isFirstLogin";
    private static final String KEY_ROLE = "role";

    public AuthController() {
        connectionClass = new ConnectionClass();
    }

    public void connect(Context context, AuthCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionClass.CONN();
                if (con == null) {
                    callback.onError("Ошибка подключения к MySQL");
                } else {
                    callback.onSuccess("Подключение к MySQL установлено", null, false);
                }
            } catch (Exception e) {
                callback.onError("Ошибка подключения: " + e.getMessage());
            }
        });
    }

    public void login(Context context, String username, String password, AuthCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                if (con == null) {
                    callback.onError("Нет подключения к базе данных");
                    return;
                }
                String query = "SELECT * FROM users WHERE login=? AND password=?";
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    callback.onError("Неверный логин или пароль");
                } else {
                    String role = resultSet.getString("role");
                    boolean isFirstLogin = resultSet.getBoolean("is_first_login");

                    // Сохраняем данные пользователя в SharedPreferences
                    SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.putString(KEY_USERNAME, username);
                    editor.putString(KEY_ROLE, role);
                    editor.putBoolean(KEY_IS_FIRST_LOGIN, isFirstLogin);
                    editor.apply();

                    callback.onSuccess("Авторизация успешна", role, isFirstLogin);
                }
            } catch (SQLException ex) {
                callback.onError("Ошибка при авторизации: " + ex.getMessage());
            }
        });
    }

    public void register(Context context, String username, String password, String confirmPassword, String selectedRole, AuthCallback callback) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            callback.onError("Заполните все поля");
            return;
        }
        if (!password.equals(confirmPassword)) {
            callback.onError("Пароли не совпадают");
            return;
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                if (con == null) {
                    callback.onError("Нет подключения к базе данных");
                    return;
                }
                String checkUserSql = "SELECT * FROM users WHERE login=?";
                PreparedStatement checkUserStatement = con.prepareStatement(checkUserSql);
                checkUserStatement.setString(1, username);
                ResultSet resultSet = checkUserStatement.executeQuery();

                if (resultSet.next()) {
                    callback.onError("Пользователь с таким логином уже существует");
                } else {
                    String insertUserSql = "INSERT INTO users (login, password, role, is_first_login) VALUES (?, ?, ?, ?)";
                    PreparedStatement insertUserStatement = con.prepareStatement(insertUserSql);
                    insertUserStatement.setString(1, username);
                    insertUserStatement.setString(2, password);
                    insertUserStatement.setString(3, selectedRole);
                    insertUserStatement.setBoolean(4, true);
                    insertUserStatement.executeUpdate();

                    SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.putString(KEY_USERNAME, username);
                    editor.putString(KEY_ROLE, selectedRole);
                    editor.putBoolean(KEY_IS_FIRST_LOGIN, true);
                    editor.apply();

                    callback.onSuccess("Пользователь успешно зарегистрирован!", selectedRole, true);
                }
            } catch (SQLException ex) {
                callback.onError("Ошибка при регистрации пользователя: " + ex.getMessage());
            }
        });
    }
} 