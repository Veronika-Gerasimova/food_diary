package com.example.lab7.controller;

import android.content.Context;
import com.example.lab7.model.Message;
import com.example.lab7.database.ConnectionClass;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Log;

public class UnansweredQuestionsController {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void loadUnansweredQuestions(Context context, UnansweredQuestionsCallback callback) {
        executor.execute(() -> {
            List<Message> questions = new ArrayList<>();
            try (Connection con = new ConnectionClass().CONN()) {
                if (con != null) {
                    String query = "SELECT m.*, np.name AS nutritionist_name, np.photo_path AS nutritionist_photo_path " +
                            "FROM Messages m " +
                            "LEFT JOIN nutritionist_profiles np ON m.nutritionist_name = np.name " +
                            "WHERE m.is_answer = 0 AND m.is_answered = 0 " +
                            "ORDER BY m.created_at DESC";
                    PreparedStatement stmt = con.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        questions.add(new Message(
                                rs.getInt("id"),
                                rs.getString("user_login"),
                                rs.getString("message_text"),
                                rs.getInt("is_answer"),
                                rs.getInt("is_answered"),
                                rs.getString("created_at"),
                                rs.getString("nutritionist_name"),
                                rs.getString("nutritionist_photo_path")
                        ));
                    }
                    rs.close();
                    stmt.close();
                    callback.onQuestionsLoaded(questions);
                } else {
                    callback.onError("Ошибка соединения с БД");
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Ошибка загрузки вопросов: " + e.getMessage());
            }
        });
    }

    public void answerQuestion(Context context, int messageId, String answerText, String nutritionistName, String nutritionistPhotoPath, UnansweredQuestionsCallback callback) {
        executor.execute(() -> {
            try (Connection con = new ConnectionClass().CONN()) {
                if (con != null) {
                    // 1. Обновить is_answered=1 для исходного сообщения
                    String updateQuery = "UPDATE Messages SET is_answered = 1 WHERE id = ?";
                    PreparedStatement updateStmt = con.prepareStatement(updateQuery);
                    updateStmt.setInt(1, messageId);
                    Log.d("UnansweredQuestions", "Обновляем is_answered для id = " + messageId);
                    int updatedRows = updateStmt.executeUpdate();
                    Log.d("UnansweredQuestions", "UPDATE выполнен, обновлено строк: " + updatedRows);
                    updateStmt.close();
                    // 2. Добавить ответ нутрициолога (is_answer=1, is_answered=1, nutritionist_name, nutritionist_photo_path)
                    String selectUserQuery = "SELECT user_login FROM Messages WHERE id = ?";
                    PreparedStatement selectStmt = con.prepareStatement(selectUserQuery);
                    selectStmt.setInt(1, messageId);
                    ResultSet rs = selectStmt.executeQuery();
                    String userLogin = null;
                    if (rs.next()) {
                        userLogin = rs.getString("user_login");
                    }
                    rs.close();
                    selectStmt.close();
                    if (userLogin != null) {
                        String insertMessageQuery = "INSERT INTO Messages (user_login, message_text, is_answer, is_answered, nutritionist_name, nutritionist_photo_path) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement insertMessageStmt = con.prepareStatement(insertMessageQuery)) {
                            insertMessageStmt.setString(1, userLogin);
                            insertMessageStmt.setString(2, answerText);
                            insertMessageStmt.setInt(3, 1);
                            insertMessageStmt.setInt(4, 1);
                            insertMessageStmt.setString(5, nutritionistName);
                            insertMessageStmt.setString(6, nutritionistPhotoPath);
                            int rowsInsertedMessage = insertMessageStmt.executeUpdate();
                            Log.d("UnansweredQuestions", "Вставка ответа нутрициолога, строк добавлено: " + rowsInsertedMessage);
                            if (rowsInsertedMessage == 0) {
                                throw new SQLException("Не удалось добавить сообщение в таблицу Messages.");
                            }
                        }
                    } else {
                        Log.d("UnansweredQuestions", "userLogin не найден для id = " + messageId);
                    }
                    // callback для обновления списка
                    if (callback != null) {
                        callback.onAnswered();
                    }
                } else {
                    callback.onError("Ошибка соединения с БД");
                }
            } catch (Exception e) {
                Log.e("UnansweredQuestions", "Ошибка при ответе: " + e.getMessage(), e);
                callback.onError("Ошибка при ответе: " + e.getMessage());
            }
        });
    }
} 