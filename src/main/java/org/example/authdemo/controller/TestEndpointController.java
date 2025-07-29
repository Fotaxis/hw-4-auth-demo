package org.example.authdemo.controller;

import org.example.authdemo.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class TestEndpointController {

    @GetMapping("/protected")
    public String protectedEndpoint(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return "Hello, " + user.getLogin() + "! You have accessed a protected endpoint.";
    }
}
