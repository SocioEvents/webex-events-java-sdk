package com.webex.events.error;

import java.util.HashMap;
import java.util.List;

public class Extensions {

    private String code;
    private int cost;
    private int availableCost;
    private int threshold;
    private int dailyThreshold;
    private int dailyAvailableCost;
    private String referenceId;
    private HashMap<String, List<String>> errors;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getAvailableCost() {
        return availableCost;
    }

    public void setAvailableCost(int availableCost) {
        this.availableCost = availableCost;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getDailyThreshold() {
        return dailyThreshold;
    }

    public void setDailyThreshold(int dailyThreshold) {
        this.dailyThreshold = dailyThreshold;
    }

    public int getDailyAvailableCost() {
        return dailyAvailableCost;
    }

    public void setDailyAvailableCost(int dailyAvailableCost) {
        this.dailyAvailableCost = dailyAvailableCost;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public HashMap<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(HashMap<String, List<String>> errors) {
        this.errors = errors;
    }
}
