package webexEvents.exceptions;

import webexEvents.Response;

public class AccessTokenIsExpiredError extends BaseException{
    public AccessTokenIsExpiredError(Response response){
        this.response = response;
    }
}
