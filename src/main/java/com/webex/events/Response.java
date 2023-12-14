package com.webex.events;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

public class Response {
    private HttpResponse<?> response;
    private String requestBody;

    private int retryCount = 0;
    private int timeSpendInMs = 0;
    private ErrorResponse errorResponse = null;
    private RateLimiter rateLimiter;

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public RateLimiter setRateLimiter(RateLimiter rateLimiter) {
        return this.rateLimiter = rateLimiter;
    }

    public Response(HttpResponse<?> response) {
        this.response = response;
    }


    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public HttpResponse<?> getResponse() {
        return response;
    }

    public int status() {
        return this.response.statusCode();
    }

    public HttpHeaders headers() {
        return this.response.headers();
    }

    public String body() {
        if (this.response.body() == null) {
            return "{}";
        }
        return this.response.body().toString();
    }

    public HttpHeaders requestHeaders() {
        return this.response.request().headers();
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
        return response.body().toString();
    }

    public String getUri() {
        return response.request().uri().toString();
    }
}
