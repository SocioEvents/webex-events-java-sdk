package com.webex.events.exceptions;

import com.webex.events.Response;

public class GatewayTimeoutError extends BaseNetworkException {
    public GatewayTimeoutError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
