package com.webex.events;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Configuration {
    private String accessToken;
    private int timeout = 30;

    private int maxRetries = 5;

    public String getAccessToken() {
        return accessToken;
    }

    public Configuration setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Configuration setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Configuration setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public URI getUri() {
        Pattern pattern = Pattern.compile("sk_live_");
        Matcher matcher = pattern.matcher(this.getAccessToken());
        String url ;
        if (matcher.find()) {
            url = "https://public.api.socio.events";
        } else {
            url = "https://public.sandbox-api.socio.events/graphql";
        }

        return URI.create(url);
    }
}
