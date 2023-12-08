package com.webex.events.exceptions;

import com.webex.events.Response;

public class AuthenticationRequiredError extends BaseException implements ExceptionInterface {

    public AuthenticationRequiredError(Response response) {
        this.response = response;
    }
}
