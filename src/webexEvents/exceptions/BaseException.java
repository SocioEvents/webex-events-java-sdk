package webexEvents.exceptions;

import webexEvents.Response;

abstract class BaseException extends Exception implements ExceptionInterface{

    Response response ;

    @Override
    public Response response() {
        return response;
    }
}
