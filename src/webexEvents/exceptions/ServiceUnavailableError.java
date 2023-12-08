package webexEvents.exceptions;

import webexEvents.Response;

public class ServiceUnavailableError extends BaseException{
    public ServiceUnavailableError(Response response) {
        this.response = response;
    }
}
