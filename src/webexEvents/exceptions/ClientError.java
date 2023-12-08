package webexEvents.exceptions;

import webexEvents.Response;

public class ClientError extends BaseException{
    public ClientError(Response response) {
        this.response = response;
    }
}
