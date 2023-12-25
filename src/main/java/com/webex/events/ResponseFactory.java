package com.webex.events;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseFactory {

    public static Response create(HttpResponse httpResponse) {
        Response response = new Response();
        response.setStatus(httpResponse.statusCode());
        response.setBody(httpResponse.body().toString());
        if (httpResponse.uri() != null) {
            response.setUri(httpResponse.uri().toString());
        }


        if (httpResponse.headers() != null) {
            HashMap<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : httpResponse.headers().map().entrySet()) {
                String responseHeaderValue = httpResponse.headers().allValues(header.getKey()).get(0);
                responseHeaders.put(header.getKey().toLowerCase(), responseHeaderValue);
            }
            response.setResponseHeaders(responseHeaders);
        }

        if (httpResponse.request() != null) {
            HashMap<String, String> requestHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : httpResponse.request().headers().map().entrySet()) {
                String requestHeaderValue = httpResponse.request().headers().allValues(header.getKey()).get(0);
                requestHeaders.put(header.getKey().toLowerCase(), requestHeaderValue);
            }
            response.setRequestHeaders(requestHeaders);
        }

        return response;
    }
}
