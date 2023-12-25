package com.webex.events.exceptions;

import com.webex.events.Response;

public class ServerError extends BaseException{
    public ServerError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
