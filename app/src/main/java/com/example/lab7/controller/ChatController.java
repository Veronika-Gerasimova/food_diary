package com.example.lab7.controller;

import android.content.Context;
import com.example.lab7.model.Message;
import com.example.lab7.database.ConnectionClass;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatController {
    private Connection con;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ConnectionClass connectionClass = new ConnectionClass();

    public void connect(Context context, ChatCallback callback) {
        executorService.execute(() -> {
            try {
                con = connectionClass.CONN();
                if (con != null) {
                    callback.onSuccess("Подключено к MySQL");
                } else {
                    callback.onError("Ошибка подключения");
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Ошибка подключения: " + e.getMessage());
            }
        });
    }

    public void insertMessage(String userLogin, String text, int isAnswer, int isAnswered, ChatCallback callback) {
        executorService.execute(() -> {
            try {
                if (con != null) {
                    String query = "INSERT INTO Messages (user_login, message_text, is_answer, is_answered) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setString(1, userLogin);
                    stmt.setString(2, text);
                    stmt.setInt(3, isAnswer);
                    stmt.setInt(4, isAnswered);
                    stmt.executeUpdate();
                    stmt.close();
                    callback.onSuccess("Сообщение отправлено");
                } else {
                    callback.onError("Нет соединения с БД");
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Ошибка при отправке сообщения: " + e.getMessage());
            }
        });
    }

    public void loadMessages(String userLogin, ChatCallback callback) {
        executorService.execute(() -> {
            List<Message> messages = new ArrayList<>();
            try {
                if (con != null) {
                    String query = "SELECT m.*, np.name AS nutritionist_name, np.photo_path AS nutritionist_photo_path " +
                            "FROM Messages m " +
                            "LEFT JOIN nutritionist_profiles np ON m.nutritionist_name = np.name " +
                            "WHERE m.user_login = ?";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setString(1, userLogin);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        Message message = new Message(
                                rs.getInt("id"),
                                rs.getString("user_login"),
                                rs.getString("message_text"),
                                rs.getInt("is_answer"),
                                rs.getInt("is_answered"),
                                rs.getString("created_at"),
                                rs.getString("nutritionist_name"),
                                rs.getString("nutritionist_photo_path")
                        );
                        messages.add(message);
                    }
                    rs.close();
                    stmt.close();
                    callback.onMessagesLoaded(messages);
                } else {
                    callback.onError("Нет соединения с БД");
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Ошибка загрузки сообщений: " + e.getMessage());
            }
        });
    }
} 