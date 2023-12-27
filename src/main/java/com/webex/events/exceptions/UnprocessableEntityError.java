package com.webex.events.exceptions;

import com.webex.events.Response;

public class UnprocessableEntityError extends BaseNetworkException {
    public UnprocessableEntityError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
