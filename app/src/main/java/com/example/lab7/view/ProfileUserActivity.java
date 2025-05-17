package com.example.lab7.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab7.R;
import com.example.lab7.controller.ProfileUserController;
import com.example.lab7.controller.ProfileUserCallback;

public class ProfileUserActivity extends AppCompatActivity {
    private ProfileUserController profileUserController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        profileUserController = new ProfileUserController();
    }

    public void calculateCPFC(View view) {
        EditText inputAge = findViewById(R.id.input_age);
        EditText inputHeight = findViewById(R.id.input_height);
        EditText inputWeight = findViewById(R.id.input_weight);
        RadioButton maleButton = findViewById(R.id.male_button);
        RadioButton femaleButton = findViewById(R.id.female_button);
        RadioButton passiveButton = findViewById(R.id.passive_button);
        RadioButton twoTimesButton = findViewById(R.id.two_times_button);
        RadioButton threeTimesButton = findViewById(R.id.three_times_button);
        RadioButton sixTimesButton = findViewById(R.id.six_times_button);
        RadioButton loseWeightButton = findViewById(R.id.lose_weight_button);
        RadioButton saveWeightButton = findViewById(R.id.save_weight_button);
        RadioButton gainWeightButton = findViewById(R.id.gain_weight_button);

        String ageText = inputAge.getText().toString();
        String heightText = inputHeight.getText().toString();
        String weightText = inputWeight.getText().toString();

        if (ageText.isEmpty() || heightText.isEmpty() || weightText.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageText);
        int height = Integer.parseInt(heightText);
        int weight = Integer.parseInt(weightText);

        double bmr;
        if (maleButton.isChecked()) {
            bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else {
            bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }

        double activityLevel;
        if (passiveButton.isChecked()) {
            activityLevel = 1.2;
        } else if (twoTimesButton.isChecked()) {
            activityLevel = 1.375;
        } else if (threeTimesButton.isChecked()) {
            activityLevel = 1.55;
        } else {
            activityLevel = 1.725;
        }

        double tdee = bmr * activityLevel;

        double calories;
        if (loseWeightButton.isChecked()) {
            calories = tdee - 200;
        } else if (saveWeightButton.isChecked()) {
            calories = tdee;
        } else {
            calories = tdee + 300;
        }

        double protein = weight * 2.2;
        double fat = weight;
        double carbs = calories * 0.6 / 4;

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userLogin = prefs.getString("username", null);

        if (userLogin != null) {
            String activity = getActivityLevelString();
            String goal = getGoalString();
            profileUserController.saveUserProfile(this, userLogin, age, height, weight, maleButton.isChecked(), activity, goal, new ProfileUserCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> Toast.makeText(ProfileUserActivity.this, message, Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(ProfileUserActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onProfileLoaded(Integer age, Integer height, Integer weight, String gender, String activityLevel, String goal) {}
            });
            profileUserController.saveRecommendedKBZHU(this, userLogin, calories, protein, fat, carbs, new ProfileUserCallback() {
                @Override
                public void onSuccess(String message) {
                    // Можно уведомить пользователя, если нужно
                }
                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(ProfileUserActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onProfileLoaded(Integer age, Integer height, Integer weight, String gender, String activityLevel, String goal) {}
            });
        }

        String result = String.format("К: %.2f; Б: %.2f г; Ж: %.2f г; У: %.2f г", calories, protein, fat, carbs);
        Intent intent = new Intent(this, NutritionActivity.class);
        intent.putExtra("calories", calories);
        intent.putExtra("protein", protein);
        intent.putExtra("fat", fat);
        intent.putExtra("carbs", carbs);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userLogin = prefs.getString("username", null);
        if (userLogin != null) {
            profileUserController.loadUserProfile(this, userLogin, new ProfileUserCallback() {
                @Override
                public void onSuccess(String message) {}
                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(ProfileUserActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onProfileLoaded(Integer age, Integer height, Integer weight, String gender, String activityLevel, String goal) {
                    runOnUiThread(() -> {
                        EditText inputAge = findViewById(R.id.input_age);
                        EditText inputHeight = findViewById(R.id.input_height);
                        EditText inputWeight = findViewById(R.id.input_weight);
                        RadioButton maleButton = findViewById(R.id.male_button);
                        RadioButton femaleButton = findViewById(R.id.female_button);
                        RadioButton passiveButton = findViewById(R.id.passive_button);
                        RadioButton twoTimesButton = findViewById(R.id.two_times_button);
                        RadioButton threeTimesButton = findViewById(R.id.three_times_button);
                        RadioButton sixTimesButton = findViewById(R.id.six_times_button);
                        RadioButton loseWeightButton = findViewById(R.id.lose_weight_button);
                        RadioButton saveWeightButton = findViewById(R.id.save_weight_button);
                        RadioButton gainWeightButton = findViewById(R.id.gain_weight_button);
                        inputAge.setText(String.valueOf(age));
                        inputHeight.setText(String.valueOf(height));
                        inputWeight.setText(String.valueOf(weight));
                        if ("Мужской".equals(gender)) {
                            maleButton.setChecked(true);
                        } else {
                            femaleButton.setChecked(true);
                        }
                        if ("Сидячий".equals(activityLevel)) {
                            passiveButton.setChecked(true);
                        } else if ("Малоподвижный".equals(activityLevel)) {
                            twoTimesButton.setChecked(true);
                        } else if ("Умеренный".equals(activityLevel)) {
                            threeTimesButton.setChecked(true);
                        } else {
                            sixTimesButton.setChecked(true);
                        }
                        if ("Похудеть".equals(goal)) {
                            loseWeightButton.setChecked(true);
                        } else if ("Поддерживать форму".equals(goal)) {
                            saveWeightButton.setChecked(true);
                        } else if ("Набрать вес".equals(goal)) {
                            gainWeightButton.setChecked(true);
                        }
                    });
                }
            });
        }
    }

    private String getGoalString() {
        RadioGroup goalGroup = findViewById(R.id.goal_group);
        int selectedId = goalGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.lose_weight_button) {
            return "Похудеть";
        } else if (selectedId == R.id.save_weight_button) {
            return "Поддерживать форму";
        } else if (selectedId == R.id.gain_weight_button) {
            return "Набрать вес";
        }
        return "";
    }

    private String getActivityLevelString() {
        RadioGroup activityGroup = findViewById(R.id.activity_group);
        int selectedId = activityGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.passive_button) {
            return "Сидячий";
        } else if (selectedId == R.id.two_times_button) {
            return "Малоподвижный";
        } else if (selectedId == R.id.three_times_button) {
            return "Умеренный";
        } else if (selectedId == R.id.six_times_button) {
            return "Подвижный";
        }
        return "";
    }
}
