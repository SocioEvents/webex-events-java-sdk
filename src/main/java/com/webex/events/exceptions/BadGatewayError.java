package com.webex.events.exceptions;

import com.webex.events.Response;

public class BadGatewayError extends BaseNetworkException {
    public BadGatewayError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
