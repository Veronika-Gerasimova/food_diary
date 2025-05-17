package com.example.lab7.controller;

import com.example.lab7.model.Message;
import java.util.List;

public interface ChatCallback {
    void onSuccess(String message);
    void onError(String errorMessage);
    void onMessagesLoaded(List<Message> messages);
} 