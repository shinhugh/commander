package org.dev.commander.controller;

import org.dev.commander.service.exception.NotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/**")
public class RootController {
    @RequestMapping
    public String throwNotFound() {
        throw new NotFoundException();
    }
}
