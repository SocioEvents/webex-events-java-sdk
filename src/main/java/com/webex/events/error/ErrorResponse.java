package com.webex.events.error;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public class ErrorResponse {
    public String message;
    public JsonNode errors;
    public Extensions extensions;

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        if (extensions == null) {
            return "";
        }

        return extensions.getCode();
    }

    public boolean isInvalidToken() {
        return Objects.equals(getCode(), "INVALID_TOKEN");
    }

    public boolean isTokenIsExpired() {
        return Objects.equals(getCode(), "TOKEN_IS_EXPIRED");
    }

    public boolean dailyAvailableCostIsReached() {
        if (extensions == null) {
            return false;
        }

        int availableCost = extensions.getDailyAvailableCost();
        return availableCost < 1;
    }

    public boolean availableCostIsReached() {
        if (extensions == null) {
            return false;
        }

        int availableCost = extensions.getAvailableCost();
        return availableCost < 1;
    }
}
