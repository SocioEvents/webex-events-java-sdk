package com.webex.events.exceptions;

import com.webex.events.Response;

public class AuthorizationFailedError extends BaseException{
    public AuthorizationFailedError(Response response){
        this.response = response;
    }
}