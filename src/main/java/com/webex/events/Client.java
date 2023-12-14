package com.webex.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webex.events.exceptions.*;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Client {
    final static Logger logger = LoggerFactory.getLogger(Client.class);
    public static final int[] RETRIABLE_HTTP_STATUSES = {408, 409, 429, 502, 503, 504};

    public static String doIntrospectQuery(Configuration configuration) throws Exception {
        logger.debug("Doing introspection query...");
        Response response = query(
                Helpers.getIntrospectionQuery(),
                "IntrospectionQuery",
                new HashMap<>(),
                new HashMap<>(),
                configuration
        );
        return response.toString();
    }

    public static Response query(
            String query,
            String operationName,
            HashMap<String, Object> variables,
            HashMap<String, Object> headers,
            Configuration config
    ) throws Exception {

        Helpers.validateIdempotencyKey(headers.get("Idempotency-Key"));
        Helpers.validateAccessTokenExistence(config.getAccessToken());

        HashMap<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("operationName", operationName);
        params.put("variables", variables);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(params);

        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + config.getAccessToken());
        headers.put("X-Sdk-Name", "Java SDK");
        headers.put("X-Sdk-Version", PomReader.sdkVersion());
        headers.put("X-Sdk-Lang-Version", System.getProperty("java.version"));
        headers.put("User-Agent", Helpers.getUserAgent());

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(Helpers.getUri(config.getAccessToken()));

        for (Map.Entry<String, Object> header : headers.entrySet()) {
            requestBuilder.header(header.getKey(), (String) header.getValue());
        }

        HttpRequest request = requestBuilder
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        long startTime = System.currentTimeMillis();
        HttpClient httpClient = HttpClient.newHttpClient();
        Response response = doOrRetryTheRequest(config, httpClient, request, operationName);
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

    private static Response doOrRetryTheRequest(Configuration config, HttpClient httpClient, HttpRequest request, String operationName) {
        logger.info("Executing {} query for the first time to {}",operationName,Helpers.getUri(config.getAccessToken()));
        IntervalFunction intervalFn = IntervalFunction.ofExponentialBackoff(250, 1.4);
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(config.getMaxRetries())
                .intervalFunction(intervalFn)
                .failAfterMaxAttempts(false)
                .retryOnResult(response -> {
                    logger.error("{} http status received for {} query.", ((Response)response).status(), operationName);
                    ObjectMapper mapper = new ObjectMapper();
                    ErrorResponse errorResponse = null;
                    try {
                        if (((Response)response).status() >= 400) {
                            errorResponse = mapper.readValue(((Response)response).body(), ErrorResponse.class);
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    ((Response)response).setErrorResponse(errorResponse);
                    if (errorResponse != null && errorResponse.dailyAvailableCostIsReached()) {
                        return false;
                    }else {
                        return IntStream.of(RETRIABLE_HTTP_STATUSES).anyMatch(x -> x == ((Response) response).status());
                    }
                })
                .build();

        // Create a RetryRegistry with a custom global configuration
        RetryRegistry registry = RetryRegistry.of(retryConfig);

        // Get or create a Retry from the registry -
        // Retry will be backed by the default config
        Retry retryWithDefaultConfig = registry.retry("custom");

        AtomicInteger retryCount = new AtomicInteger();
        retryWithDefaultConfig.getEventPublisher().onRetry(retryEvent -> {
            retryCount.incrementAndGet();
            logger.info(
                    "The HTTP request is being restarted for {} query. Waiting for {} ms...",
                    operationName,
                    retryEvent.getWaitInterval().toMillis()
            );
        });
        CheckedSupplier<Response> retryableSupplier = Retry
                .decorateCheckedSupplier(retryWithDefaultConfig, () -> {
                    try {
                        return new Response(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

        try {
            Response response = retryableSupplier.get();
            response.setRetryCount(retryCount.intValue());
            return response;
        } catch (Throwable e) {
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
