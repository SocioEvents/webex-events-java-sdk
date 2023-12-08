package webexEvents.exceptions;

import webexEvents.Response;

public class ServerError extends BaseException{
    public ServerError(Response response) {
        this.response = response;
    }
}
