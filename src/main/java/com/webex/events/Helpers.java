package com.webex.events;

import com.webex.events.exceptions.AccessTokenIsRequiredError;
import com.webex.events.exceptions.InvalidUUIDFormatError;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Helpers {

    private static String userAgent = null;

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

        userAgent = String.format("Webex Java SDK(v%s) - OS(%s) - hostname(%s) - Java Version(%s)", PomReader.sdkVersion(), os, hostName, javaVersion);
        return userAgent;
    }

    public static URI getUri(String accessToken) {
        String path = "/graphql";
        String url ;
        if (accessToken.startsWith("sk_live")) {
            url = "https://public.api.socio.events" + path;
        } else {
            url = "https://public.sandbox-api.socio.events" + path;
        }

        return URI.create(url);
    }

    public static String getIntrospectionQuery() {
        return """
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
