package com.webex.events.exceptions;

import com.webex.events.Response;

public class ServerError extends BaseException{
    public ServerError(Response response) {
        this.response = response;
    }
}