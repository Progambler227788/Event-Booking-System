package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.dto.SignupRequest;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.security.JwtUtil;
import com.talhaatif.ticketbook.services.UserDetailsServiceImpl;
import com.talhaatif.ticketbook.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile") // base url
@Slf4j // log purpose

// @RequestMapping is an annotation in Spring Boot
// that is used to map HTTP requests to specific controller classes or methods.
// base url and then endpoints
public class UserController {

    private final UserService userService;
    private  final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Constructor-based dependency injection (Best practice)
    public UserController(UserService userService, UserDetailsServiceImpl userDetailsService,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // End points for get, post, delete and put



// why login call is post call??
    // User Login

    // Global Exception Handling for Better Code Structure
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }

    // alternative of below method @RequestMapping(value = "/profile", method = RequestMethod.GET)

    @GetMapping("/user/hello")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }


}
