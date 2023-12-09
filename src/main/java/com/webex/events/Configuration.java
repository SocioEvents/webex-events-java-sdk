package com.webex.events;

public class Configuration {
    private String accessToken;
    private byte timeout = 30;

    private byte maxRetries = 5;

    public String getAccessToken() {
        return accessToken;
    }

    public Configuration setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Configuration setTimeout(byte timeout) {
        assert timeout >= (byte) 1;
        this.timeout = timeout;
        return this;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Configuration setMaxRetries(byte maxRetries) {
        assert maxRetries >= (byte) 1;
        this.maxRetries = maxRetries;
        return this;
    }
}
