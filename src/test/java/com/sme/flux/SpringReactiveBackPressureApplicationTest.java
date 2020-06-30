package com.sme.flux;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sme.flux.service.ArticleNameInMemoryService;

/**
 * Unit tests of {@link SpringReactiveBackPressureApplication}.
 */
@SpringBootTest(classes = SpringReactiveBackPressureApplication.class)
public class SpringReactiveBackPressureApplicationTest
{
    @Autowired
    private ArticleNameInMemoryService articleNameService;

    @Test
    void testContext() throws Exception
    {
        assertNotNull(articleNameService, "Expects started context");
    }
}
