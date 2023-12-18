package com.webex.events.exceptions;

import com.webex.events.Response;

public class ClientError extends BaseException{
    public ClientError(Response response)
    {
        super(response.body());
        this.response = response;
    }
}
