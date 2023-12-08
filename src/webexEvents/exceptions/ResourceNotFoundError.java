package webexEvents.exceptions;

import webexEvents.Response;

public class ResourceNotFoundError extends BaseException {
    public ResourceNotFoundError(Response response) {
        this.response = response;
    }
}
