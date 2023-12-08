package com.webex.events;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webex.events.exceptions.*;

public class Client {
    public static final String VERSION = "0.1.0";

    public static Response query(
            String query,
            String operationName,
            HashMap<String, Object> variables,
            HashMap<String, Object> headers,
            Configuration config
    ) throws IOException, InterruptedException, Exception {

        if (headers.containsKey("Idempotency-Key")) {
            Pattern regex = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

            if (!regex.matcher(headers.get("Idempotency-Key").toString()).matches()) {
                throw new RuntimeException("Idempotency-Key must be UUID format");
            }

        }

        HashMap<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("operationName", operationName);
        params.put("variables", variables);

        HttpClient httpClient = HttpClient.newHttpClient();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(params);

        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + config.getAccessToken());
        headers.put("X-Sdk-Name", "Java SDK");
        headers.put("X-Sdk-Version", VERSION);
        headers.put("X-Sdk-Lang-Version", System.getProperty("java.version"));
        headers.put("User-Agent", userAgent());

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(config.getUri());

        for (Map.Entry<String, Object> header : headers.entrySet()) {
            requestBuilder.header(header.getKey(), (String) header.getValue());
        }

        HttpRequest request = requestBuilder
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        int retryCount = 0;
        double intervalRate = 1.4;
        int sleep = 250;
        Response response = null;
        long startTime = System.currentTimeMillis();
        while (retryCount < config.getMaxRetries()) {
            sleep = (int)(intervalRate * sleep);
            try {
                response = new Response(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
                response.setRetryCount(retryCount);
                long endTime = System.currentTimeMillis();
                response.setTimeSpentInMs((int)(endTime - startTime));
                if (response.status() == 200) {
                   break;
                } else {
                    manageErrorState(response);
                }
            } catch (SecondBasedQuotaIsReachedError | RequestTimeoutError | ConflictError | BadGatewayError |
                     ServiceUnavailableError | GatewayTimeoutError e) {

                retryCount++;
                if (retryCount == config.getMaxRetries()) {
                    long endTime = System.currentTimeMillis();
                    response.setTimeSpentInMs((int)(endTime - startTime));
                    throw e;
                } else {
                    Thread.sleep(sleep);
                }
            }
        }

        assert response != null;

        long endTime = System.currentTimeMillis();
        response.setTimeSpentInMs((int)(endTime - startTime));

        return response;
    }

    private static String userAgent(){
        String os = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");

        String hostName = "";
        try {
            InetAddress id = InetAddress.getLocalHost();
            hostName = id.getHostName();
        } catch (UnknownHostException ignored) {
        }

        return String.format("Webex Java SDK(v%s) - OS(%s) - hostname(%s) - Java Version(%s)", VERSION, os, hostName, javaVersion);
    }

    private static void manageErrorState(Response response) throws JsonProcessingException, Exception {
        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errorResponse = mapper.readValue(response.body(), ErrorResponse.class);

        switch (response.status()) {
            case 400:
                if (Objects.equals(errorResponse.extensions.get("code").textValue(), "INVALID_TOKEN")) {
                    throw new InvalidAccessTokenError(response);
                } else if (Objects.equals(errorResponse.extensions.get("code").textValue(), "TOKEN_IS_EXPIRED")) {
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
                JsonNode dailyAvailableCost = errorResponse.extensions.get("dailyAvailableCost");

                if (dailyAvailableCost != null && dailyAvailableCost.intValue() < 1) {
                    throw new DailyQuotaIsReachedError(response);
                }

                JsonNode availableCost = errorResponse.extensions.get("availableCost");

                if (availableCost != null && availableCost.intValue() < 1) {
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
                }else {
                    throw new RuntimeException();
                }
        }
    }
}
