package com.sme.flux.controller;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sme.flux.model.ArticleName;

import reactor.core.publisher.Flux;

/**
 * Server send event controller to generate {@link ArticleName} instance and send to client.
 */
@RestController
@RequestMapping(value = "/v1/article-names/sse")
public class ArticleNameServerSendEventController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleNameController.class);

    /**
     * Generates Server send events.
     * 
     * @return Returns flux instance of generated article name.
     */
    @GetMapping("/name")
    public Flux<String> name()
    {
        return Flux.interval(Duration.ofSeconds(1)).map(l ->
        {
            LOGGER.debug("Generate new ArticleName#name in {} interval time", l);
            return "artname" + l + "<br>";
        });
    }
}
