package webexEvents.exceptions;

import webexEvents.Response;

public class GatewayTimeoutError extends BaseException{
    public GatewayTimeoutError(Response response) {
        this.response = response;
    }
}
