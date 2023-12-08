package webexEvents.exceptions;

import webexEvents.Response;

public class DailyQuotaIsReachedError extends BaseException{

    public DailyQuotaIsReachedError(Response response) {
        this.response = response;
    }
}
