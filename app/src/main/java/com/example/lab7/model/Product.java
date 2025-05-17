package com.example.lab7.model;


import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;

public class Product  implements Serializable {
    private int id;
    private String name;
    private double kcal;
    private double protein;
    private double carbs;
    private double fat;
    private double amount;
    public Product( String name, double kcal, double protein, double fat, double carbs, double amount) {

        this.name = name;
        this.kcal = kcal;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getKcal() {
        return kcal;
    }

    public void setKcal(double kcal) {
        this.kcal = kcal;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setFats(double fat) {
        this.fat = fat;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", kcal=" + kcal +
                ", protein=" + protein +
                ", fats=" + fat  +
                ", carbs=" + carbs +
                ", amount=" + amount +
                '}';
    }


    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getCalories() {
        return kcal;
    }


}
