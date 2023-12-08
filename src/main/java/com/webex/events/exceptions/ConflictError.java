package com.webex.events.exceptions;

import com.webex.events.Response;

public class ConflictError extends BaseException {

    public ConflictError(Response response) {
        this.response = response;
    }
}
