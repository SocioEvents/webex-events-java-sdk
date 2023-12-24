package com.webex.events;

import com.webex.events.exceptions.AccessTokenIsRequiredError;
import com.webex.events.exceptions.InvalidUUIDFormatError;

import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

public class Helpers {
    public static final String ACCESS_TOKEN_IS_MISSING = "Access token is missing.";
    private static String sdkVersion = null;
    private static String userAgent = null;
    private static String introspectionQuery = null;
    private static HashMap<String, URI> uris = new HashMap<>();

    static String getSDKVersion() {
        try {
            if (sdkVersion == null) {
                Properties properties = new Properties();
                properties.load(Helpers.class.getResourceAsStream("/version.properties"));
                return sdkVersion = properties.getProperty("version");
            }
        } catch (IOException ignored) {
            sdkVersion = "";
        }
        return sdkVersion;
    }

    static void validateAccessTokenExistence() throws AccessTokenIsRequiredError {
        if (Configuration.getAccessToken() == null || Configuration.getAccessToken().isEmpty()) {
            throw new AccessTokenIsRequiredError(ACCESS_TOKEN_IS_MISSING);
        }
    }

    public static String getUserAgent() {
        if (userAgent != null) {
            return userAgent;
        }

        String os = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");

        String hostName = "";
        try {
            InetAddress id = InetAddress.getLocalHost();
            hostName = id.getHostName();
        } catch (UnknownHostException ignored) {
        }

        userAgent = String.format("Webex Java SDK(v%s) - OS(%s) - hostname(%s) - Java Version(%s)", getSDKVersion(), os, hostName, javaVersion);
        return userAgent;
    }

    public static URI getUri(String accessToken) {
        if (uris.containsKey(accessToken)) {
            return uris.get(accessToken);
        }

        String path = "/graphql";
        String url;
        if (accessToken.startsWith("sk_live")) {
            url = "https://public.api.socio.events" + path;
        } else {
            url = "https://public.sandbox-api.socio.events" + path;
        }

        URI uri = URI.create(url);
        uris.put(accessToken, uri);

        return uri;
    }

    public static String getIntrospectionQuery() {
        if (introspectionQuery != null) {
            return introspectionQuery;
        }

        try {
            ClassLoader classLoader = Helpers.class.getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("introspection.query")).getFile());
            InputStream inputStream = new FileInputStream(file);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            introspectionQuery = sb.toString();
        } catch (IOException ignored) {
        }
        return introspectionQuery;
    }
}
