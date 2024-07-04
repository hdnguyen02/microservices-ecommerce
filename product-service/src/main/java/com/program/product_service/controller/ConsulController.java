package com.program.product_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsulController {


    @GetMapping("/health")
    public String health() {
        return "health";
    }
}
