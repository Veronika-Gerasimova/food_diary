package com.example.lab7.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab7.R;
import com.example.lab7.view.adapter.ResultsAdapter;
import com.example.lab7.model.WeightLossResult;
import com.example.lab7.controller.ViewResultsController;
import com.example.lab7.controller.ViewResultsCallback;

import java.util.List;

public class ViewResultsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ResultsAdapter adapter;
    private ViewResultsController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_results);

        recyclerView = findViewById(R.id.recycler_view_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button backButton = findViewById(R.id.btn_back);

        controller = new ViewResultsController();
        loadResults();

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(ViewResultsActivity.this, NutritionistActivity.class));
            finish();
        });
    }

    private void loadResults() {
        controller.loadResults(this, new ViewResultsCallback() {
            @Override
            public void onResultsLoaded(List<WeightLossResult> results) {
                runOnUiThread(() -> {
                    adapter = new ResultsAdapter(results, ViewResultsActivity.this);
                    recyclerView.setAdapter(adapter);
                });
            }
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ViewResultsActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadResults();
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(ViewResultsActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void sendRecommendation(int resultId, String userLogin, String recommendation) {
        // Получаем имя и фото нутрициолога из SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String nutritionistName = prefs.getString("username", "");
        String nutritionistPhotoPath = prefs.getString("photo_path", "");
        controller.sendRecommendation(this, resultId, userLogin, recommendation, nutritionistName, nutritionistPhotoPath, new ViewResultsCallback() {
            @Override
            public void onResultsLoaded(List<WeightLossResult> results) {}
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ViewResultsActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadResults();
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(ViewResultsActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}