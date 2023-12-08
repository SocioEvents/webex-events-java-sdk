package com.webex.events.exceptions;

import com.webex.events.Response;

public class RequestTimeoutError extends BaseException {

    public RequestTimeoutError(Response response) {
        this.response = response;
    }
}