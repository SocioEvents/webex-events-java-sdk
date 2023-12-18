package com.webex.events.exceptions;

import com.webex.events.Response;

public class BadGatewayError extends BaseException {
    public BadGatewayError(Response response) {
        super(response.body());
        this.response = response;
    }
}
