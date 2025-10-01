![Application Architecture](./Architecture.gif)

# GraphQL Contract Testing Using Specmatic

* [Specmatic Website](https://specmatic.io)
* [Specmatic Documentation](https://specmatic.io/documentation.html)

This sample project demonstrates how we can practice contract-driven development and contract testing in a GraphQL (Kotlin) API that depends on an external domain service. Here, Specmatic is used to stub calls to domain API service based on its OpenAPI specification.

### Starting the server

Run the following script on Linux:

```shell
chmod u+x gradlew
./gradlew bootRun
```

On Windows:

```commandline
gradlew bootRun
```

You'll need the backend product API server running for this to work. You can get it from [here](https://github.com/specmatic/specmatic-order-api-java). The README.md file in the repo contain instructions for starting up the backend API server.

Visit http://localhost:8080/graphiql to access the GraphiQL interface.

### Running the contract tests using Docker

1. Start the Specmatic http stub server to emulate domain service:

   ```shell
      docker run --network host -p 8090:8090 -v "$(pwd)/specmatic.yaml:/usr/src/app/specmatic.yaml" specmatic/specmatic virtualize --port=8090
   ```

2. Build and run the BFF service (System Under Test) in a Docker container:

   ```shell
   docker build --no-cache -t specmatic-order-bff-graphql .
   ```

   ```shell
   docker run -p 8080:8080 specmatic-order-bff-graphql
   ```

   OR, you can run the application locally with `./gradlew bootRun`

3. Finally, run Specmatic Contract on the BFF service (System Under Test):

   ```shell
   docker run --network host -v "$(pwd)/specmatic.yaml:/usr/src/app/specmatic.yaml" -v "$(pwd)/build/reports/specmatic:/usr/src/app/build/reports/specmatic"  -e SPECMATIC_GENERATIVE_TESTS=true specmatic/specmatic-graphql test --port=8080 --host=host.docker.internal
   ```
