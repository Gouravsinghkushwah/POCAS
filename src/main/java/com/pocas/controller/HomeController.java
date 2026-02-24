package com.pocas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/test")
    public String apiTest() {
        return "API Running";
    }

    // Catch-all to serve React app for unknown paths
    @GetMapping(value = { "/", "/home", "/about", "/dashboard/**", "/login", "/register" })
    public String index() {
        return "forward:/index.html";  // Serves React's index.html for SPA routing
    }
}