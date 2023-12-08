package com.webex.events.exceptions;

import com.webex.events.Response;

public class AccessTokenIsExpiredError extends BaseException{
    public AccessTokenIsExpiredError(Response response){
        this.response = response;
    }
}
