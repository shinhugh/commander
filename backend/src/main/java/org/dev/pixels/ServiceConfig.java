package org.dev.pixels;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("org.dev.pixels.service")
@EnableScheduling
public class ServiceConfig {
}
