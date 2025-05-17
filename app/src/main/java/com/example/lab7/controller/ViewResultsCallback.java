package com.example.lab7.controller;

import com.example.lab7.model.WeightLossResult;
import java.util.List;

public interface ViewResultsCallback {
    void onResultsLoaded(List<WeightLossResult> results);
    void onSuccess(String message);
    void onError(String errorMessage);
} 