package com.webex.events;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public class ErrorResponse {
    public String message;
    public JsonNode extensions;

    public String getCode() {
        return extensions.get("code").textValue();
    }

    public boolean getIsInvalidToken() {
        return Objects.equals(getCode(), "INVALID_TOKEN");
    }

    public boolean getIsTokenIsExpired() {
        return Objects.equals(getCode(), "TOKEN_IS_EXPIRED");
    }

    public boolean getDailyAvailableCostIsReached() {
        if (extensions != null){
            JsonNode availableCost = extensions.get("dailyAvailableCost");
            return availableCost != null && availableCost.intValue() < 1;
        }else {
            return false;
        }
    }

    public boolean getAvailableCostIsReached() {
        if (extensions != null) {
            JsonNode availableCost = extensions.get("availableCost");
            return availableCost != null && availableCost.intValue() < 1;
        }else {
            return false;
        }
    }
}
