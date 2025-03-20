package com.talhaatif.ticketbook.service;

import static org.junit.jupiter.api.Assertions.*;

import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.Seat;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.entities.user.Wallet;
import com.talhaatif.ticketbook.repositories.EventRepository;
import com.talhaatif.ticketbook.repositories.UserRepository;
import com.talhaatif.ticketbook.services.BookingService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private String eventId;
    private String userId;

    @BeforeEach
    public void setup() {
        // Setting up test data: Creating an event with seats
        Event event = new Event();
        event.setTotalSeats(10);
        event.setSeats(List.of(
                new Seat("1", true, 20.0, 0L),
                new Seat("2", true, 20.0, 0L),
                new Seat("3", true, 20.0, 0L),
                new Seat("4", true, 20.0, 0L),
                new Seat("5", true, 20.0, 0L),
                new Seat("6", true, 20.0, 0L),
                new Seat("7", true, 10.0, 0L)
        ));
        event = eventRepository.save(event);
        eventId = event.getId();

        // Creating a user with a wallet balance
        User user = new User();
        user.setId(new ObjectId().toString());
        user.setWallet(new Wallet(500.0, "USD"));
        user = userRepository.save(user); // Saving user in database
        userId = user.getId();
    }

    @Test
    public void testOptimisticLockingWithConcurrentBookings() throws Exception {
        System.out.println("🔍 Starting concurrent booking test...");

        // 5 users
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            futures.add(executor.submit(() -> {
                try {
                    List<String> seatNumbers = List.of("1"); // All threads trying to book seat "1"
                    bookingService.bookSeats(userId, eventId, seatNumbers, "WALLET");
                    System.out.println("✅ Booking succeeded for one thread.");
                    return true; // Booking success
                } catch (Exception e) {
                    System.out.println("❌ Booking failed due to concurrent booking attempt.");
                    return false; // Booking failed due to Optimistic Locking
                }
            }));
        }

        int successfulBookings = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) successfulBookings++;
        }

        executor.shutdown();

        // Only one booking should succeed due to optimistic locking
        if (successfulBookings == 1) {
            System.out.println("✅ Test PASSED: Only one booking succeeded as expected.");
        } else {
            System.out.println("❌ Test FAILED: More than one booking succeeded. Concurrency control is not working!");
        }

        assertEquals(1, successfulBookings,
                "❌ Optimistic Locking failed! Multiple users were able to book the same seat.");
    }

    // Different seats

    @Test
    public void testConcurrentBookingsForDifferentSeats() throws Exception {
        System.out.println("\n🔍 Starting concurrent booking test for different seats...");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Boolean>> futures = new ArrayList<>();

        futures.add(executor.submit(() -> bookSeats(userId, eventId, List.of("7")))); // Thread 1
        futures.add(executor.submit(() -> bookSeats(userId, eventId, List.of("6"))));       // Thread 2
        futures.add(executor.submit(() -> bookSeats(userId, eventId, List.of("4"))));       // Thread 3
        futures.add(executor.submit(() -> bookSeats(userId, eventId, List.of("5"))));       // Thread 4

        int successfulBookings = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) successfulBookings++;
        }

        executor.shutdown();
        // 3, 1 fail

        // Since each thread books different seats, all bookings should succeed
        if (successfulBookings == 4) {
            System.out.println("✅ Test PASSED: All bookings succeeded as expected.");
        } else {
            System.out.println("❌ Test FAILED: Some bookings failed unexpectedly!");
        }

        assertEquals(4, successfulBookings,
                "❌ Unexpected failure! All bookings should have been successful.");
    }

    private boolean bookSeats(String userId, String eventId, List<String> seatNumbers) {
        try {
            bookingService.bookSeats(userId, eventId, seatNumbers, "WALLET");
            System.out.println("✅ Booking succeeded for seats: " + seatNumbers);
            return true; // Booking success
        } catch (Exception e) {
            System.out.println("❌ Booking failed for seats: " + seatNumbers);
            return false; // Booking failed
        }
    }
}
