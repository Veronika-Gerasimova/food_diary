package com.example.lab7.controller;

import android.content.Context;
import com.example.lab7.database.ConnectionClass;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddProductController {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void addProduct(Context context, String name, int categoryId, double calories, double proteins, double fats, double carbohydrates, double amount, AddProductCallback callback) {
        executor.execute(() -> {
            try (Connection con = new ConnectionClass().CONN()) {
                if (con != null) {
                    String sql = "INSERT INTO products (name, category_id, calories, protein, fat, carbohydrates, amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = con.prepareStatement(sql)) {
                        statement.setString(1, name);
                        statement.setInt(2, categoryId);
                        statement.setDouble(3, calories);
                        statement.setDouble(4, proteins);
                        statement.setDouble(5, fats);
                        statement.setDouble(6, carbohydrates);
                        statement.setDouble(7, amount);
                        int rows = statement.executeUpdate();
                        if (rows > 0) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Не удалось добавить продукт");
                        }
                    }
                } else {
                    callback.onError("Ошибка соединения с БД");
                }
            } catch (SQLException e) {
                callback.onError("Ошибка при добавлении продукта: " + e.getMessage());
            }
        });
    }
} 