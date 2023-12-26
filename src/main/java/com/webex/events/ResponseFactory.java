package com.webex.events;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;

public class ResponseFactory {

    public static Response create(CloseableHttpResponse httpResponse, HttpPost httpPost) throws IOException {
        Response response = new Response();
        response.setStatus(httpResponse.getStatusLine().getStatusCode());
        response.setBody(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));

        response.setUri(httpPost.getURI().toString());

        HashMap<String, String> responseHeaders = new HashMap<>();
        for (Header header : httpResponse.getAllHeaders()) {
            responseHeaders.put(header.getName().toLowerCase(), header.getValue());
        }
        response.setResponseHeaders(responseHeaders);

        HashMap<String, String> requestHeaders = new HashMap<>();
        for (Header header : httpPost.getAllHeaders()) {
            requestHeaders.put(header.getName().toLowerCase(), header.getValue());
        }
        response.setRequestHeaders(requestHeaders);


        return response;
    }
}
