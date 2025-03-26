package com.talhaatif.ticketbook.service;

import static org.junit.jupiter.api.Assertions.*;

import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
        event.setTotalSeats(11);
        event.setSeats(List.of(
                new Seat("1", true, 20.0, 0L),
                new Seat("2", true, 20.0, 0L),
                new Seat("3", true, 20.0, 0L),
                new Seat("4", true, 20.0, 0L),
                new Seat("5", true, 20.0, 0L),
                new Seat("6", true, 20.0, 0L),
                new Seat("7", true, 10.0, 0L),
                new Seat("8", true, 10.0, 0L),
                new Seat("9", true, 10.0, 0L),
                new Seat("10", true, 10.0, 0L),
                new Seat("11", true, 10.0, 0L)
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
    public void testConcurrentBookingsForTwoSeats() throws Exception {
        System.out.println("\n🔍 Starting concurrent booking test for two seats...");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1); // Ensures both threads start at the same time

        Future<Boolean> future1 = executor.submit(() -> {
            startLatch.await(); // Wait until both threads are ready
            boolean success = bookSeats(userId, eventId, List.of("6")); // Booking seat 6
            System.out.println(Thread.currentThread().getName() + " 📝 Booking Status: " + (success ? "✅ Success" : "❌ Failed"));
            return success;
        });

        Future<Boolean> future2 = executor.submit(() -> {
            startLatch.await();
            boolean success = bookSeats(userId, eventId, List.of("6")); // Booking seat 6 (same seat)
            System.out.println(Thread.currentThread().getName() + " 📝 Booking Status: " + (success ? "✅ Success" : "❌ Failed"));
            return success;
        });

        // Release both threads at the same time
        startLatch.countDown();

        boolean booking1 = future1.get();
        boolean booking2 = future2.get();

        executor.shutdown();

        System.out.println("\n✅ Final Booking Results:");
        System.out.println("🔹 Thread 1: " + (booking1 ? "✅ Success" : "❌ Failed"));
        System.out.println("🔹 Thread 2: " + (booking2 ? "✅ Success" : "❌ Failed"));

        // Check if both succeeded (which should NOT happen)
        if (booking1 && booking2) {
            System.out.println("❌ Test FAILED: Both bookings succeeded concurrently.");
        } else {
            System.out.println("✅ Test PASSED: One of the bookings failed as expected.");
        }
    }

    @Test
    public void testConcurrentBookingsForSameSeats() throws Exception {
        System.out.println("\n🔍 Starting concurrent booking test for the same seats...");

        int numUsers = 5; // Change to 3, 4, or 5 for different test cases
        ExecutorService executor = Executors.newFixedThreadPool(numUsers);
        CountDownLatch startLatch = new CountDownLatch(1); // Ensures all threads start together

        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < numUsers; i++) {
            int userIndex = i + 1;
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for the signal to start
                    boolean success = bookSeats(userId, eventId, List.of("6")); // All users book seat 6
                    if (success) {
                        System.out.println(Thread.currentThread().getName() + " ✅ successfully booked the seat.");
                    } else {
                        System.out.println(Thread.currentThread().getName() + " ❌ booking failed!");
                    }
                    return success;
                } catch (Exception ex) {
                    System.out.println(Thread.currentThread().getName() + " ❌ Exception: " + ex.getMessage());
                    return false;
                }
            }));
        }

        Thread.sleep(100); // Ensure all threads are ready
        startLatch.countDown(); // Start all threads simultaneously

        int successfulBookings = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) successfulBookings++;
        }

        executor.shutdown();

        System.out.println("\n✅ Total successful bookings: " + successfulBookings);
        System.out.println("❌ Expected failures: " + (numUsers - 1)); // Only 1 user should succeed

        assertEquals(1, successfulBookings, "❌ More than one booking succeeded for the same seat!");
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
            Booking result = bookingService.bookWithWallet(userId, eventId, seatNumbers);
            if (result!=null) {
                System.out.println("✅ Booking succeeded with ID: " + result.getId());
                System.out.println("✅ Booking succeeded for seats: " + seatNumbers);
            }
            return true; // Booking success
        } catch (Exception e) {
            System.out.println("OPTIMISTIC LOCKING") ;
            System.out.println(e.getMessage());
            System.out.println("❌ Booking failed for seats: " + seatNumbers);
            return false; // Booking failed
        }
    }


    @Test
    public void testBookSeatsWithTransaction() {
        System.out.println("🔍 Testing seat booking with transaction...");

        List<String> seatNumbers = List.of("8", "9");
        Booking booking = bookingService.bookWithWallet(userId, eventId, seatNumbers);

        assertNotNull(booking, "Booking should not be null");
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus(), "Booking status should be CONFIRMED");
        assertEquals(seatNumbers.size(), booking.getSeats().size(), "Number of booked seats should match");

        // Verify seats are marked as unavailable
        Event event = eventRepository.findById(eventId).orElseThrow();
        for (String seatNumber : seatNumbers) {
            assertFalse(event.getSeats().stream()
                    .filter(seat -> seat.getSeatNumber().equals(seatNumber))
                    .findFirst().get().isAvailable(), "Seats should be marked as unavailable");
        }
    }

    @Test
    public void testCancelBooking() {
        System.out.println("🔍 Testing booking cancellation...");

        // Book some seats first
        List<String> seatNumbers = List.of("10", "11");
        Booking booking = bookingService.bookWithWallet(userId, eventId, seatNumbers);

        // Cancel the booking
        Booking cancelledBooking = bookingService.cancelBooking(booking.getId());

        assertNotNull(cancelledBooking, "Cancelled booking should not be null");
        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus(), "Booking status should be CANCELLED");

        // Verify seats are marked as available again
        Event event = eventRepository.findById(eventId).orElseThrow();
        for (String seatNumber : seatNumbers) {
            assertTrue(event.getSeats().stream()
                    .filter(seat -> seat.getSeatNumber().equals(seatNumber))
                    .findFirst().get().isAvailable(), "Seats should be marked as available again");
        }
    }

}
