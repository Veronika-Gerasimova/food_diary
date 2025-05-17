package com.example.lab7.controller;

public interface AuthCallback {
    void onSuccess(String message, String role, boolean isFirstLogin);
    void onError(String errorMessage);
} 