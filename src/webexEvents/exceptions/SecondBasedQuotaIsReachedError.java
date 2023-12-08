package webexEvents.exceptions;

import webexEvents.Response;

public class SecondBasedQuotaIsReachedError extends BaseException {
    public SecondBasedQuotaIsReachedError(Response response) {
        this.response = response;
    }
}
