package com.webex.events.exceptions;

import com.webex.events.Response;

public class ResourceNotFoundError extends BaseException {
    public ResourceNotFoundError(Response response) {
        this.response = response;
    }
}