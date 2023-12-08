package com.webex.events.exceptions;

import com.webex.events.Response;

public class BadGatewayError extends BaseException {
    public BadGatewayError(Response response) {
        this.response = response;
    }
}
