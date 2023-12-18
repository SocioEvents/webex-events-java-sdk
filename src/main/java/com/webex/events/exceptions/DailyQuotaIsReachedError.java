package com.webex.events.exceptions;

import com.webex.events.Response;

public class DailyQuotaIsReachedError extends BaseException{

    public DailyQuotaIsReachedError(Response response) {
        super(response.body());
        this.response = response;
    }
}
