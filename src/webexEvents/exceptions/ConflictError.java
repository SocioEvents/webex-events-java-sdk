package webexEvents.exceptions;

import webexEvents.Response;

public class ConflictError extends BaseException {

    public ConflictError(Response response) {
        this.response = response;
    }
}
