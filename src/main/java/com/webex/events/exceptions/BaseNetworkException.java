package com.webex.events.exceptions;

import com.webex.events.Response;

abstract class BaseNetworkException extends Exception implements NetworkException {

    Response response;
    String message;

    @Override
    public Response response() {
        return response;
    }

    public BaseNetworkException(String message) {
        super(message);
        this.message = message;
    }
}
