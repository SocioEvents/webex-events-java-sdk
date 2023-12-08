package com.webex.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientTest {

    HttpResponse response;

    @Test
    void test() throws Exception {
        MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class);
        HttpClient httpClient = mock(HttpClient.class);
        httpClientStatic.when( ()-> HttpClient.newHttpClient() ).thenReturn(httpClient);
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        final String graphqlQuery = "query Currency($isoCode: String!){ currency(isoCode: $isoCode) { isoCode}}";
        final String operationName = "Currency";
        final HashMap<String, Object> variables = new HashMap<>();
        final HashMap<String, Object> headers = new HashMap<>();
        headers.put("Idempotency-Key", UUID.randomUUID().toString());

        final Configuration config = new Configuration()
                .setMaxRetries(3)
                .setAccessToken("sk_test_token_0190101010");

        variables.put("isoCode", "USD");

        final Response query = Client.query(graphqlQuery, operationName, variables, headers, config);
    }
}