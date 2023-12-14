package com.webex.events;

import com.webex.events.exceptions.AccessTokenIsRequiredError;
import com.webex.events.exceptions.InvalidUUIDFormatError;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

public class Helpers {

    public static final String UUID_REGEX_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    public static final String UUID_ERROR_MESSAGE = "Idempotency-Key must be UUID format";
    public static final String ACCESS_TOKEN_IS_MISSING = "Access token is missing.";
    private static String sdkVersion = null;
    private static String userAgent = null;
    private static String introspectionQuery = null;
    private static HashMap<String, URI> uris = new HashMap<>();

    static String getSDKVersion() {
        try {
            if (sdkVersion == null) {
                Properties properties = new Properties();
                properties.load(Client.class.getResourceAsStream("/version.properties"));
                return sdkVersion = properties.getProperty("version");
            }
        } catch (IOException ignored) {
            sdkVersion = "";
        }
        return sdkVersion;
    }
    static void validateIdempotencyKey(Object key) throws InvalidUUIDFormatError {
        if (key == null || key.toString().isEmpty()) {
            return;
        }

        Pattern regex = Pattern.compile(UUID_REGEX_PATTERN);

        if (regex.matcher(key.toString()).matches()) {
            return;
        }

        throw new InvalidUUIDFormatError(UUID_ERROR_MESSAGE);
    }

    static void validateAccessTokenExistence(String accessToken) throws AccessTokenIsRequiredError {
        if (accessToken == null || accessToken.isEmpty()) {
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
        return introspectionQuery = """
                query IntrospectionQuery {
                  __schema {
                   \s
                    queryType { name }
                    mutationType { name }
                    subscriptionType { name }
                    types {
                      ...FullType
                    }
                    directives {
                      name
                      description
                      locations
                     \s
                      args {
                        ...InputValue
                      }
                    }
                  }
                }
                fragment FullType on __Type {
                  kind
                  name
                  description
                 \s
                 \s
                  fields(includeDeprecated: true) {
                    name
                    description
                    args {
                      ...InputValue
                    }
                    type {
                      ...TypeRef
                    }
                    isDeprecated
                    deprecationReason
                  }
                  inputFields {
                    ...InputValue
                  }
                  interfaces {
                    ...TypeRef
                  }
                  enumValues(includeDeprecated: true) {
                    name
                    description
                    isDeprecated
                    deprecationReason
                  }
                  possibleTypes {
                    ...TypeRef
                  }
                }
                fragment InputValue on __InputValue {
                  name
                  description
                  type { ...TypeRef }
                  defaultValue
                 \s
                 \s
                }
                fragment TypeRef on __Type {
                  kind
                  name
                  ofType {
                    kind
                    name
                    ofType {
                      kind
                      name
                      ofType {
                        kind
                        name
                        ofType {
                          kind
                          name
                          ofType {
                            kind
                            name
                            ofType {
                              kind
                              name
                              ofType {
                                kind
                                name
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """;
    }
}
