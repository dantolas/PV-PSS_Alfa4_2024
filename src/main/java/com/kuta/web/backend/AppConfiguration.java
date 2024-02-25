package com.kuta.web.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AppConfiguration
 */
@Configuration
public class AppConfiguration {

    @Bean
    public boolean running(){
        return true;
    }


    @Bean
    public int port() {
        return 9876;
    }
    
}
