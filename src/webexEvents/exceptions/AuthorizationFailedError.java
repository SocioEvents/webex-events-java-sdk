package webexEvents.exceptions;

import webexEvents.Response;

public class AuthorizationFailedError extends BaseException{
    public AuthorizationFailedError(Response response){
        this.response = response;
    }
}
