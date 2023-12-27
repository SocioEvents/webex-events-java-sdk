package com.webex.events.exceptions;

import com.webex.events.Response;

public class ClientError extends BaseNetworkException {
    public ClientError(Response response)
    {
        super(response.getBody());
        this.response = response;
    }
}
