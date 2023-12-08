package webexEvents.exceptions;

import webexEvents.Response;

public class RequestTimeoutError extends BaseException {

    public RequestTimeoutError(Response response) {
        this.response = response;
    }
}
