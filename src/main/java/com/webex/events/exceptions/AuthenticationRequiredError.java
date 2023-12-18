package com.webex.events.exceptions;

import com.webex.events.Response;

public class AuthenticationRequiredError extends BaseException {

    public AuthenticationRequiredError(Response response) {
        super(response.body());
        this.response = response;
    }
}
