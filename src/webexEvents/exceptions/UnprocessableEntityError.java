package webexEvents.exceptions;

import webexEvents.Response;

public class UnprocessableEntityError extends BaseException {
    public UnprocessableEntityError(Response response) {
        this.response = response;
    }
}
