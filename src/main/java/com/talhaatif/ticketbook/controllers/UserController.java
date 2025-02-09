package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.dto.LoginRequest;
import com.talhaatif.ticketbook.dto.SignupRequest;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.security.JwtUtil;
import com.talhaatif.ticketbook.services.UserDetailsServiceImpl;
import com.talhaatif.ticketbook.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
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

// why login call is post call??
    // User Login

    // Global Exception Handling for Better Code Structure
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }

    @GetMapping("/user/hello")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }


    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody LoginRequest authRequest) {

        log.info("Authentication request received for user: {}", authRequest.getUserName());
        log.info("user password in body: {}", authRequest.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );

            log.info("Authentication successful for user: {}", authRequest.getUserName());

            if (authentication.isAuthenticated()) {
                String token = jwtUtil.generateToken(authRequest.getUserName());
                log.info("Generated JWT token for user: {}", authRequest.getUserName());
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
}
