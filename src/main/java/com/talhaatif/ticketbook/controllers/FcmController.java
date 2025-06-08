package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.entities.notification.DeviceToken;
import com.talhaatif.ticketbook.repositories.DeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/fcm")
public class FcmController {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<Void> registerToken(@RequestParam String userId, @RequestParam String token) {
        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setUserId(userId);
        deviceToken.setToken(token);
        deviceTokenRepository.save(deviceToken);
        return ResponseEntity.ok().build();
    }
}

