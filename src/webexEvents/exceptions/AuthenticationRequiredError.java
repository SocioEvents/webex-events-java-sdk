package webexEvents.exceptions;

import webexEvents.Response;

public class AuthenticationRequiredError extends BaseException implements ExceptionInterface {

    public AuthenticationRequiredError(Response response) {
        this.response = response;
    }
}
