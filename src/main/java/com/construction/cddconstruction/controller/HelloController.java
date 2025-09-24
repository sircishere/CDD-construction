package com.construction.cddconstruction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "🏗️ Construction App Backend is Running! ✅";
    }

    @GetMapping("/api/test")
    public String apiTest() {
        return "API is working perfectly! 🚀";
    }

    @GetMapping("/api/status")
    public String status() {
        return "Backend Status: ONLINE ✅";
    }
}
