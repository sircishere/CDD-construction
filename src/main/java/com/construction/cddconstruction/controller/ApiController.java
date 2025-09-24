package com.construction.cddconstruction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/test")
    public String apiTest() {
        return "API is working perfectly! 🚀";
    }

    @GetMapping("/status")
    public String status() {
        return "Backend Status: ONLINE ✅";
    }
}