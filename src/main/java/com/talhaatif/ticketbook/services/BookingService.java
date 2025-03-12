package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.Seat;
import com.talhaatif.ticketbook.entities.payments.Payment;
import com.talhaatif.ticketbook.entities.payments.PaymentStatus;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.exceptions.ResourceMissingException;
import com.talhaatif.ticketbook.repositories.BookingRepository;
import com.talhaatif.ticketbook.repositories.BookingRepositoryImpl;
import com.talhaatif.ticketbook.repositories.EventRepository;
import com.talhaatif.ticketbook.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BookingService {

    @Autowired
    private  BookingRepositoryImpl bookingRepositoryImpl;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    // Book Seats for an Event
    public Booking bookSeats(String userId, String eventId, int numSeats, String paymentMethod) {
        // 1️⃣ Fetch Event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceMissingException("Event not found"));

        // 2️⃣ Fetch User
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new ResourceMissingException("User not found"));

        // 3️⃣ Find Available Seats
        List<Seat> availableSeats = event.getSeats().stream()
                .filter(Seat::isAvailable)
                .limit(numSeats)
                .toList();

        if (availableSeats.size() < numSeats) {
            throw new RuntimeException("Only " + availableSeats.size() + " seats left");
        }

        // 4️⃣ Calculate Total Amount
        double totalAmount = availableSeats.stream()
                .mapToDouble(Seat::getPrice)
                .sum();

        // 5️⃣ Handle Payment
        Payment payment = null;

        if ("WALLET".equalsIgnoreCase(paymentMethod)) {
            // Check if user has enough balance in wallet
            if (user.getWallet().getBalance() < totalAmount) {
                throw new RuntimeException("Insufficient balance in wallet");
            }

            // Deduct amount from wallet
            user.getWallet().setBalance(user.getWallet().getBalance() - totalAmount);

            // Create Payment
            payment = Payment.builder()
                    .amount(totalAmount)
                    .method("WALLET")
                    .status(PaymentStatus.SUCCESS)
                    .timestamp(new Date())
                    .userId(userId)
                    .build();

        } else if ("STRIPE".equalsIgnoreCase(paymentMethod)) {
            // Handle Stripe Payment
            try {
                // Create a PaymentIntent with Stripe
                Map<String, String> paymentIntent = paymentService.createPaymentIntent(totalAmount, "usd", userId);

                // Create Payment
                payment = Payment.builder()
                        .amount(totalAmount)
                        .method("STRIPE")
                        .status(PaymentStatus.SUCCESS)
                        .timestamp(new Date())
                        .userId(userId)
                        .build();

            } catch (Exception e) {
                throw new RuntimeException("Payment failed: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Invalid payment method");
        }

        // 6️⃣ Mark seats as BOOKED
        availableSeats.forEach(seat -> seat.setAvailable(false));

        // 7️⃣ Create Booking
        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(eventId)
                .seats(availableSeats) // Store booked seats
                .status(BookingStatus.CONFIRMED)
                .payment(payment)
                .createdAt(new Date())
                .build();

        // 8️⃣ Remove booked seats from event's available seats
        event.setSeats(event.getSeats().stream()
                .filter(Seat::isAvailable)
                .toList());

        // 9️⃣ Update totalSeats
        event.setTotalSeats(event.getTotalSeats() - numSeats);

        // 🔟 Save changes
        userRepository.save(user); // Save updated wallet balance
        eventRepository.save(event); // Save updated event
        return bookingRepository.save(booking); // Save booking
    }

    // ✅ Get all bookings for a user
    public List<Booking> getBookingsByUser(String userId) {
        return bookingRepository.findByUserId(userId);
    }


    // ✅ Get all bookings
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Delete all bookings
    public void deleteAllBookings() {
        bookingRepository.deleteAll();
    }

    @Transactional
    public void deleteAllEvents() {
        try {
            eventRepository.deleteAll();
            bookingRepository.deleteAll();
        } catch (Exception e) {
              log.info("Bug occurred in deleting all events");
//            System.out.println(e.getMessage());
            throw new RuntimeException("An error occured in operation");
        }
    }



    public Booking getBookingById(String bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceMissingException("Booking not found with ID: " + bookingId));

        return booking;
    }

    // ✅ Confirm Booking (after payment)
    public Booking confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceMissingException("Booking not found with ID: " + bookingId));

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }


    // ✅ Cancel Booking
    public Booking cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceMissingException("Booking not found"));

        Event event = eventRepository.findById(booking.getEventId())
                .orElseThrow(() -> new ResourceMissingException("Event not found"));

        // 1️⃣ Mark seats as AVAILABLE
        List<String> bookedSeatNumbers = booking.getSeats().stream()
                .map(Seat::getSeatNumber)
                .toList();

        event.getSeats().forEach(seat -> {
            if (bookedSeatNumbers.contains(seat.getSeatNumber())) {
                seat.setAvailable(true);
            }
        });

        // 2️⃣ Restore total seats
        event.setTotalSeats(event.getTotalSeats() + bookedSeatNumbers.size());

        // 3️⃣ Save changes, update status to cancelled

        eventRepository.save(event);
        booking.setStatus(BookingStatus.CANCELLED);
        return  bookingRepository.save(booking);

    }

    // -----------------Filter and Sorting Section
    public List<Booking> filterBookingsByUserId(Integer month, Integer year, BookingStatus status, int page, int size, String userId){
        return  bookingRepositoryImpl.filterBookingsByUserId(month, year, status, page, size, userId);
    }

}

