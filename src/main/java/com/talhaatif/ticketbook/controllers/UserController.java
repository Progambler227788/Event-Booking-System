package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.dto.SeatUpdate;
import com.talhaatif.ticketbook.dto.StripeIntentResponse;
import com.talhaatif.ticketbook.dto.UpcomingEvents;
import com.talhaatif.ticketbook.dto.UserInformation;
import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.services.EventService;
import com.talhaatif.ticketbook.services.UserService;
import com.talhaatif.ticketbook.services.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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
import java.util.Optional;

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

    @GetMapping("/details")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getUserById() {

        String userId = getAuthenticatedUserId();
        UserInformation fetchedUser = userService.getUserById(new ObjectId(userId));

        // return not found
        if (fetchedUser==null ) {
            return ResponseEntity.notFound().build();
        }
        // return user details if found
        return ResponseEntity.ok(fetchedUser);
    }


    // --------------------Booking Section---------------------------------



    // ✅ Book Ticket (No try-catch needed since exceptions are handled globally)
    // For Wallet payments

    @PostMapping("/book-with-wallet")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> bookWithWallet(
            @RequestParam String eventId,
            @RequestParam List<String> seatNumbers) {

        String userId = getAuthenticatedUserId();
        Booking booking = userService.bookWithWallet(userId, eventId, seatNumbers);

        broadcastSeatUpdate(eventId, seatNumbers, "BOOKED");

        return ResponseEntity.ok(Map.of(
                "message", "Ticket booked successfully",
                "bookingId", booking.getId()
        ));
    }

    // For Stripe payments - Step 1
    @PostMapping("/create-stripe-intent")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<StripeIntentResponse> createStripePaymentIntent(
            @RequestParam String eventId,
            @RequestParam List<String> seatNumbers) {

        String userId = getAuthenticatedUserId();
        StripeIntentResponse response = userService.createStripePaymentIntent(
                userId, eventId, seatNumbers
        );

        return ResponseEntity.ok(response);
    }

    // For Stripe payments - Step 2
    @PostMapping("/confirm-stripe-booking")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> confirmStripeBooking(
            @RequestParam String paymentIntentId,
            @RequestParam String eventId,
            @RequestParam List<String> seatNumbers) {

        String userId = getAuthenticatedUserId();
        Booking booking = userService.confirmStripeBooking(
                userId, paymentIntentId, eventId, seatNumbers
        );

        broadcastSeatUpdate(eventId, seatNumbers, "BOOKED");
        return ResponseEntity.ok(Map.of(
                "message", "Booking confirmed successfully",
                "bookingId", booking.getId()
        ));
    }

    // Helper method to get user ID
    private String getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserIdByUserName(((UserDetails) auth.getPrincipal()).getUsername());
    }

    // Helper method to broadcast seat updates
    private void broadcastSeatUpdate(String eventId, List<String> seatNumbers, String status) {
        messagingTemplate.convertAndSend("/topic/seats",
                new SeatUpdate(eventId, seatNumbers, status));
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

    @GetMapping(value = "/events/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getEventById(@PathVariable String eventId) {
        System.out.println("Fetching event by ID...");

        return ResponseEntity.ok(eventService.getEventById(eventId));

    }

    @GetMapping("/events/upcomingEvents")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<UpcomingEvents>> fetchAllEvent() {
        System.out.println("Fetching all events...");
        return ResponseEntity.ok(eventService.getAllUpcomingEvents());
    }


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
    public ResponseEntity<Map<String, String>> updateCurrency(@RequestParam String currencyType){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);
        userService.updateCurrencyType(userId,currencyType);
        return ResponseEntity.ok(Map.of("message", "Currency updated for User account"));
    }

    @PutMapping("/wallet/updateLocation")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Map<String, String>> updateLocation(@RequestParam String location){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = ((UserDetails) authentication.getPrincipal()).getUsername();
        String userId = userService.getUserIdByUserName(authenticatedUserName);
        userService.updateLocation(userId,location);
        return ResponseEntity.ok(Map.of("message", "Location updated for User account"));
    }

}
