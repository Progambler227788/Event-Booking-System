package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.services.UserService;
import com.talhaatif.ticketbook.services.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")// Base URL
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
                                                          @RequestParam List<String> seatNumbers,@RequestParam String paymentMethod) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);

        userService.bookTickets(userId, eventId, seatNumbers, paymentMethod);
        return ResponseEntity.ok(Map.of("message", "Ticket booked successfully"));
    }

    // ✅ Cancel Ticket
    @DeleteMapping("/cancelTicket")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> cancelTicket(@RequestParam String bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);


        userService.cancelBooking(userId, bookingId);

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


    @GetMapping("/filter")
    public ResponseEntity<?> filterBookings(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);

        List<Booking> filteredBookings = userService.filterBookingsByUserId(month, year, status, page, size, userId);
        return ResponseEntity.ok(filteredBookings);
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

    @GetMapping("/wallet/getUserBalance")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getUserBalance() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);
        return ResponseEntity.ok(userService.getBalance(userId));
    }

    @PutMapping("/wallet/updateCurrency")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> deductBalance(@RequestParam String currencyType){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);
        userService.updateCurrencyType(userId,currencyType);
        return ResponseEntity.ok(Map.of("message", "Currency updated for User account"));
    }

}
