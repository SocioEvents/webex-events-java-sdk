package com.webex.events.exceptions;

import com.webex.events.Response;

public class AuthenticationRequiredError extends BaseNetworkException {

    public AuthenticationRequiredError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
