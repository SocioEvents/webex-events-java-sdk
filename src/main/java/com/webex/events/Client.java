package com.webex.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webex.events.error.ErrorResponse;
import com.webex.events.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Client {
    final static Logger logger = LoggerFactory.getLogger(Client.class);
    public static final int[] RETRIABLE_HTTP_STATUSES = {408, 409, 429, 502, 503, 504};

    public static String doIntrospectQuery() throws Exception {
        logger.debug("Doing introspection query...");
        Response response = query(
                Helpers.getIntrospectionQuery(),
                "IntrospectionQuery",
                new HashMap<>(),
                new HashMap<>()
        );
        return response.toString();
    }

    public static Response query(
            String query,
            String operationName,
            HashMap<String, Object> variables,
            HashMap<String, Object> headers
    ) throws Exception {

        Helpers.validateIdempotencyKey(headers.get("Idempotency-Key"));
        Helpers.validateAccessTokenExistence();

        HashMap<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("operationName", operationName);
        params.put("variables", variables);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(params);

        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + Configuration.getAccessToken());
        headers.put("X-Sdk-Name", "Java SDK");
        headers.put("X-Sdk-Version", Helpers.getSDKVersion());
        headers.put("X-Sdk-Lang-Version", System.getProperty("java.version"));
        headers.put("User-Agent", Helpers.getUserAgent());

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(Helpers.getUri(Configuration.getAccessToken()));

        for (Map.Entry<String, Object> header : headers.entrySet()) {
            requestBuilder.header(header.getKey(), (String) header.getValue());
        }

        HttpRequest request = requestBuilder
                .timeout(Configuration.getTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        long startTime = System.currentTimeMillis();
        HttpClient httpClient = HttpClient.newHttpClient();
        Response response = doOrRetryTheRequest(httpClient, request, operationName);
        long endTime = System.currentTimeMillis();

        response.setTimeSpentInMs((int) (endTime - startTime));
        response.setRequestBody(json);
        response.setRateLimiter(new RateLimiter(response));

        logger.info(
                "Executing {} query is finished with {} status code. It took {} ms and retried {} times.",
                operationName,
                response.status(),
                response.getTimeSpendInMs(),
                response.getRetryCount()
        );

        if (response.status() > 299) {
            logger.error("Executing {} query is failed. Received status code is {}", operationName, response.status());
            manageErrorState(response);
        }

        return response;
    }

    private static Response doOrRetryTheRequest(HttpClient httpClient, HttpRequest request, String operationName) {
        logger.info("Executing {} query for the first time to {}", operationName, Helpers.getUri(Configuration.getAccessToken()));

        Response response;
        try {
            response = new Response(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
            buildErrorResponse(response);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.error("{} http status received for {} query.", response.status(), operationName);

        Response finalResponse = response;
        int i = 0;
        double waitInterval = 250.0;
        double waitRate = 1.4;
        if (IntStream.of(RETRIABLE_HTTP_STATUSES).anyMatch(x -> x == finalResponse.status())) {
            while ((i < Configuration.getMaxRetries())) {
                i++;
                try {
                    response = new Response(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
                    buildErrorResponse(response);
                    ErrorResponse errorResponse = response.getErrorResponse();
                    if (errorResponse != null && errorResponse.dailyAvailableCostIsReached()) {
                        break;
                    }

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (response.status() < 300) {
                    break;
                } else {
                    waitInterval *= waitRate;
                    logger.info(
                            "The HTTP request is being restarted for {} query. Waiting for {} ms...",
                            operationName,
                            (int) waitInterval
                    );

                    try {
                        Thread.sleep((int) waitInterval);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        response.setRetryCount(i);
        return response;
    }

    private static void buildErrorResponse(Response response) {
        try {
            if (response.status() > 299) {
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse errResponse;
                errResponse = mapper.readValue(response.body(), ErrorResponse.class);
                response.setErrorResponse(errResponse);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private static void manageErrorState(Response response) throws Exception {
        ErrorResponse errorResponse = response.getErrorResponse();
        switch (response.status()) {
            case 400:
                if (errorResponse.isInvalidToken()) {
                    throw new InvalidAccessTokenError(response);
                } else if (errorResponse.isTokenIsExpired()) {
                    throw new AccessTokenIsExpiredError(response);
                } else {
                    throw new BadRequestError(response);
                }
            case 401:
                throw new AuthenticationRequiredError(response);
            case 403:
                throw new AuthorizationFailedError(response);
            case 404:
                throw new ResourceNotFoundError(response);
            case 408:
                throw new RequestTimeoutError(response);
            case 409:
                throw new ConflictError(response);
            case 413:
                throw new QueryComplexityIsTooHighError(response);
            case 422:
                throw new UnprocessableEntityError(response);
            case 429:
                if (errorResponse.dailyAvailableCostIsReached()) {
                    throw new DailyQuotaIsReachedError(response);
                }

                if (errorResponse.availableCostIsReached()) {
                    throw new SecondBasedQuotaIsReachedError(response);
                }
            case 500:
                throw new ServerError(response);
            case 502:
                throw new BadGatewayError(response);
            case 503:
                throw new ServiceUnavailableError(response);
            case 504:
                throw new GatewayTimeoutError(response);
            default:
                if (response.status() >= 400 && response.status() < 500) {
                    throw new ClientError(response);
                } else if (response.status() >= 500 && response.status() < 600) {
                    throw new ServerError(response);
                } else {
                    throw new UnknownStatusError(response);
                }
        }
    }
}
