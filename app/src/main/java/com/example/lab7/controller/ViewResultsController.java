package com.example.lab7.controller;

import android.content.Context;
import com.example.lab7.model.WeightLossResult;
import com.example.lab7.database.ConnectionClass;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewResultsController {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void loadResults(Context context, ViewResultsCallback callback) {
        executor.execute(() -> {
            List<WeightLossResult> results = new ArrayList<>();
            try (Connection con = new ConnectionClass().CONN()) {
                String query = "SELECT * FROM WeightLossResults WHERE is_processed = FALSE";
                PreparedStatement stmt = con.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    results.add(new WeightLossResult(
                            rs.getInt("id"),
                            rs.getString("user_login"),
                            rs.getDouble("weight_loss"),
                            rs.getDouble("waist_loss"),
                            rs.getString("other_notes")
                    ));
                }
                stmt.close();
                rs.close();
                callback.onResultsLoaded(results);
            } catch (Exception e) {
                callback.onError("Ошибка загрузки результатов: " + e.getMessage());
            }
        });
    }

    public void sendRecommendation(Context context, int resultId, String userLogin, String recommendation, String nutritionistName, String nutritionistPhotoPath, ViewResultsCallback callback) {
        executor.execute(() -> {
            try (Connection con = new ConnectionClass().CONN()) {
                if (con == null) throw new SQLException("Ошибка подключения к базе данных.");

                // Проверяем, существует ли пользователь с таким login в таблице users
                String checkUserQuery = "SELECT COUNT(*) FROM users WHERE login = ?";
                try (PreparedStatement checkStmt = con.prepareStatement(checkUserQuery)) {
                    checkStmt.setString(1, userLogin);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        throw new SQLException("Пользователь с логином " + userLogin + " не существует.");
                    }
                }

                // Сохранение рекомендации
                String insertQuery = "INSERT INTO WeightLossRecommendations (user_login, recommendation) VALUES (?, ?)";
                try (PreparedStatement insertStmt = con.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, userLogin);
                    insertStmt.setString(2, recommendation);
                    int rowsInserted = insertStmt.executeUpdate();
                    if (rowsInserted == 0) {
                        throw new SQLException("Не удалось добавить рекомендацию в таблицу WeightLossRecommendations.");
                    }
                }

                // Обновление статуса результата
                String updateQuery = "UPDATE WeightLossResults SET is_processed = TRUE WHERE id = ?";
                try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, resultId);
                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new SQLException("Не удалось обновить статус в таблице WeightLossResults.");
                    }
                }

                // Добавление сообщения с именем и фото нутрициолога
                String insertMessageQuery = "INSERT INTO Messages (user_login, message_text, is_answer, is_answered, nutritionist_name, nutritionist_photo_path) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertMessageStmt = con.prepareStatement(insertMessageQuery)) {
                    insertMessageStmt.setString(1, userLogin);
                    insertMessageStmt.setString(2, recommendation);
                    insertMessageStmt.setInt(3, 1);
                    insertMessageStmt.setInt(4, 1);
                    insertMessageStmt.setString(5, nutritionistName);
                    insertMessageStmt.setString(6, nutritionistPhotoPath);
                    int rowsInsertedMessage = insertMessageStmt.executeUpdate();
                    if (rowsInsertedMessage == 0) {
                        throw new SQLException("Не удалось добавить сообщение в таблицу Messages.");
                    }
                }

                callback.onSuccess("Рекомендация отправлена");
            } catch (SQLException e) {
                callback.onError("Ошибка при отправке рекомендации: " + e.getMessage());
            } catch (Exception e) {
                callback.onError("Неизвестная ошибка: " + e.getMessage());
            }
        });
    }
} 