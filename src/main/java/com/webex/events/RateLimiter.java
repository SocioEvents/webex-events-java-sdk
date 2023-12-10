package com.webex.events;

public class RateLimiter {
    private static final String SECONDLY_CALL_LIMIT = "x-secondly-call-limit";
    private static final String DAILY_RETRY_AFTER = "x-daily-retry-after";
    private static final String SECONDLY_RETRY_AFTER = "x-secondly-retry-after";
    private static final String DAILY_CALL_LIMIT = "x-daily-call-limit";

    private int usedSecondBasedCost = 0;
    private int secondBasedCostThreshold = 0;
    private int usedDailyBasedCost = 0;
    private int dailyBasedCostThreshold = 0;
    private int dailyRetryAfter = 0;
    private int secondlyRetryAfter = 0;

    RateLimiter(Response response) {
        // Todo: remove this line after mock response headers on test level.
        if (response.headers() == null) {
            return;
        }
        if (!response.headers().allValues(DAILY_CALL_LIMIT).isEmpty()) {
            String dailyCallLimit = response.headers().allValues(DAILY_CALL_LIMIT).get(0);
            if (dailyCallLimit != null && !dailyCallLimit.isEmpty()) {
                String[] parts = dailyCallLimit.split("/");
                this.usedDailyBasedCost = Integer.parseInt(parts[0]);
                this.dailyBasedCostThreshold = Integer.parseInt(parts[1]);
            }
        }

        if (!response.headers().allValues(SECONDLY_CALL_LIMIT).isEmpty()) {
            String secondlyCallLimit = response.headers().allValues(SECONDLY_CALL_LIMIT).get(0);
            if (secondlyCallLimit != null && !secondlyCallLimit.isEmpty()) {
                String[] parts = secondlyCallLimit.split("/");
                this.usedSecondBasedCost = Integer.parseInt(parts[0]);
                this.secondBasedCostThreshold = Integer.parseInt(parts[1]);
            }
        }

        if (!response.headers().allValues(DAILY_RETRY_AFTER).isEmpty()) {
            String dailyRetryAfter = response.headers().allValues(DAILY_RETRY_AFTER).get(0);
            this.dailyRetryAfter = Integer.parseInt(dailyRetryAfter);
        }

        if (!response.headers().allValues(SECONDLY_RETRY_AFTER).isEmpty()) {
            String secondlyRetryAfter = response.headers().allValues(SECONDLY_RETRY_AFTER).get(0);
            this.secondlyRetryAfter = Integer.parseInt(secondlyRetryAfter);
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

    int getDailyRetryAfter() {
        return this.dailyRetryAfter;
    }

    int getSecondlyRetryAfter() {
        return this.secondlyRetryAfter;
    }
}
