package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.dto.SignupRequest;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.services.UserDetailsServiceImpl;
import com.talhaatif.ticketbook.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.talhaatif.ticketbook.dto.LoginRequest;
import com.talhaatif.ticketbook.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
// @RequestMapping is an annotation in Spring Boot
// that is used to map HTTP requests to specific controller classes or methods.
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private  final UserDetailsServiceImpl userDetailsService;
    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody LoginRequest authRequest) {

        log.info("Authentication request received for user: {}", authRequest.getUserName());

        try {
            // Spring Security internally checks if the username (john_doe) and password (password123) are
            // valid by comparing them against the database or in-memory user details.
            // Creates an object of -> UsernamePasswordAuthenticationToken
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );

            log.info("Authentication successful for user: {}", authRequest.getUserName());
        // if user is authenticated extract its details
            if (authentication.isAuthenticated()) {
                // 🔹 Get user details that has role as well
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // 🔹 Extract role
                String role = userDetails.getAuthorities().stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Role not found"))
                        .getAuthority()
                        .replace("ROLE_", ""); // Remove "ROLE_" prefix

                // 🔹 Generate token
                String token = jwtUtil.generateToken(authRequest.getUserName(), role);
                log.info("Generated JWT token for user: {} with role: {}", authRequest.getUserName(), role);
                return token;
            } else {
                log.warn("Authentication failed for user: {}", authRequest.getUserName());
                throw new UsernameNotFoundException("Invalid user request!");
            }
        } catch (Exception e) {
            log.error("Authentication error for user: {} - {}", authRequest.getUserName(), e.getMessage());
            throw new UsernameNotFoundException("Invalid user request!", e);
        }
    }

    // User Registration (Sign-Up)
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody SignupRequest signupRequest) {
        if (userService.findByUserName(signupRequest.getUserName()) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists!"));
        }

        // Convert DTO to Entity
        User newUser = new User();
        newUser.setUserName(signupRequest.getUserName());
        newUser.setEmail(signupRequest.getEmail());
        newUser.setPassword(signupRequest.getPassword());

        // Save user to DB
        userService.saveAdminUser(newUser);

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }
}

