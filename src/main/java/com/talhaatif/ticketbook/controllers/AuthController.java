package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.dto.SignupRequest;
import com.talhaatif.ticketbook.dto.UpdateRequest;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.services.UserDetailsServiceImpl;
import com.talhaatif.ticketbook.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j

// @RequestMapping is an annotation in Spring Boot
// that is used to map HTTP requests to specific controller classes or methods.
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {

    // required args constructor will provide their instances
    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final UserService userService;

    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody LoginRequest authRequest) {

        log.info("Authentication request received for user: {}", authRequest.getUserName());

        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );

            log.info("Authentication successful for user: {}", authRequest.getUserName());

            if (authentication.isAuthenticated()) {
                // Get user details
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // Extract role
                String role = userDetails.getAuthorities().stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Role not found"))
                        .getAuthority()
                        .replace("ROLE_", ""); // Remove "ROLE_" prefix

                // Generate token
                String token = jwtUtil.generateToken(authRequest.getUserName(), role);
                log.info("Generated JWT token for user: {} with role: {}", authRequest.getUserName(), role);

                // Return token in a JSON object
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Authentication failed for user: {}", authRequest.getUserName());
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid user request"));
            }
        } catch (Exception e) {
            log.error("Authentication error for user: {} - {}", authRequest.getUserName(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication error for user"));
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
        userService.saveNewUser(newUser);

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }

    @PutMapping("/updateProfile")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> updateProfile(@RequestBody UpdateRequest updateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUserName = ((UserDetails) auth.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(loggedInUserName);

        // Validate input fields
        if (updateRequest.getUserName() == null || updateRequest.getUserName().isEmpty() ||
                updateRequest.getEmail() == null || updateRequest.getEmail().isEmpty() ||
                updateRequest.getPhoneNumber() == null || updateRequest.getPhoneNumber().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields must be provided"));
        }

        String newJwtToken = userService.updateUserProfile(userId, updateRequest);

        // Build response
        Map<String, String> response = new HashMap<>();
        response.put("message", "User profile updated successfully");
        if (newJwtToken != null && !newJwtToken.isEmpty()) {
            response.put("jwtToken", newJwtToken);
        }

        return ResponseEntity.ok(response);
    }

}

