package com.webex.events.exceptions;

import com.webex.events.Response;

public class GatewayTimeoutError extends BaseException{
    public GatewayTimeoutError(Response response) {
        this.response = response;
    }
}
