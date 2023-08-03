package org.dev.commander;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan("org.dev.commander.controller")
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private static final String ALLOWED_ORIGIN = "http://localhost";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/api/**")
                .allowedOrigins(ALLOWED_ORIGIN)
                .allowedMethods("*");
    }
}
