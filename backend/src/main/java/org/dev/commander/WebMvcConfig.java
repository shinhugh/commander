package org.dev.commander;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan("org.dev.commander.controller")
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private static final String DEV_ORIGIN = "http://localhost";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration registration = registry.addMapping("/api/**");
        String originVar = System.getenv("ORIGINS");
        if (originVar == null) {
            registration.allowedOrigins(DEV_ORIGIN);
        } else {
            String[] origins = originVar.split(",");
            String[] originsWithDev = new String[origins.length + 1];
            System.arraycopy(origins, 0, originsWithDev, 0, origins.length);
            originsWithDev[originsWithDev.length - 1] = DEV_ORIGIN;
            registration.allowedOrigins(originsWithDev);
        }
        registration.allowedMethods("*");
    }
}
