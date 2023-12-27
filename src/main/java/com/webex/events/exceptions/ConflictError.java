package com.webex.events.exceptions;

import com.webex.events.Response;

public class ConflictError extends BaseNetworkException {

    public ConflictError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
