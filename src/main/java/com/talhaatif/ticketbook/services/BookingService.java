package com.talhaatif.ticketbook.services;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.mongodb.client.result.UpdateResult;
import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.Seat;
import com.talhaatif.ticketbook.entities.payments.Payment;
import com.talhaatif.ticketbook.entities.payments.PaymentStatus;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.exceptions.ResourceMissingException;
import com.talhaatif.ticketbook.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private MongoTemplate mongoTemplate;


    @Autowired
    private DeviceTokenRepository tokenRepo;

    @Autowired
    private FcmService fcmService;

    // For wallet payments only
    @Transactional(rollbackFor = Exception.class)
    public Booking bookWithWallet(String userId, String eventId, List<String> seatNumbers) {
        // 1. Verify seats and user
        Event event = verifySeatAvailability(eventId, seatNumbers);
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new ResourceMissingException("User not found"));

        // 2. Calculate total
        double totalAmount = calculateTotal(event, seatNumbers);

        // 3. Process wallet payment
        if (user.getWallet().getBalance() < totalAmount) {
            throw new RuntimeException("Insufficient balance in wallet");
        }
        user.getWallet().setBalance(user.getWallet().getBalance() - totalAmount);
        userRepository.save(user);

        // 4. Create payment record
        Payment payment = Payment.builder()
                .amount(totalAmount)
                .method("WALLET")
                .status(PaymentStatus.SUCCESS)
                .timestamp(new Date())
                .userId(userId)
                .build();

        // 5. Mark seats as booked
        markSeatsAsBooked(eventId, seatNumbers);

        // 6. Create booking
        return createBooking(userId, eventId, seatNumbers, payment);
    }

    // For Stripe payments (after successful payment)
    @Transactional(rollbackFor = Exception.class)
    public Booking createBookingAfterStripePayment(
            String userId,
            String eventId,
            List<String> seatNumbers,
            double amountPaid) {

        // 1. Verify seats
        verifySeatAvailability(eventId, seatNumbers);

        // 2. Create payment record
        Payment payment = Payment.builder()
                .amount(amountPaid)
                .method("STRIPE")
                .status(PaymentStatus.SUCCESS)
                .timestamp(new Date())
                .userId(userId)
                .build();

        // 3. Mark seats as booked
        markSeatsAsBooked(eventId, seatNumbers);

        // 4. Create booking
        return createBooking(userId, eventId, seatNumbers, payment);
    }

    // Shared helper methods
    public Event verifySeatAvailability(String eventId, List<String> seatNumbers) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceMissingException("Event not found"));

        long availableSeats = event.getSeats().stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNumber()) && seat.isAvailable())
                .count();

        if (availableSeats < seatNumbers.size()) {
            throw new RuntimeException("Some seats are already booked");
        }

        return event;
    }


    public double calculateTotal(Event event, List<String> seatNumbers) {
        return event.getSeats().stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNumber()))
                .mapToDouble(Seat::getPrice)
                .sum();
    }

    private void markSeatsAsBooked(String eventId, List<String> seatNumbers) {
        Query query = new Query(Criteria.where("_id").is(eventId)
                .and("seats.seatNumber").in(seatNumbers)
                .and("seats.isAvailable").is(true));

        Update update = new Update();
        for (int i = 0; i < seatNumbers.size(); i++) {
            update.set("seats.$[elem" + i + "].isAvailable", false)
                    .set("seats.$[elem" + i + "].version", 1)
                    .filterArray(Criteria.where("elem" + i + ".seatNumber").is(seatNumbers.get(i)));
        }
        update.inc("totalBookedSeats", seatNumbers.size());

        UpdateResult result = mongoTemplate.updateFirst(query, update, Event.class);
        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Failed to book seats");
        }
    }

    private Booking createBooking(
            String userId,
            String eventId,
            List<String> seatNumbers,
            Payment payment) {

        Event event = eventRepository.findById(eventId).orElseThrow();
        List<Seat> selectedSeats = event.getSeats().stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNumber()))
                .collect(Collectors.toList());

        tokenRepo.findByUserId(userId).forEach(deviceToken -> {
            try {
                fcmService.sendNotification(
                        deviceToken.getToken(),
                        "Booking Confirmed",
                        "Your ticket has been successfully booked!"
                );
            } catch (FirebaseMessagingException e) {
                // Log error but don't block main logic
                System.err.println("FCM failed: " + e.getMessage());
            }
        });

        return bookingRepository.save(
                Booking.builder()
                        .userId(userId)
                        .eventId(eventId)
                        .eventName(event.getTitle())
                        .seats(selectedSeats)
                        .eventDate(event.getDateTime())
                        .status(BookingStatus.CONFIRMED)
                        .payment(payment)
                        .createdAt(new Date())
                        .build()
        );
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
                seat.setVersion(seat.getVersion() - 1);
            }
        });

        // 2️⃣ Restore total seats
        event.setTotalBookedSeats(event.getTotalBookedSeats() - bookedSeatNumbers.size());

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

