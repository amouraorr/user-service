package com.fiap.userservice.adapter.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class UserController {

    @GetMapping("/api/health")
    public String health() { return "user-ok";

    }

}