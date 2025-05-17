package com.example.lab7.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab7.R;

import com.example.lab7.view.adapter.MessageAdapter;
import com.example.lab7.controller.ChatController;
import com.example.lab7.controller.ChatCallback;

import java.util.List;

public class ChartActivity extends AppCompatActivity {
    private String userLogin;
    public static ChartActivity instance;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private EditText userInput;
    private Button sendButton;
    private ChatController chatController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userLogin = prefs.getString("username", null);
        instance = this;
        if (userLogin == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        recyclerView = findViewById(R.id.recyclerViewMessages);
        userInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
        Button backButton = findViewById(R.id.back_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatController = new ChatController();
        connect();
        sendButton.setOnClickListener(v -> {
            String question = userInput.getText().toString().trim();
            if (!TextUtils.isEmpty(question)) {
                chatController.insertMessage(userLogin, question, 0, 0, new ChatCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> refreshMessages());
                    }
                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> Toast.makeText(ChartActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                    }
                    @Override
                    public void onMessagesLoaded(List<com.example.lab7.model.Message> messages) {}
                });
                userInput.setText("");
            }
        });
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(ChartActivity.this, NutritionActivity.class));
            finish();
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }
    private void connect() {
        chatController.connect(this, new ChatCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> Toast.makeText(ChartActivity.this, message, Toast.LENGTH_SHORT).show());
                refreshMessages();
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(ChartActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onMessagesLoaded(List<com.example.lab7.model.Message> messages) {}
        });
    }
    public void refreshMessagesFromOutside() {
        runOnUiThread(this::refreshMessages);
    }
    private void refreshMessages() {
        chatController.loadMessages(userLogin, new ChatCallback() {
            @Override
            public void onSuccess(String message) {}
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(ChartActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onMessagesLoaded(List<com.example.lab7.model.Message> messages) {
                runOnUiThread(() -> {
                    messageAdapter = new MessageAdapter(messages, userLogin);
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.scrollToPosition(messages.size() - 1);
                });
            }
        });
    }
}
