package com.webex.events;

public class RateLimiter {
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
        if (!response.headers().allValues("x-daily-call-limit").isEmpty()) {
            String dailyCallLimit = response.headers().allValues("x-daily-call-limit").get(0);
            if (dailyCallLimit != null && !dailyCallLimit.isEmpty()) {
                String[] parts = dailyCallLimit.split("/");
                this.usedDailyBasedCost = Integer.parseInt(parts[0]);
                this.dailyBasedCostThreshold = Integer.parseInt(parts[1]);
            }
        }

        if (!response.headers().allValues("x-secondly-call-limit").isEmpty()) {
            String secondlyCallLimit = response.headers().allValues("x-secondly-call-limit").get(0);
            if (secondlyCallLimit != null && !secondlyCallLimit.isEmpty()) {
                String[] parts = secondlyCallLimit.split("/");
                this.usedSecondBasedCost = Integer.parseInt(parts[0]);
                this.secondBasedCostThreshold = Integer.parseInt(parts[1]);
            }
        }

        if (!response.headers().allValues("x-daily-retry-after").isEmpty()) {
            String dailyRetryAfter = response.headers().allValues("x-daily-retry-after").get(0);
            this.dailyRetryAfter = Integer.parseInt(dailyRetryAfter);
        }

        if (!response.headers().allValues("x-secondly-retry-after").isEmpty()) {
            String secondlyRetryAfter = response.headers().allValues("x-secondly-retry-after").get(0);
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
