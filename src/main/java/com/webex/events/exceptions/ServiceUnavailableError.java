package com.webex.events.exceptions;

import com.webex.events.Response;

public class ServiceUnavailableError extends BaseException{
    public ServiceUnavailableError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
