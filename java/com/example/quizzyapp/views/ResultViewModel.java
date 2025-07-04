package com.example.quizzyapp.views;

import androidx.lifecycle.ViewModel;

public class ResultViewModel extends ViewModel {
    private boolean isScoreSaved = false;

    public boolean isScoreSaved() {
        return isScoreSaved;
    }

    public void setScoreSaved(boolean scoreSaved) {
        this.isScoreSaved = scoreSaved;
    }
}