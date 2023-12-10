package com.webex.events;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public class ErrorResponse {
    public String message;
    public JsonNode errors;

    public JsonNode extensions;

    public String getCode() {
        if (extensions == null) {
            return "";
        }

        return extensions.get("code").textValue();
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

        JsonNode availableCost = extensions.get("dailyAvailableCost");
        return availableCost != null && availableCost.intValue() < 1;
    }

    public boolean availableCostIsReached() {
        if (extensions == null) {
            return false;
        }

        JsonNode availableCost = extensions.get("availableCost");
        return availableCost != null && availableCost.intValue() < 1;
    }
}
