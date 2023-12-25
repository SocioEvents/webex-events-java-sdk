package com.webex.events;

import com.webex.events.error.ErrorResponse;

import java.util.HashMap;

public class Response {
    private String requestBody;
    private int retryCount = 0;
    private int timeSpendInMs = 0;
    private ErrorResponse errorResponse = null;
    private RateLimiter rateLimiter;
    private int status;
    private String body;
    private HashMap<String, String> responseHeaders;
    private HashMap<String, String> requestHeaders;
    private String uri;

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public RateLimiter setRateLimiter(RateLimiter rateLimiter) {
        return this.rateLimiter = rateLimiter;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public HashMap<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }

    public void setResponseHeaders(HashMap<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public HashMap<String, String> getRequestHeaders() {
        return this.requestHeaders;
    }

    public void setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }


    public String getBody() {
        if (this.body == null) {
            return "{}";
        }
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String requestBody() {
        return this.requestBody;
    }

    public String setRequestBody(String requestBody) {
        return this.requestBody = requestBody;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getTimeSpendInMs() {
        return this.timeSpendInMs;
    }

    public void setTimeSpentInMs(int timeSpendInMs) {
        this.timeSpendInMs = timeSpendInMs;
    }

    public String toString() {
        return body;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
