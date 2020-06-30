package com.sme.flux.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeFunctions;

import com.sme.flux.model.ArticleName;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

/**
 * Integration tests of {@link ArticleNameController}.
 */
public class ArticleNameControllerIntegrationTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleNameControllerIntegrationTest.class);
    private static final String END_POINT = "http://localhost:8010/v1/article-names";

    private final ExchangeFunction exchange = ExchangeFunctions.create(new ReactorClientHttpConnector());   // netty client

    /**
     * The test sends a request to ArticleNameController controller that prepares ParallelFlux and fetches all response pieces step by step in
     * subscriber.
     */
    @Test
    void testGetActiveArticlesByQueryInParallelFlux() throws Exception
    {
        Hooks.onOperatorDebug();

        CountDownLatch callingThreadBlocker = new CountDownLatch(1);

        URI uri = URI.create(END_POINT + "/parallel?qName=2&delay=5000");
        ClientRequest request = ClientRequest.create(HttpMethod.GET, uri).build();

        List<ArticleName> list = new ArrayList<>();

        exchange.exchange(request)
                .flatMapMany(response -> response.bodyToFlux(ArticleName.class))
                .doOnNext(value ->
                {
                    LOGGER.debug("doNextOn: Got {} value on", value);
                })
                .subscribe(value ->
                {
                    LOGGER.debug("Subscribe: Got {} value", value);
                    list.add(value);
                },
                        t ->
                        {
                            LOGGER.error("Got {} error", t);
                        },
                        () ->
                        {
                            LOGGER.debug("Completed callback");
                            callingThreadBlocker.countDown();
                        });

        callingThreadBlocker.await();

        assertEquals(19, list.size());
        assertEquals(19, list.stream().filter(an -> an.getName().contains("2")).count());
    }

    /**
     * The test sends a request to ArticleNameController controller that prepares ParallelFlux and fetches all response pieces step by step in
     * subscriber. The test subscribe the result to Mono and blocks processing indefinitely until the event is finished.
     */
    @Test
    void testGetActiveArticlesByQueryInBlockInParallelFlux() throws Exception
    {
        Hooks.onOperatorDebug();

        URI uri = URI.create(END_POINT + "/parallel?qName=2&delay=5000");
        ClientRequest request = ClientRequest.create(HttpMethod.GET, uri).build();

        Flux<ArticleName> flux = exchange.exchange(request)
                .flatMapMany(response -> response.bodyToFlux(ArticleName.class))
                .doOnNext(value ->
                {
                    LOGGER.debug("doNextOn: Got {} value on", value);
                });

        Mono<List<ArticleName>> monoArticleNames = flux.collectList();
        List<ArticleName> articleNames = monoArticleNames.block();

        assertEquals(19, articleNames.size());
        assertEquals(19, articleNames.stream().filter(an -> an.getName().contains("2")).count());
    }

    /**
     * The test sends a request to ArticleNameController controller that prepares Flux and fetches response.
     */
    @Test
    void testGetActiveArticlesByQuery() throws Exception
    {
        Hooks.onOperatorDebug();

        URI uri = URI.create(END_POINT + "?qName=article&page=2&size=10&delay=10");
        ClientRequest request = ClientRequest.create(HttpMethod.GET, uri).build();

        Flux<ArticleName> flux = exchange.exchange(request)
                .flatMapMany(response -> response.bodyToFlux(ArticleName.class))
                .doOnNext(value ->
                {
                    LOGGER.debug("doNextOn: Got {} value on", value);
                });

        Mono<List<ArticleName>> monoArticleNames = flux.collectList();
        List<ArticleName> articleNames = monoArticleNames.block();

        assertEquals(10, articleNames.size());
        assertEquals(10, articleNames.stream().filter(an -> an.getName().contains("article")).count());
    }
}
