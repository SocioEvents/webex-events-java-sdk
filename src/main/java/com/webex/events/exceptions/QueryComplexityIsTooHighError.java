package com.webex.events.exceptions;

import com.webex.events.Response;

public class QueryComplexityIsTooHighError extends BaseException{
    public QueryComplexityIsTooHighError(Response response) {
        super(response.body());
        this.response = response;
    }
}
