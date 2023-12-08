package com.webex.events;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

public class Response {
    private HttpResponse response;
    private String requestBody;

    private int retryCount = 0;
    private int timeSpendInMs = 0;

    public Response(HttpResponse response) {
        this.response = response;
    }

    public HttpResponse getResponse() {
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

    public int setRetryCount(int retryCount) {
        return this.retryCount = retryCount;
    }

    public int getTimeSpendInMs() {
        return this.timeSpendInMs;
    }

    public int setTimeSpentInMs(int timeSpendInMs) {
        return this.timeSpendInMs = timeSpendInMs;
    }

}
