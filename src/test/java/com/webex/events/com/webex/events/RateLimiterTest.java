package com.webex.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.*;
import java.net.http.HttpHeaders;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimiterTest {
    Response response;
    Map<String, List<String>> headersMap = new HashMap<>();
    Arrays Arrays = null;
    RateLimiter rateLimiter;

    @BeforeEach
    <HttpClientWrapper>
    void setUp() throws IOException, InterruptedException {
        response = Mockito.mock(Response.class);
    }

    @Test
    @DisplayName("Should return daily limits")
    void testDailyCallLimitHeader() {
        headersMap.put("x-daily-call-limit", Arrays.asList("2/10"));
        when(response.headers()).thenReturn(HttpHeaders.of(headersMap, (k, v) -> true));
        rateLimiter = new RateLimiter(response);

        assertEquals(rateLimiter.getUsedDailyBasedCost(), 2);
        assertEquals(rateLimiter.getDailyBasedCostThreshold(), 10);
    }


    @Test
    @DisplayName("Should return secondly limits")
    void testSecondlyCallLimitHeader() {
        headersMap.put("x-secondly-call-limit", Arrays.asList("35/50"));
        when(response.headers()).thenReturn(HttpHeaders.of(headersMap, (k, v) -> true));
        rateLimiter = new RateLimiter(response);

        assertEquals(rateLimiter.getUsedSecondBasedCost(), 35);
        assertEquals(rateLimiter.getSecondBasedCostThreshold(), 50);
    }

    @Test
    @DisplayName("Should return daily retry after")
    void testDailyRetryAfter() {
        headersMap.put("x-daily-retry-after", Arrays.asList("3"));
        when(response.headers()).thenReturn(HttpHeaders.of(headersMap, (k, v) -> true));
        rateLimiter = new RateLimiter(response);

        assertEquals(rateLimiter.getDailyRetryAfterInSecond(), 3);
    }

    @Test
    @DisplayName("Should return daily retry after")
    void testSecondlyRetryAfter() {
        headersMap.put("x-secondly-retry-after", Arrays.asList("30"));
        when(response.headers()).thenReturn(HttpHeaders.of(headersMap, (k, v) -> true));
        rateLimiter = new RateLimiter(response);

        assertEquals(rateLimiter.getSecondlyRetryAfterInMs(), 30);
    }
}