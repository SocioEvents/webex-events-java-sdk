package com.webex.events;

import java.time.Duration;

public class Configuration {
    private static String accessToken;

    private static Duration timeout = Duration.ofSeconds(30);
    private static int maxRetries = 5;


    public static void setAccessToken(String apiKey) {
        accessToken = apiKey;
    }
    public static String getAccessToken(){
        return accessToken;
    }

    public static Duration getTimeout() {
        return timeout;
    }

    public static void setTimeout(Duration timeout) {
        Configuration.timeout = timeout;
    }

    public static int getMaxRetries() {
        return maxRetries;
    }

    public static void setMaxRetries(int maxRetries) {
        Configuration.maxRetries = maxRetries;
    }
}
