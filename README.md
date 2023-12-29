[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE.txt)
[![Webex Events](https://github.com/SocioEvents/webex-events-java-sdk/actions/workflows/maven.yml/badge.svg)](https://github.com/SocioEvents/webex-events-java-sdk/actions)

⚠️ This library has not been released yet.
# Webex Events Api Java SDK

Webex Events provides a range of additional SDKs to accelerate your development process.
They allow a standardized way for developers to interact with and leverage the features and functionalities.
Pre-built code modules will help access the APIs with your private keys, simplifying data gathering and update flows.

Requirements
-----------------

Java 8+

Installation
-----------------
For maven:
```xml
<dependency>
    <groupId>com.webex.events</groupId>
    <artifactId>webex-events</artifactId>
    <version>desired version</version>
</dependency>
```
```shell
$ mvn clean install
```

For Gradle:
```
dependencies {
    compile "com.webex.events:webex-events:<version>"
}
```
```
$ gradle build --refresh-dependencies
```

Configuration
-----------------

```java
Configuration.setAccessToken("sk_live_your_access_token");
Configuration.setTimeout(30); // Optional
Configuration.setMaxRetries(5); // Optional
```

Usage
-----------------
```java
String query = "query CurrenciesList{ currenciesList{ isoCode }}";
String operationName = "CurrenciesList";

try {
    Response response = Client.query(query, operationName);
}catch (DailyQuotaIsReachedError e) {
    // Handle what will happen if daily quota is reached here.
}catch (SecondBasedQuotaIsReachedError e) {
    int retryAfter = e.response().getRateLimiter().getSecondlyRetryAfterInMs();
    if (retryAfter > 0) {
        Thread.sleep(retryAfter);
        // Retry the request.
    }
}catch (ConflictError e) {
    Thread.sleep(250);
    // Retry the request
}
```

By default some HTTP statuses are retriable such as `408, 409, 429, 502, 503, 504`. This library tries this status
codes 5 times by default. If this is not sufficient, increase max retry count through Configuration class or re-catch 
the exceptions to implement your logic here. 

For Introspection Query
-----------------
```java
String json = Client.doIntrospectQuery();
```

Idempotency
-----------------
The API supports idempotency for safely retrying requests without accidentally performing the same operation twice.
When doing a mutation request, use an idempotency key. If a connection error occurs, you can repeat
the request without risk of creating a second object or performing the update twice.

To perform mutation request, you must add a header which contains the idempotency key such as
`Idempotency-Key: <your key>`. The SDK does not produce an Idempotency Key on behalf of you if it is missed.
Here is an example like the following:

```java
String query = "mutation TrackDelete($input: TrackDeleteInput!) {trackDelete(input: $input) {success}}";
String operationName = "TrackDelete";
HashMap<String, Object> variables = new HashMap<>();
RequestOptions options = RequestOptions.newBuilder().setIdempotencyKey(UUID.randomUUID());

HashMap<String, Object> input = new HashMap<>();
input.put("ids",new int[] {
    1, 2, 3
});
input.put("eventId",1);
variables.put("input",input);

Response response = Client.query(query, operationName, variables, options);
```

Telemetry Data Collection
-----------------
Webex Events collects telemetry data, including hostname, operating system, language and SDK version, via API requests.
This information allows us to improve our services and track any usage-related faults/issues. We handle all data with
the utmost respect for your privacy. For more details, please refer to the Privacy Policy at https://www.cisco.com/c/en/us/about/legal/privacy-full.html

Development
-----------------

After checking out the repo, install dependencies. Then run tests.

Contributing
-----------------
Please see the [contributing guidelines](CONTRIBUTING.md).

License
-----------------

The library is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).

Code of Conduct
-----------------

Everyone interacting in the Webex Events API project's codebases, issue trackers, chat rooms and mailing lists is expected to follow the [code of conduct](https://github.com/SocioEvents/webex-events-java-sdk/blob/main/CODE_OF_CONDUCT.md).
