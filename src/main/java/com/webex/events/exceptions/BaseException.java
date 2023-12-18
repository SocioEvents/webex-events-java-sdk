package com.webex.events.exceptions;

import com.webex.events.Response;

abstract class BaseException extends Exception implements ExceptionImpl {

    Response response;
    String message;

    @Override
    public Response response() {
        return response;
    }

    public BaseException(String message) {
        super(message);
        this.message = message;
    }
}
