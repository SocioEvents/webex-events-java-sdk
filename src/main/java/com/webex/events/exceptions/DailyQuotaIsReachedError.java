package com.webex.events.exceptions;

import com.webex.events.Response;

public class DailyQuotaIsReachedError extends BaseNetworkException {

    public DailyQuotaIsReachedError(Response response) {
        super(response.getBody());
        this.response = response;
    }
}
