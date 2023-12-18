package com.webex.events.exceptions;

import com.webex.events.Response;

public class BadRequestError extends BaseException{
    public BadRequestError(Response response) {
        super(response.body());
        this.response = response;
    }
}
