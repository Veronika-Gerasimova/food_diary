package com.example.lab7.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab7.R;
import com.example.lab7.database.ConnectionClass;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeightLossActivity extends AppCompatActivity {
    private EditText weightInput, waistInput, otherInput;
    private Button saveButton, backButton;
    private String userLogin;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ConnectionClass connectionClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_weight_loss);

        // Получаем логин пользователя из SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userLogin = prefs.getString("username", null);

        if (userLogin == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация UI элементов
        weightInput = findViewById(R.id.editTextWeight);
        waistInput = findViewById(R.id.editTextWaist);
        otherInput = findViewById(R.id.editTextOther);
        saveButton = findViewById(R.id.buttonSave);
        backButton = findViewById(R.id.back_button);

        connectionClass = new ConnectionClass();

        // Обработчик нажатия на кнопку "Сохранить"
        saveButton.setOnClickListener(v -> {
            String weight = weightInput.getText().toString().trim();
            String waist = waistInput.getText().toString().trim();
            String other = otherInput.getText().toString().trim();

            if (TextUtils.isEmpty(weight) || TextUtils.isEmpty(waist)) {
                Toast.makeText(this, "Заполните вес и объём талии", Toast.LENGTH_SHORT).show();
                return;
            }

            saveResultsToDatabase(weight, waist, other);
        });

        // Обработчик нажатия на кнопку "Назад"
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(WeightLossActivity.this, NutritionActivity.class);
            startActivity(intent);
            finish();
        });
        fetchWeightLossData();

    }

    private void saveResultsToDatabase(String weight, String waist, String other) {
        executorService.execute(() -> {
            try (Connection con = connectionClass.CONN()) {
                if (con != null) {
                    String query = "INSERT INTO WeightLossResults (user_login, weight_loss, waist_loss, other_notes, created_at) VALUES (?, ?, ?, ?, CURRENT_DATE)";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setString(1, userLogin);
                    stmt.setString(2, weight);
                    stmt.setString(3, waist);
                    stmt.setString(4, other);
                    stmt.executeUpdate();
                    runOnUiThread(() -> Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show());
                    fetchWeightLossData();
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка подключения к базе данных", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchWeightLossData() {
        executorService.execute(() -> {
            try (Connection con = connectionClass.CONN()) {
                if (con != null) {
                    String query = "SELECT created_at, weight_loss FROM WeightLossResults WHERE user_login = ? ORDER BY created_at";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setString(1, userLogin);

                    ResultSet rs = stmt.executeQuery();

                    List<String> dates = new ArrayList<>();
                    List<Float> weights = new ArrayList<>();

                    while (rs.next()) {
                        dates.add(rs.getString("created_at"));
                        weights.add(rs.getFloat("weight_loss"));
                    }

                    runOnUiThread(() -> displayChart(dates, weights));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка подключения к базе данных", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка получения данных", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void displayChart(List<String> dates, List<Float> weights) {
        LineChart lineChart = findViewById(R.id.lineChart);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < weights.size(); i++) {
            entries.add(new Entry(i, weights.get(i))); // X - индекс, Y - вес
        }

        LineDataSet dataSet = new LineDataSet(entries, "Изменение веса");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getDescription().setText("График изменения веса");

        lineChart.invalidate(); // Обновление графика
    }

}
