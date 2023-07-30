package org.dev.commander;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("org.dev.commander.service")
@EnableScheduling
public class ServiceConfig {
}
