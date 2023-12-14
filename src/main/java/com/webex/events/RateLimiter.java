package com.webex.events;

import java.net.http.HttpHeaders;

public class RateLimiter {
    private static final String SECONDLY_CALL_LIMIT = "x-secondly-call-limit";
    private static final String DAILY_RETRY_AFTER = "x-daily-retry-after";
    private static final String SECONDLY_RETRY_AFTER = "x-secondly-retry-after";
    private static final String DAILY_CALL_LIMIT = "x-daily-call-limit";

    private int usedSecondBasedCost = 0;
    private int secondBasedCostThreshold = 0;
    private int usedDailyBasedCost = 0;
    private int dailyBasedCostThreshold = 0;
    private int dailyRetryAfterInSecond = 0;
    private int secondlyRetryAfterInMs = 0;
    private HttpHeaders headers;

    RateLimiter(Response response) {
        this.headers = response.headers();
        // Todo: remove this line after mocking response headers on test level.
        if (headers == null) {
            return;
        }

        String dailyCallLimit = getHeaderValue(DAILY_CALL_LIMIT);
        if (!dailyCallLimit.isEmpty()) {
            String[] parts = dailyCallLimit.split("/");
            this.usedDailyBasedCost = Integer.parseInt(parts[0]);
            this.dailyBasedCostThreshold = Integer.parseInt(parts[1]);
        }

        String secondlyCallLimit = getHeaderValue(SECONDLY_CALL_LIMIT);
        if (!secondlyCallLimit.isEmpty()) {
            String[] parts = secondlyCallLimit.split("/");
            this.usedSecondBasedCost = Integer.parseInt(parts[0]);
            this.secondBasedCostThreshold = Integer.parseInt(parts[1]);
        }

        String dailyRetryAfter = getHeaderValue(DAILY_RETRY_AFTER);
        if (!dailyRetryAfter.isEmpty()) {
            this.dailyRetryAfterInSecond = Integer.parseInt(dailyRetryAfter);
        }

        String secondlyRetryAfter = getHeaderValue(SECONDLY_RETRY_AFTER);
        if (!secondlyRetryAfter.isEmpty()) {
            this.secondlyRetryAfterInMs = Integer.parseInt(secondlyRetryAfter);
        }
    }

    int getUsedSecondBasedCost() {
        return this.usedSecondBasedCost;
    }

    int getSecondBasedCostThreshold() {
        return this.secondBasedCostThreshold;
    }

    int getUsedDailyBasedCost() {
        return this.usedDailyBasedCost;
    }

    int getDailyBasedCostThreshold() {
        return this.dailyBasedCostThreshold;
    }

    int getDailyRetryAfterInSecond() {
        return this.dailyRetryAfterInSecond;
    }

    int getSecondlyRetryAfterInMs() {
        return this.secondlyRetryAfterInMs;
    }

    private String getHeaderValue(String header) {
        String headerValue = "";

        try {
            headerValue = headers.allValues(header).get(0);
        }catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return headerValue;
    }
}
