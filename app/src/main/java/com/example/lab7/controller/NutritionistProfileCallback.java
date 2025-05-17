package com.example.lab7.controller;

public interface NutritionistProfileCallback {
    void onSuccess(String message);
    void onError(String errorMessage);
    void onProfileLoaded(String name, String photoPath);
} 