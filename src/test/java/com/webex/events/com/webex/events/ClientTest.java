package com.webex.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.webex.events.exceptions.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    public static WireMockServer wm;

    @BeforeAll
    static void beforeAll() {
        wm = new WireMockServer(options().port(8080));
        wm.start();
    }

    @AfterAll
    static void afterAll() {
        wm.stop();
    }

    @BeforeEach
    void beforeEach() {
        Configuration.setAccessToken("sk_wiremock_token_0190101010");
        Configuration.setMaxRetries(3);
    }

    Response doRequest() throws Exception {
        final String graphqlQuery = "query Currency($isoCode: String!){ currency(isoCode: $isoCode) { isoCode}}";
        final String operationName = "Currency";
        final HashMap<String, Object> variables = new HashMap<>();
        final HashMap<String, Object> headers = new HashMap<>();
        final RequestOptions options = RequestOptions.NewBuilder().setIdempotencyKey(UUID.randomUUID().toString());
        variables.put("isoCode", "USD");
        return Client.query(graphqlQuery, operationName, variables, options);
    }

    void mockRequest(int statusCode, String response) throws IOException {
        stubFor(post(urlEqualTo("/graphql"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(statusCode)
                        .withBody(response)));
    }
    @Test
    @DisplayName("It does introspection query")
    void doesIntrospectionQuery() throws Exception {
        mockRequest(200, "introspection");
        String response = Client.doIntrospectQuery();
        assertEquals("introspection", response);
    }

    @Test
    @DisplayName("Should raise AccessTokenIsRequired exception without given access token")
    void withoutAccessToken() throws Exception {
        mockRequest(200, "{}");
        final String graphqlQuery = "query CurrenciesList{ currenciesList { isoCode}}";
        final String operationName = "CurrenciesList";
        final HashMap<String, Object> variables = new HashMap<>();
        Configuration.setAccessToken("");
        Exception exception = assertThrows(AccessTokenIsRequiredError.class, () -> {
            Client.query(graphqlQuery, operationName, variables);
        });

        assertEquals("Access token is missing.", exception.getMessage());
    }


    @Test
    @DisplayName("Should not raise without given parameters")
    void withoutParams() throws Exception {
        mockRequest(200, "{}");
        final String graphqlQuery = "query CurrenciesList{ currenciesList { isoCode}}";
        final String operationName = "CurrenciesList";
        final HashMap<String, Object> variables = new HashMap<>();
        Response response = Client.query(graphqlQuery, operationName, variables);
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Should return 200 and does not throw exception.")
    void successCase() throws Exception {
        mockRequest(200, "{}");
        final Response query = doRequest();
        assertEquals(query.getStatus(), 200);
        assertEquals(query.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw InvalidAccessTokenError exception.")
    void statusCode400WithInvalidToken() throws Exception {
        HashMap<String, Object> jsonObject = new HashMap<>();
        HashMap<String, String> code = new HashMap<>();
        code.put("code", "INVALID_TOKEN");
        jsonObject.put("extensions", code);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jsonObject);

        mockRequest(400, json);;

        InvalidAccessTokenError exception = assertThrows(InvalidAccessTokenError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(400, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw AccessTokenIsExpiredError exception.")
    void statusCode400WithTokenIsExpired() throws Exception {
        HashMap<String, Object> jsonObject = new HashMap<>();
        HashMap<String, String> code = new HashMap<>();
        code.put("code", "TOKEN_IS_EXPIRED");
        jsonObject.put("extensions", code);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jsonObject);

        mockRequest(400, json);

        AccessTokenIsExpiredError exception = assertThrows(AccessTokenIsExpiredError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(400, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw AuthenticationRequiredError exception.")
    void statusCode401() throws Exception {
        mockRequest(401, "{}");

        AuthenticationRequiredError exception = assertThrows(AuthenticationRequiredError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(401, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw AuthorizationFailedError exception.")
    void statusCode403() throws Exception {
        mockRequest(403, "{}");

        AuthorizationFailedError exception = assertThrows(AuthorizationFailedError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(403, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundError exception.")
    void statusCode404() throws Exception {
        mockRequest(404, "{}");

        ResourceNotFoundError exception = assertThrows(ResourceNotFoundError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(404, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }


    @Test
    @DisplayName("Should throw RequestTimeoutError exception and does retry the request again.")
    void statusCode408() throws Exception {
        mockRequest(408, "{}");

        RequestTimeoutError exception = assertThrows(RequestTimeoutError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(408, response.getStatus());
        assertEquals(response.getRetryCount(), 3);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw ConflictError exception and does retry the request again.")
    void statusCode409() throws Exception {
        mockRequest(409, "{}");

        ConflictError exception = assertThrows(ConflictError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(409, response.getStatus());
        assertEquals(response.getRetryCount(), 3);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw QueryComplexityIsTooHighError exception")
    void statusCode413() throws Exception {
        mockRequest(413, "{}");

        QueryComplexityIsTooHighError exception = assertThrows(QueryComplexityIsTooHighError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(413, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }

    @Test
    @DisplayName("Should throw UnprocessableEntityError exception")
    void statusCode422() throws Exception {
        mockRequest(422, "{}");

        UnprocessableEntityError exception = assertThrows(UnprocessableEntityError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(422, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }


    @Test
    @DisplayName("Should throw DailyQuotaIsReachedError exception when daily available cost is zero")
    void statusCode429WithDailyQuotaReached() throws Exception {
        HashMap<String, Object> jsonObject = new HashMap<>();
        HashMap<String, Object> code = new HashMap<>();
        code.put("dailyAvailableCost", 0);
        jsonObject.put("extensions", code);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jsonObject);

        mockRequest(429, json);

        DailyQuotaIsReachedError exception = assertThrows(DailyQuotaIsReachedError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(429, response.getStatus());
        assertEquals(1, response.getRetryCount());
    }

    @Test
    @DisplayName("Should throw SecondBasedQuotaIsReachedError exception when secondly available cost is zero and does retry the request")
    void statusCode429WithSecondlyQuotaReached() throws Exception {
        HashMap<String, Object> jsonObject = new HashMap<>();
        HashMap<String, Object> code = new HashMap<>();
        code.put("availableCost", 0);
        code.put("dailyAvailableCost", 1);
        jsonObject.put("extensions", code);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(jsonObject);

        mockRequest(429, json);

        SecondBasedQuotaIsReachedError exception = assertThrows(SecondBasedQuotaIsReachedError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(429, response.getStatus());
        assertEquals(response.getRetryCount(), 3);
        assertTrue(response.getTimeSpendInMs() > 0);
    }


    @Test
    @DisplayName("Should throw ServerError exception")
    void statusCode500() throws Exception {
        mockRequest(500, "{}");

        ServerError exception = assertThrows(ServerError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(500, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }


    @Test
    @DisplayName("Should throw BadGatewayError exception and does retry the request.")
    void statusCode502() throws Exception {
        mockRequest(502, "{}");

        BadGatewayError exception = assertThrows(BadGatewayError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(502, response.getStatus());
        assertEquals(response.getRetryCount(), 3);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw ServiceUnavailableError exception and does retry the request.")
    void statusCode503() throws Exception {
        mockRequest(503, "{}");

        ServiceUnavailableError exception = assertThrows(ServiceUnavailableError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(503, response.getStatus());
        assertEquals(response.getRetryCount(), 3);
        assertTrue(response.getTimeSpendInMs() > 0);
    }


    @Test
    @DisplayName("Should throw GatewayTimeoutError exception and does retry the request.")
    void statusCode504() throws Exception {
        mockRequest(504, "{}");

        GatewayTimeoutError exception = assertThrows(GatewayTimeoutError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(504, response.getStatus());
        assertEquals(response.getRetryCount(), 3);
        assertTrue(response.getTimeSpendInMs() > 0);
    }

    @Test
    @DisplayName("Should throw ClientError exception")
    void statusCode420() throws Exception {
        mockRequest(420, "{}");

        ClientError exception = assertThrows(ClientError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(420, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }


    @Test
    @DisplayName("Should throw ServerError exception")
    void statusCode510() throws Exception {
        mockRequest(510, "{}");

        ServerError exception = assertThrows(ServerError.class, this::doRequest);

        Response response = exception.response();
        assertEquals(510, response.getStatus());
        assertEquals(response.getRetryCount(), 0);
    }
}