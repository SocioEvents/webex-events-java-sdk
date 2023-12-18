package com.webex.events.exceptions;

import com.webex.events.Response;

public class InvalidAccessTokenError extends BaseException {
    public InvalidAccessTokenError(Response response) {
        super(response.body());
        this.response = response;
    }
}
