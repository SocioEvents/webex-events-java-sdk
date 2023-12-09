⚠️ This library has not been released yet.
# Webex Events Api

Webex Events provides a range of additional SDKs to accelerate your development process.
They allow a standardized way for developers to interact with and leverage the features and functionalities.
Pre-built code modules will help access the APIs with your private keys, simplifying data gathering and update flows.

Requirements
-----------------

TODO:

Installation
-----------------

TODO:

Configuration
-----------------

TODO:

Usage
-----------------
TODO:

Idempotency
-----------------
The API supports idempotency for safely retrying requests without accidentally performing the same operation twice.
When doing a mutation request, use an idempotency key. If a connection error occurs, you can repeat
the request without risk of creating a second object or performing the update twice.

To perform mutation request, you must add a header which contains the idempotency key such as
`Idempotency-Key: <your key>`. The SDK does not produce an Idempotency Key on behalf of you if it is missed.
The SDK also validates the key on runtime, if it is not valid UUID token it will raise an exception. Here is an example
like the following:

TODO: 

Telemetry Data Collection
-----------------
Webex Events collects telemetry data, including hostname, operating system, language and SDK version, via API requests.
This information allows us to improve our services and track any usage-related faults/issues. We handle all data with
the utmost respect for your privacy. For more details, please refer to the Privacy Policy at https://www.cisco.com/c/en/us/about/legal/privacy-full.html

Development
-----------------

TODO:

Contributing
-----------------
Please see the [contributing guidelines](CONTRIBUTING.md).

License
-----------------

The gem is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).

Code of Conduct
-----------------

Everyone interacting in the Webex Events API project's codebases, issue trackers, chat rooms and mailing lists is expected to follow the [code of conduct](https://github.com/SocioEvents/webex-events-java-sdk/blob/main/CODE_OF_CONDUCT.md).
