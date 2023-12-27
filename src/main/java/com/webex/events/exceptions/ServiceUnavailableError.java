package com.webex.events.exceptions;

import com.webex.events.Response;

public class ServiceUnavailableError extends BaseNetworkException {
    public ServiceUnavailableError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
