package com.webex.events.exceptions;

import com.webex.events.Response;

abstract class BaseException extends Exception implements ExceptionInterface{

    Response response ;

    @Override
    public Response response() {
        return response;
    }
}
