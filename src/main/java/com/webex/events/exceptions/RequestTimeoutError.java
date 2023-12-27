package com.webex.events.exceptions;

import com.webex.events.Response;

public class RequestTimeoutError extends BaseNetworkException {

    public RequestTimeoutError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
