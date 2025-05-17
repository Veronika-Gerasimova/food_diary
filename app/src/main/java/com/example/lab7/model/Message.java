package com.example.lab7.model;

public class Message {
    private int id;
    private String userLogin;
    private String messageText;
    private int isAnswer;      // 0 — вопрос, 1 — ответ
    private int isAnswered;    // 0 — неотвечен, 1 — отвечен
    private String createdAt;
    private String nutritionistName;
    private String nutritionistPhotoPath;


    public Message(int id, String userLogin, String messageText, int isAnswer, int isAnswered, String createdAt, String nutritionistName, String nutritionistPhotoPath) {
        this.id = id;
        this.userLogin = userLogin;
        this.messageText = messageText;
        this.isAnswer = isAnswer;
        this.isAnswered = isAnswered;
        this.createdAt = createdAt;
        this.nutritionistName = nutritionistName;
        this.nutritionistPhotoPath = nutritionistPhotoPath;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getNutritionistName() {
        return nutritionistName;
    }

    public String getNutritionistPhotoPath() {
        return nutritionistPhotoPath;
    }
    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public int getIsAnswer() {
        return isAnswer;
    }

    public void setIsAnswer(int isAnswer) {
        this.isAnswer = isAnswer;
    }

    public int getIsAnswered() {
        return isAnswered;
    }

    public void setIsAnswered(int isAnswered) {
        this.isAnswered = isAnswered;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
