package com.example.lab7.controller;

public interface ProfileUserCallback {
    void onSuccess(String message);
    void onError(String errorMessage);
    // Для загрузки профиля
    void onProfileLoaded(Integer age, Integer height, Integer weight, String gender, String activityLevel, String goal);
} 