package com.sme.flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Springboot application based on WebFlux.
 */
@SpringBootApplication
public class SpringReactiveBackPressureApplication
{
    /**
     * Main entry point.
     * 
     * @param args The list of arguments for the JVM.
     */
    public static void main(String[] args)
    {
        SpringApplication.run(SpringReactiveBackPressureApplication.class, args);
    }
}
