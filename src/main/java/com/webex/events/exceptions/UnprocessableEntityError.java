package com.webex.events.exceptions;

import com.webex.events.Response;

public class UnprocessableEntityError extends BaseException {
    public UnprocessableEntityError(Response response) {
        super(response.body());
        this.response = response;
    }
}
