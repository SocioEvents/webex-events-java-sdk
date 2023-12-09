package com.webex.events;

import com.webex.events.exceptions.AccessTokenIsRequiredError;
import com.webex.events.exceptions.InvalidUUIDFormatError;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Helpers {

    static void validateIdempotencyKey(Object key) throws InvalidUUIDFormatError {
        if (key != null) {
            Pattern regex = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

            if (!regex.matcher(key.toString()).matches()) {
                throw new InvalidUUIDFormatError("Idempotency-Key must be UUID format");
            }

        }
    }

    static void validateAccessTokenExistence(String accessToken) throws AccessTokenIsRequiredError {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new AccessTokenIsRequiredError("Access token is missing.");
        }
    }

    static String getUserAgent() {
        String os = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");

        String hostName = "";
        try {
            InetAddress id = InetAddress.getLocalHost();
            hostName = id.getHostName();
        } catch (UnknownHostException ignored) {
        }

        return String.format("Webex Java SDK(v%s) - OS(%s) - hostname(%s) - Java Version(%s)", PomReader.sdkVersion(), os, hostName, javaVersion);
    }
}
