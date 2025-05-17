package com.example.lab7.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab7.database.ConnectionClass;
import com.example.lab7.R;
import com.example.lab7.controller.AuthController;
import com.example.lab7.controller.AuthCallback;

import java.sql.Connection;

public class MainActivity extends AppCompatActivity {
    ConnectionClass connectionClass;
    Connection con;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private boolean isRegistrationInProgress = false;
    private Spinner roleSpinner;

    private static final String ROLE_USER = "user";
    private static final String ROLE_NUTRITIONIST = "nutritionist";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_FIRST_LOGIN = "isFirstLogin";

    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionClass = new ConnectionClass();
        authController = new AuthController();
        connect();
    }
    public void connect() {
        authController.connect(this, new AuthCallback() {
            @Override
            public void onSuccess(String message, String role, boolean isFirstLogin) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void btnClick(View view) {
        usernameEditText = findViewById(R.id.editTextTextPersonName);
        passwordEditText = findViewById(R.id.editTextTextPersonName2);
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        authController.login(this, username, password, new AuthCallback() {
            @Override
            public void onSuccess(String message, String role, boolean isFirstLogin) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    if (ROLE_NUTRITIONIST.equals(role)) {
                        Intent intent = new Intent(MainActivity.this, NutritionistProfileActivity.class);
                        intent.putExtra("login", username);
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(MainActivity.this, ProfileUserActivity.class));
                    }
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void register(View view) {
        setContentView(R.layout.register);
    }

    public void next(View view) {
        roleSpinner = findViewById(R.id.roleSpinner);
        String selectedRole = roleSpinner.getSelectedItem().toString();
        usernameEditText = findViewById(R.id.editTextTextPersonName);
        passwordEditText = findViewById(R.id.editTextTextPersonName2);
        EditText confirmPasswordEditText = findViewById(R.id.editTextTextPersonName3);
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        if (isRegistrationInProgress) return;
        isRegistrationInProgress = true;
        authController.register(this, username, password, confirmPassword, selectedRole, new AuthCallback() {
            @Override
            public void onSuccess(String message, String role, boolean isFirstLogin) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    isRegistrationInProgress = false;
                    Intent intent;
                    if (role.equalsIgnoreCase("user")) {
                        intent = new Intent(MainActivity.this, ProfileUserActivity.class);
                    } else if (role.equalsIgnoreCase("nutritionist")) {
                        intent = new Intent(MainActivity.this, NutritionistProfileActivity.class);
                        intent.putExtra("login", username);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Неизвестная роль: " + role, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(intent);
                    finish();
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    isRegistrationInProgress = false;
                });
            }
        });
    }
}