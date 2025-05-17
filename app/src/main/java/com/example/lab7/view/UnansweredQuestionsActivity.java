package com.example.lab7.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab7.R;
import com.example.lab7.view.adapter.QuestionAdapter;
import com.example.lab7.controller.UnansweredQuestionsController;
import com.example.lab7.controller.UnansweredQuestionsCallback;

import java.util.List;

public class UnansweredQuestionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuestionAdapter adapter;
    private UnansweredQuestionsController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unanswered_questions);

        recyclerView = findViewById(R.id.recyclerViewQuestions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        controller = new UnansweredQuestionsController();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, NutritionistActivity.class));
            finish();
        });

        loadUnansweredQuestions();
    }

    private void loadUnansweredQuestions() {
        controller.loadUnansweredQuestions(this, new UnansweredQuestionsCallback() {
            @Override
            public void onQuestionsLoaded(List<com.example.lab7.model.Message> questions) {
                runOnUiThread(() -> {
                    adapter = new QuestionAdapter(questions, null, UnansweredQuestionsActivity.this::loadUnansweredQuestions);
                    recyclerView.setAdapter(adapter);
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(UnansweredQuestionsActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onAnswered() {
                // Не требуется для этого случая
            }
        });
    }
}
