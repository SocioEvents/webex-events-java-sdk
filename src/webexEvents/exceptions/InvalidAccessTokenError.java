package webexEvents.exceptions;

import webexEvents.Response;

public class InvalidAccessTokenError extends BaseException {
    public InvalidAccessTokenError(Response response) {
        this.response = response;
    }
}
