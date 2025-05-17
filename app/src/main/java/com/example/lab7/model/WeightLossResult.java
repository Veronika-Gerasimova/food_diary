package com.example.lab7.model;

public class WeightLossResult {
    private int id;
    private String userLogin;
    private double weightLoss;
    private double waistLoss;
    private String otherNotes;

    public WeightLossResult(int id, String userLogin, double weightLoss, double waistLoss, String otherNotes) {
        this.id = id;
        this.userLogin = userLogin;
        this.weightLoss = weightLoss;
        this.waistLoss = waistLoss;
        this.otherNotes = otherNotes;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public double getWeightLoss() {
        return weightLoss;
    }

    public void setWeightLoss(double weightLoss) {
        this.weightLoss = weightLoss;
    }

    public double getWaistLoss() {
        return waistLoss;
    }

    public void setWaistLoss(double waistLoss) {
        this.waistLoss = waistLoss;
    }

    public String getOtherNotes() {
        return otherNotes;
    }

    public void setOtherNotes(String otherNotes) {
        this.otherNotes = otherNotes;
    }

    // Переопределение метода toString для удобного вывода
    @Override
    public String toString() {
        return "WeightLossResult{" +
                "id=" + id +
                ", userLogin='" + userLogin + '\'' +
                ", weightLoss=" + weightLoss +
                ", waistLoss=" + waistLoss +
                ", otherNotes='" + otherNotes + '\'' +
                '}';
    }
}
