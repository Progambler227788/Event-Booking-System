package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.entities.notification.DeviceToken;
import com.talhaatif.ticketbook.repositories.DeviceTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/fcm")
@Slf4j
public class FcmController {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerToken(@RequestParam String userId, @RequestParam String token) {
        log.info("Registering FCM token for user: {}", userId);
        log.info("Token: {}", token);

        if (userId == null || userId.isEmpty() || token == null || token.isEmpty()) {
            log.warn("Invalid userId or token. userId: {}, token: {}", userId, token);
            return ResponseEntity.badRequest().body("userId and token must not be empty");
        }

        try {

            List<DeviceToken> existingTokens = deviceTokenRepository.findByUserId(userId);

            if (existingTokens.isEmpty()) {
                DeviceToken deviceToken = new DeviceToken();
                deviceToken.setUserId(userId);
                deviceToken.setToken(token);
                deviceTokenRepository.save(deviceToken);

            }
            log.info("FCM token registered successfully.");
            return ResponseEntity.ok("Token registered successfully");

        } catch (Exception ex) {
            log.error("Error saving FCM token", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to register token: " + ex.getMessage());
        }
    }

}

