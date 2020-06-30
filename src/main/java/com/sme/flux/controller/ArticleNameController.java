package com.sme.flux.controller;

import static java.util.Comparator.comparing;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sme.flux.exception.ResourceNotFoundException;
import com.sme.flux.model.ArticleName;
import com.sme.flux.service.ArticleNameInMemoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

/**
 * A simple rest controller to work with {@link ArticleName} instances using SpringReactive project.
 */
@RestController
@RequestMapping(value = "/v1/article-names")
public class ArticleNameController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleNameController.class);
    private final ArticleNameInMemoryService articleNameInMemoryService;

    public ArticleNameController(ArticleNameInMemoryService articleNameInMemoryService)
    {
        this.articleNameInMemoryService = articleNameInMemoryService;
    }

    /**
     * Prepare {@link ParallelFlux} instance with filtered article names.
     * 
     * @param queryName The request parameter to filter a list of article names by name;
     * @param delay The parameter to delay elements while processing a result;
     * @return Returns {@link ParallelFlux} instance with filtered list of article names.
     */
    @GetMapping(value = "/parallel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ParallelFlux<ArticleName> getActiveArticlesByQueryInParallelFlux(@RequestParam(value = "qName", required = false) String queryName,
            @RequestParam(value = "delay", defaultValue = "0") long delay)
    {
        ParallelFlux<ArticleName> parallelFlux = articleNameInMemoryService.findAll()
                .parallel() // number of CPU cores
                .filter(ArticleName::isActiveFlg)
                .filter(an ->
                {
                    return Optional.ofNullable(queryName).map(k -> an.getName().contains(k)).orElse(false);
                })
                .doOnNext(an ->
                {
                    LOGGER.debug("Do on Next element in publisher {} article name", an);
                })
                .doOnEach(an ->
                {
                    LOGGER.debug("Do on Each element in subscriber: {} article name", an);
                    sleep(delay);
                })
                .doOnComplete(() ->
                {
                    LOGGER.debug("Completed");
                });

        return parallelFlux;
    }

    /**
     * Prepare response of {@link Flux} instance with filtered article names.
     * 
     * @param queryName The request parameter to filter a list of article names by name;
     * @param page The number of page;
     * @param size The size of elements in page;
     * @param delay The parameter to delay elements while processing a result;
     * @return Returns response instance with filtered list of article names.
     */
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ArticleName>> getActiveArticlesByQuery(@RequestParam(value = "qName", required = false) String queryName,
            @RequestParam(value = "page", defaultValue = "0") long page,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(value = "delay", defaultValue = "0") long delay)
    {
        Flux<ArticleName> flux = articleNameInMemoryService.findAll()
                .filter(ArticleName::isActiveFlg)
                .filter(an ->
                {
                    return Optional.ofNullable(queryName).map(k -> an.getName().contains(k)).orElse(true);
                })
                .doOnNext(an ->
                {
                    LOGGER.debug("Do on Next element in publisher {} article name", an);
                })
                .doOnEach(an ->
                {
                    LOGGER.debug("Do on Each element in subscriber: {} article name", an);
                    sleep(delay);
                })
                .doOnComplete(() ->
                {
                    LOGGER.debug("Completed");
                })
                .sort(comparing(ArticleName::getId))
                .skip(page * size)
                .take(size);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(flux);
    }

    /**
     * Fetch article name by id.
     * 
     * @param id The ID variable path in URI;
     * @return Returns article name.
     */
    @GetMapping("/{id}")
    public Mono<ArticleName> get(@PathVariable("id") Long id)
    {
        return articleNameInMemoryService.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Article not found by id= " + id)));
    }

    private void sleep(long delay)
    {
        if (delay > 0)
        {
            try
            {
                Thread.sleep(delay);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
