package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.services.UserService;
import com.talhaatif.ticketbook.services.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/profile") // Base URL
@Slf4j // Logging
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    public UserController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    // --------------------Booking Section---------------------------------

    // ✅ Book Ticket (No try-catch needed since exceptions are handled globally)
    @PostMapping("/bookTicket")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> bookTicket(@RequestParam String eventId,
                                                          @RequestParam int totalTickets) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);

        userService.bookTickets(userId, eventId, totalTickets);
        return ResponseEntity.ok(Map.of("message", "Ticket booked successfully"));
    }

    // ✅ Cancel Ticket
    @DeleteMapping("/cancelTicket")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> cancelTicket(@RequestParam String bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);

        Booking booking = bookingService.getBookingById(bookingId); // 🔥 If not found, exception is auto-handled globally

        if (!booking.getUserId().equals(userId)) {
            throw new SecurityException("You are not authorized to cancel this booking"); // 🔥 Auto-handled
        }

        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(Map.of("message", "Booking canceled successfully"));
    }

    // ✅ Get All Bookings of a User
    @GetMapping("/getBookings")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getUserBookingsByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);

        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    // ✅ Test User Profile
    @GetMapping("/user/hello")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }

    // --------------------Wallet Section--------------------
    @PostMapping("/wallet/addBalance")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> addBalance(@RequestParam double balance){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);

        userService.addBalance(userId,balance);
        return ResponseEntity.ok(Map.of("message", "Balance Added in User account"));
    }

    @PostMapping("/wallet/deductBalance")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> deductBalance(@RequestParam double balance){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);

        userService.deductBalance(userId,balance);
        return ResponseEntity.ok(Map.of("message", "Balance deducted from User account"));
    }

}
