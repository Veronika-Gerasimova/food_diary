package com.example.lab7.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.example.lab7.R;

public class NutritionistActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutritionist);

        Button btnViewQuestions = findViewById(R.id.btn_view_questions);
        btnViewQuestions.setOnClickListener(v -> {
            Intent intent = new Intent(NutritionistActivity.this, UnansweredQuestionsActivity.class);
            startActivity(intent);
        });
        Button btnViewRecommend = findViewById(R.id.btn_recommendations);
        btnViewRecommend.setOnClickListener(v -> {
            Intent intent = new Intent(NutritionistActivity.this, ViewResultsActivity.class);
            startActivity(intent);
        });
    }
}
