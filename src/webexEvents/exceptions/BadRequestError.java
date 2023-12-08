package webexEvents.exceptions;

import webexEvents.Response;

public class BadRequestError extends BaseException{
    public BadRequestError(Response response) {
        this.response = response;
    }
}
