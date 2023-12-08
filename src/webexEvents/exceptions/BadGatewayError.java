package webexEvents.exceptions;

import webexEvents.Response;

public class BadGatewayError extends BaseException {
    public BadGatewayError(Response response) {
        this.response = response;
    }
}
