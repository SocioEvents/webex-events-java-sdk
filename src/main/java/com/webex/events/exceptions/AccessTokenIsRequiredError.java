package com.webex.events.exceptions;

public class AccessTokenIsRequiredError extends Exception{
    public AccessTokenIsRequiredError(String msg) {
        super(msg);
    }
}
