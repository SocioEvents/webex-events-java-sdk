package com.webex.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webex.events.exceptions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientTest {

    private HttpResponse httpResponse;
    private HttpClient  httpClient;
    private MockedStatic<HttpClient> httpClientStatic;

    @BeforeEach
    void beforeEach() {
        this.httpClientStatic = mockStatic(HttpClient.class);
        this.httpClient = mock(HttpClient.class);
        httpClientStatic.when( ()-> HttpClient.newHttpClient() ).thenReturn(httpClient);
        this.httpResponse = mock(HttpResponse.class);
    }

    @AfterEach
    void afterEach() {
        httpClientStatic.close();
    }

    Response doRequest() throws Exception {
        final String graphqlQuery = "query Currency($isoCode: String!){ currency(isoCode: $isoCode) { isoCode}}";
        final String operationName = "Currency";
        final HashMap<String, Object> variables = new HashMap<>();
        final HashMap<String, Object> headers = new HashMap<>();
        headers.put("Idempotency-Key", UUID.randomUUID().toString());

        final Configuration config = new Configuration()
                .setMaxRetries(3)
                .setAccessToken("sk_test_token_0190101010");
        variables.put("isoCode", "USD");
        return Client.query(graphqlQuery, operationName, variables, headers, config);
    }


    @Test
    @DisplayName("Should raise AccessTokenIsRequired exception without given access token")
    void withoutAccessToken() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);
        final String graphqlQuery = "query CurrenciesList{ currenciesList { isoCode}}";
        final String operationName = "CurrenciesList";
        final HashMap<String, Object> variables = new HashMap<>();
        final HashMap<String, Object> headers = new HashMap<>();
        final Configuration config = new Configuration();
        Exception exception = assertThrows(AccessTokenIsRequiredError.class, () -> {
            Client.query(graphqlQuery, operationName, variables, headers, config);
        });

        assertTrue(Objects.equals(exception.getMessage(), "Access token is missing."));
    }

    @Test
    @DisplayName("Should not raise without given parameters")
    void withoutParams() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);
        final String graphqlQuery = "query CurrenciesList{ currenciesList { isoCode}}";
        final String operationName = "CurrenciesList";
        final HashMap<String, Object> variables = new HashMap<>();
        final HashMap<String, Object> headers = new HashMap<>();
        final Configuration config = new Configuration()
                .setMaxRetries(3)
                .setAccessToken("sk_test_token_0190101010");
        Response response = Client.query(graphqlQuery, operationName, variables, headers, config);
        assertTrue(response.status() == 200);
    }

    @Test
    @DisplayName("Should raise InvalidUUIDFormatError exception if given Idempotency Key is not UUID format.")
    void invalidUUIDTest() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        final String graphqlQuery = "query Currency($isoCode: String!){ currency(isoCode: $isoCode) { isoCode}}";
        final String operationName = "Currency";
        final HashMap<String, Object> variables = new HashMap<>();
        final HashMap<String, Object> headers = new HashMap<>();
        headers.put("Idempotency-Key", UUID.randomUUID() + "invalid");

        final Configuration config = new Configuration()
                .setMaxRetries(3)
                .setAccessToken("sk_test_token_0190101010");
        variables.put("isoCode", "USD");

        Exception exception = assertThrows(InvalidUUIDFormatError.class, () -> {
            Client.query(graphqlQuery, operationName, variables, headers, config);
        });

        assertTrue(Objects.equals(exception.getMessage(), "Idempotency-Key must be UUID format"));
    }

    @Test
    @DisplayName("Should return 200 and does not throw exception.")
    void successCase() throws Exception {

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        final Response query = doRequest();
        assertEquals(query.status(), 200);
        assertEquals(query.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw InvalidAccessTokenError exception.")
    void statusCode400WithInvalidToken() throws Exception {
        when(httpResponse.statusCode()).thenReturn(400);
        HashMap<String, Object> jsonObject = new HashMap<>();
        HashMap<String, String> code = new HashMap<>();
        code.put("code", "INVALID_TOKEN");
        jsonObject.put("extensions", code);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jsonObject);

        when(httpResponse.body()).thenReturn(json);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(InvalidAccessTokenError.class, this::doRequest);

        Response response = ((InvalidAccessTokenError) exception).response();
        assertTrue(response.status() == 400);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw AccessTokenIsExpiredError exception.")
    void statusCode400WithTokenIsExpired() throws Exception {
        when(httpResponse.statusCode()).thenReturn(400);
        HashMap<String, Object> jsonObject = new HashMap<>();
        HashMap<String, String> code = new HashMap<>();
        code.put("code", "TOKEN_IS_EXPIRED");
        jsonObject.put("extensions", code);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jsonObject);

        when(httpResponse.body()).thenReturn(json);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(AccessTokenIsExpiredError.class, this::doRequest);

        Response response = ((AccessTokenIsExpiredError) exception).response();
        assertTrue(response.status() == 400);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw AuthenticationRequiredError exception.")
    void statusCode401() throws Exception {
        when(httpResponse.statusCode()).thenReturn(401);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(AuthenticationRequiredError.class, this::doRequest);

        Response response = ((AuthenticationRequiredError) exception).response();
        assertTrue(response.status() == 401);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw AuthorizationFailedError exception.")
    void statusCode403() throws Exception {
        when(httpResponse.statusCode()).thenReturn(403);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(AuthorizationFailedError.class, this::doRequest);

        Response response = ((AuthorizationFailedError) exception).response();
        assertTrue(response.status() == 403);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundError exception.")
    void statusCode404() throws Exception {
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(ResourceNotFoundError.class, this::doRequest);

        Response response = ((ResourceNotFoundError) exception).response();
        assertTrue(response.status() == 404);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw RequestTimeoutError exception and does retry the request again.")
    void statusCode408() throws Exception {
        when(httpResponse.statusCode()).thenReturn(408);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(RequestTimeoutError.class, this::doRequest);

        Response response = ((RequestTimeoutError) exception).response();
        assertTrue(response.status() == 408);
        assertEquals(response.getRetryCount(), 2);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw ConflictError exception and does retry the request again.")
    void statusCode409() throws Exception {
        when(httpResponse.statusCode()).thenReturn(409);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(ConflictError.class, this::doRequest);

        Response response = ((ConflictError) exception).response();
        assertTrue(response.status() == 409);
        assertEquals(response.getRetryCount(), 2);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw QueryComplexityIsTooHighError exception")
    void statusCode413() throws Exception {
        when(httpResponse.statusCode()).thenReturn(413);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(QueryComplexityIsTooHighError.class, this::doRequest);

        Response response = ((QueryComplexityIsTooHighError) exception).response();
        assertTrue(response.status() == 413);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityError exception")
    void statusCode422() throws Exception {
        when(httpResponse.statusCode()).thenReturn(422);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(UnprocessableEntityError.class, this::doRequest);

        Response response = ((UnprocessableEntityError) exception).response();
        assertTrue(response.status() == 422);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw DailyQuotaIsReachedError exception when daily available cost is zero")
    void statusCode429WithDailyQuotaReached() throws Exception {
        when(httpResponse.statusCode()).thenReturn(429);
        HashMap<String, Object> jsonObject = new HashMap<>();
        HashMap<String, Object> code = new HashMap<>();
        code.put("dailyAvailableCost", 0);
        jsonObject.put("extensions", code);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jsonObject);

        when(httpResponse.body()).thenReturn(json);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);


        Exception exception = assertThrows(DailyQuotaIsReachedError.class, this::doRequest);

        Response response = ((DailyQuotaIsReachedError) exception).response();
        assertTrue(response.status() == 429);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw SecondBasedQuotaIsReachedError exception when secondly available cost is zero and does retry the request")
    void statusCode429WithSecondlyQuotaReached() throws Exception {
        when(httpResponse.statusCode()).thenReturn(429);
        HashMap<String, Object> jsonObject = new HashMap<>();
        HashMap<String, Object> code = new HashMap<>();
        code.put("availableCost", 0);
        jsonObject.put("extensions", code);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jsonObject);

        when(httpResponse.body()).thenReturn(json);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);


        Exception exception = assertThrows(SecondBasedQuotaIsReachedError.class, this::doRequest);

        Response response = ((SecondBasedQuotaIsReachedError) exception).response();
        assertTrue(response.status() == 429);
        assertEquals(response.getRetryCount(), 2);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw ServerError exception")
    void statusCode500() throws Exception {
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(ServerError.class, this::doRequest);

        Response response = ((ServerError) exception).response();
        assertTrue(response.status() == 500);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw BadGatewayError exception and does retry the request.")
    void statusCode502() throws Exception {
        when(httpResponse.statusCode()).thenReturn(502);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(BadGatewayError.class, this::doRequest);

        Response response = ((BadGatewayError) exception).response();
        assertTrue(response.status() == 502);
        assertEquals(response.getRetryCount(), 2);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw ServiceUnavailableError exception and does retry the request.")
    void statusCode503() throws Exception {
        when(httpResponse.statusCode()).thenReturn(503);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(ServiceUnavailableError.class, this::doRequest);

        Response response = ((ServiceUnavailableError) exception).response();
        assertTrue(response.status() == 503);
        assertEquals(response.getRetryCount(), 2);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw GatewayTimeoutError exception and does retry the request.")
    void statusCode504() throws Exception {
        when(httpResponse.statusCode()).thenReturn(504);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(GatewayTimeoutError.class, this::doRequest);

        Response response = ((GatewayTimeoutError) exception).response();
        assertTrue(response.status() == 504);
        assertEquals(response.getRetryCount(), 2);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw ClientError exception")
    void statusCode420() throws Exception {
        when(httpResponse.statusCode()).thenReturn(420);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(ClientError.class, this::doRequest);

        Response response = ((ClientError) exception).response();
        assertTrue(response.status() == 420);
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw ServerError exception")
    void statusCode510() throws Exception {
        when(httpResponse.statusCode()).thenReturn(510);
        when(httpClient.send(any(),any())).thenReturn(httpResponse);

        Exception exception = assertThrows(ServerError.class, this::doRequest);

        Response response = ((ServerError) exception).response();
        assertTrue(response.status() == 510);
        assertEquals(response.getRetryCount(), 0);
    }
}