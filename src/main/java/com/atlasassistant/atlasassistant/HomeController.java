package com.atlasassistant.atlasassistant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Atlas Assistant is running";
    }

    @GetMapping("/health")
    public String health() {
        return "Atlas Assistant is healthy";
    }

}