package com.webex.events.exceptions;

import com.webex.events.Response;

public class UnknownStatusError extends BaseNetworkException {
    public UnknownStatusError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
