package com.example.lab7.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

public class WeightLossChartDialog extends Dialog {
    private Context context;
    private String userLogin;
    private ConnectionClass connectionClass;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public WeightLossChartDialog(@NonNull Context context, String userLogin) {
        super(context);
        this.context = context;
        this.userLogin = userLogin;
        connectionClass = new ConnectionClass();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_weight_chart);

        LineChart lineChart = findViewById(R.id.lineChart);
        Button closeButton = findViewById(R.id.btn_close);

        closeButton.setOnClickListener(v -> dismiss());

        fetchWeightLossData(userLogin, lineChart);
    }

    private void fetchWeightLossData(String userLogin, LineChart lineChart) {
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

                    if (!dates.isEmpty() && !weights.isEmpty()) {
                        ((ViewResultsActivity) context).runOnUiThread(() -> displayChart(lineChart, dates, weights));

                    } else {
                        ((ViewResultsActivity) context).runOnUiThread(() -> Toast.makeText(getContext(), "Нет данных для отображения", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void displayChart(LineChart lineChart, List<String> dates, List<Float> weights) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < weights.size(); i++) {
            entries.add(new Entry(i, weights.get(i)));
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

        lineChart.invalidate();
    }
}
