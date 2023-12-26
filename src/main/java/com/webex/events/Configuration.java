package com.webex.events;

public class Configuration {
    private static String accessToken;

    private static int timeout = 30;
    private static int maxRetries = 5;


    public static void setAccessToken(String apiKey) {
        accessToken = apiKey;
    }
    public static String getAccessToken(){
        return accessToken;
    }

    public static int getTimeout() {
        return timeout;
    }

    public static void setTimeout(int timeout) {
        Configuration.timeout = timeout;
    }

    public static int getMaxRetries() {
        return maxRetries;
    }

    public static void setMaxRetries(int maxRetries) {
        Configuration.maxRetries = maxRetries;
    }
}
