# SpringReactive BackPressure example

Personally I did not find a good tutorial how to start SpringReactive and understand how it works in reallity.

This simple project describes a full lifecycle of Spring WebFlux in examples.

Also the project shows how BackPressure works in ProjectReactive to avoid bottlenecks in backend services.

## Description

### Start with SpringReactive based on ProjectReactive framework.

First of all check how reactive pipeline works in general in <a href="https://github.com/StepanMelnik/ProjectReactor_Examples">ProjectReactor_Examples</a> project.

Pay attention on <a href="https://github.com/StepanMelnik/ProjectReactor_Examples/blob/master/src/test/java/com/sme/reactor/ReactorBackpressureTest.java">ReactorBackpressureTest.java</a> unit tests to work with BackPressure based on a limit of requests in subscriber.

When you understand how ProjectReactive works, continue with current project.

#### Netty server

When we start SpringBoot with reactive project, <a href="https://en.wikipedia.org/wiki/Netty_(software)#:~:text=Netty%20is%20a%20non%2Dblocking,TCP%20and%20UDP%20socket%20servers.">Non-blocking I/O Netty server</a> will be started.

When you start the current project, you should see the following in the logs:

    org.springframework.boot.web.embedded.netty.NettyWebServer: Netty started on port(s): 8010

Let's say <a href="https://www.elastic.co/">Elastic Search</a> based on Netty Server and has quite good performance.

#### WebFlux lifecycle

The example works with **Flux** and **ParallelFlux** publishers.

Both implementation works in Non-blocking mode.

The most interesting is ParallelFlux publisher that is described how to work with.

To understand the reactive lifecycle of WebFlux, check the following:
* Open "getActiveArticlesByQueryInParallelFlux" method in <a href="https://github.com/StepanMelnik/SpringReactiveBackPressure_Examples/blob/master/src/main/java/com/sme/flux/controller/ArticleNameController.java#L48">ArticleNameController.java</a> class;
* This method returns **ParallelFlux** publisher that works with some pipeline operations to prepare data;
* Pay attention that Publisher (Flux or Mono instance) starts to work on subscriber only. So the @RestController prepares Flux instance only that will be processed in subscriber implemented in Spring WebFlux core;
* "doOnEach" method emits each item in subscriber to send to the client. So we will work with the method to see how a client and Netty server communicates with.

 
##### Example in Browser
* start <a href="https://github.com/StepanMelnik/SpringReactiveBackPressure_Examples/blob/master/src/main/java/com/sme/flux/SpringReactiveBackPressureApplication.java">SpringReactiveBackPressureApplication.java</a> that will start Netty on 8010 port;
* open <a href="http://localhost:8010/v1/article-names/parallel?qName=2&delay=5000">http://localhost:8010/v1/article-names/parallel?qName=2&delay=5000</a> in browser. The request filters all list of article names and filters by ArticleName#name:contains("2"). Also the request works with "delay" parameter that will delay to send each element from the server to client to see how it works.
* Also you may put Breakpoint in "doOnEach" method to see what element is prepared to send back to the client.

![Example in Browser](https://github.com/StepanMelnik/SpringReactiveBackPressure_Examples/blob/master/images/ExampleInBrowser.png?raw=true)


Every ArticleName POJO will be send back to the client with delay=5000ms.

##### Example in java client
As a rule Spring WebFlux is demonstrated with javascript.

But it's hard to understand all aspects if a developer did not work enough with reactive javascript frameworks.

<a href="https://github.com/StepanMelnik/SpringReactiveBackPressure_Examples/blob/master/src/test/java/com/sme/flux/controller/ArticleNameControllerIntegrationTest.java#L40">ArticleNameControllerIntegrationTest#testGetActiveArticlesByQueryInParallelFlux</a> tests RestController by Netty client.

Just check "subscribe" callback of "exchange" function.

Netty will prepare ArticleName POJO in subscriber and will send a response every 5000ms (delay in the request parameters).
And the client "exchange" function will get the entity in subscriber every 5000ms.

![Example in Netty Client](https://github.com/StepanMelnik/SpringReactiveBackPressure_Examples/blob/master/images/ExampleInNettyClient.png?raw=true)

Also ArticleNameControllerIntegrationTest.java covers other cases to work with ArticleNameController.

### BackPressure

TODO

## Build

Clone and install <a href="https://github.com/StepanMelnik/Parent.git">Parent</a> project before building.

Clone current project.

### Maven

Start SpringBoot application

> mvn spring-boot:run


Run unit integration tests
> mvn clean test 


