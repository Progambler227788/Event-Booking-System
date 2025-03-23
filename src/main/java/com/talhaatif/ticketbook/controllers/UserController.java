package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.dto.SeatUpdate;
import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.services.EventService;
import com.talhaatif.ticketbook.services.UserService;
import com.talhaatif.ticketbook.services.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")// Base URL
@Slf4j // Logging
@Tag(name = "User APIS",description = "User related apis")
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;
    private final EventService eventService;
    private final SimpMessagingTemplate messagingTemplate; // For WebSocket messaging

    public UserController(UserService userService, BookingService bookingService, EventService eventService, SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.eventService = eventService;
        this.messagingTemplate = messagingTemplate;
    }

    // ✅ Test User Profile
    @GetMapping("/user/hello")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
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

        // Broadcast seat updates to all clients
        SeatUpdate seatUpdate = new SeatUpdate(eventId, seatNumbers, "BOOKED");
        messagingTemplate.convertAndSend("/topic/seats", seatUpdate);


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

   // Filter all bookings of a user by size and page
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



    // --------------------Events Section--------------------

    @GetMapping("/events/trendingEvents")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> fetchTrendingEvents() {
        System.out.println("Fetching trending events...");
        return ResponseEntity.ok(eventService.getTrendingEvents());
    }


    // 🔍 Search events by location with pagination
    @GetMapping("/events/searchByLocation")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> searchEventsByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.searchEventsByLocation(location, page, size));
    }

    // 🔍 Search events by category with pagination
    @GetMapping("/events/searchByCategory")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> searchEventsByCategory(
            @RequestParam Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.searchEventsByCategory(category, page, size));
    }

    // 🔍 Search events by date range with pagination
    @GetMapping("/events/searchByDateRange")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> searchEventsByDateRange(
            @RequestParam Date startTime,
            @RequestParam Date endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.searchEventsByDateRange(startTime, endTime, page, size));
    }

    // 🔍 Search events by title or description with pagination
    @GetMapping("/events/searchByTitleOrDescription")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> searchEventsByTitleOrDescription(
            @RequestParam String queryText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.searchEventsByTitleOrDescription(queryText, page, size));
    }

    // 🔍 Search events by minimum rating with pagination
    @GetMapping("/events/searchByRating")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> searchEventsByRating(
            @RequestParam double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.searchEventsByRating(minRating, page, size));
    }

    // 🔍 Search events with available seats with pagination
    @GetMapping("/events/searchWithAvailableSeats")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> searchEventsWithAvailableSeats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.searchEventsWithAvailableSeats(page, size));
    }

    // 🔍 Search events sorted by date, rating, or price with pagination
    @GetMapping("/events/searchSorted")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> searchEventsSorted(
            @RequestParam String sortBy,
            @RequestParam boolean ascending,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.searchEventsSorted(sortBy, ascending, page, size));
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
