package com.webex.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webex.events.error.ErrorResponse;
import com.webex.events.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Client {
    final static Logger logger = LoggerFactory.getLogger(Client.class);
    public static final int[] RETRIABLE_HTTP_STATUSES = {408, 409, 429, 502, 503, 504};

    public static String doIntrospectQuery() throws Exception {
        logger.debug("Doing introspection query...");
        Response response = query(Helpers.getIntrospectionQuery(), "IntrospectionQuery");
        return response.toString();
    }

    public static Response query(String query, String operationName) throws Exception {
        return query(query, operationName, new HashMap<String, Object>(), RequestOptions.NewBuilder());
    }

    public static Response query(String query, String operationName, HashMap<String, Object> variables) throws Exception {
        return query(query, operationName, variables, RequestOptions.NewBuilder());
    }

    public static Response query(String query, String operationName, RequestOptions options) throws Exception {
        return query(query, operationName, new HashMap<String, Object>(), options);
    }

    public static Response query(
            String query,
            String operationName,
            HashMap<String, Object> variables,
            RequestOptions options
    ) throws Exception {

        Helpers.validateAccessTokenExistence();

        HashMap<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("operationName", operationName);
        params.put("variables", variables);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(params);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + options.getAccessToken());
        headers.put("X-Sdk-Name", "Java SDK");
        headers.put("X-Sdk-Version", Helpers.getSDKVersion());
        headers.put("X-Sdk-Lang-Version", System.getProperty("java.version"));
        headers.put("User-Agent", Helpers.getUserAgent());

        if (options.getIdempotencyKey() != null) {
            headers.put("Idempotency-Key", options.getIdempotencyKey());
        }

        HttpPost httpPost = new HttpPost(Helpers.getUri(options.getAccessToken()));

        for (Map.Entry<String, String> header : headers.entrySet()) {
            httpPost.setHeader(header.getKey(), header.getValue());
        }

        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);

        long startTime = System.currentTimeMillis();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        Response response = doOrRetryTheRequest(httpClient, httpPost, operationName, options);
        long endTime = System.currentTimeMillis();

        response.setTimeSpentInMs((int) (endTime - startTime));
        response.setRequestBody(json);
        response.setRateLimiter(new RateLimiter(response));

        logger.info(
                "Executing {} query is finished with {} status code. It took {} ms and retried {} times.",
                operationName,
                response.getStatus(),
                response.getTimeSpendInMs(),
                response.getRetryCount()
        );

        if (response.getStatus() > 299) {
            logger.error("Executing {} query is failed. Received status code is {}", operationName, response.getStatus());
            manageErrorState(response);
        }

        return response;
    }

    private static Response doOrRetryTheRequest(CloseableHttpClient httpClient, HttpPost httpPost, String operationName, RequestOptions options) {
        logger.info("Executing {} query for the first time to {}", operationName, Helpers.getUri(options.getAccessToken()));

        Response response;
        try {
            response = ResponseFactory.create(httpClient.execute(httpPost), httpPost);
            buildErrorResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.error("{} http status received for {} query.", response.getStatus(), operationName);

        Response finalResponse = response;
        int i = 0;
        double waitInterval = 250.0;
        double waitRate = 1.4;
        if (IntStream.of(RETRIABLE_HTTP_STATUSES).anyMatch(x -> x == finalResponse.getStatus())) {
            while ((i < options.getMaxRetries())) {
                i++;
                waitInterval *= waitRate;
                logger.info(
                        "The HTTP httpPost is being restarted for {} query. Waiting for {} ms...",
                        operationName,
                        (int) waitInterval
                );

                try {
                    Thread.sleep((int) waitInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    response = ResponseFactory.create(httpClient.execute(httpPost), httpPost);
                    buildErrorResponse(response);
                    ErrorResponse errorResponse = response.getErrorResponse();
                    if (errorResponse != null && errorResponse.dailyAvailableCostIsReached()) {
                        break;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (response.getStatus() < 300) {
                    break;
                }
            }
        }
        response.setRetryCount(i);
        return response;
    }

    private static void buildErrorResponse(Response response) {
        try {
            if (response.getStatus() > 299) {
                ObjectMapper mapper = new ObjectMapper();
                ErrorResponse errResponse;
                errResponse = mapper.readValue(response.getBody(), ErrorResponse.class);
                response.setErrorResponse(errResponse);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private static void manageErrorState(Response response) throws Exception {
        ErrorResponse errorResponse = response.getErrorResponse();
        switch (response.getStatus()) {
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
                if (response.getStatus() >= 400 && response.getStatus() < 500) {
                    throw new ClientError(response);
                } else if (response.getStatus() >= 500 && response.getStatus() < 600) {
                    throw new ServerError(response);
                } else {
                    throw new UnknownStatusError(response);
                }
        }
    }
}
