package webexEvents.exceptions;

import webexEvents.Response;

public class QueryComplexityIsTooHighError extends BaseException{
    public QueryComplexityIsTooHighError(Response response) {
        this.response = response;
    }
}
