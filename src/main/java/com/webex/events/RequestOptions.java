package com.webex.events;

public class RequestOptions {

    private String accessToken = Configuration.getAccessToken();
    private int maxRetries = Configuration.getMaxRetries();
    private int timeout = Configuration.getTimeout();
    private String idempotencyKey;

    public static RequestOptions NewBuilder(){
        return new RequestOptions();
    }

    public String getAccessToken(){
        return this.accessToken;
    }

    public RequestOptions setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public int getMaxRetries(){
        return this.maxRetries;
    }

    public RequestOptions setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public int getTimeout(){
        return this.timeout;
    }

    public RequestOptions setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public RequestOptions setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        return this;
    }
}
