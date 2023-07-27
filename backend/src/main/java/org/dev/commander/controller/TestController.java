package org.dev.commander.controller;

import org.dev.commander.service.external.ExternalTestService;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Order(-1)
public class TestController {
    private final ExternalTestService externalTestService;

    public TestController(ExternalTestService externalTestService) {
        this.externalTestService = externalTestService;
    }

    @GetMapping
    public void test() {
        externalTestService.test();
    }
}
