package com.example.lab7.controller;

import com.example.lab7.model.Message;
import java.util.List;

public interface UnansweredQuestionsCallback {
    void onQuestionsLoaded(List<Message> questions);
    void onError(String errorMessage);
    void onAnswered();
} 