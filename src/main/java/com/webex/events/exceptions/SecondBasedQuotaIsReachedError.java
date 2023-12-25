package com.webex.events.exceptions;

import com.webex.events.Response;

public class SecondBasedQuotaIsReachedError extends BaseException {
    public SecondBasedQuotaIsReachedError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
