package com.talhaatif.ticketbook.services;

import com.mongodb.client.result.UpdateResult;
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

    public Booking bookSeats(String userId, String eventId, List<String> seatNumbers, String paymentMethod) {
        int retryCount = 0;

        while (retryCount < 3) {  // Retry mechanism for version conflicts
            try {
                return bookSeatsWithTransaction(userId, eventId, seatNumbers, paymentMethod);
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                if (retryCount == 3) {
                    System.out.println("❌ Booking failed due to concurrent updates. Please try again.");
                    throw new RuntimeException("Booking failed due to concurrent updates. Please try again.");
                }
            }
        }
        throw new RuntimeException("Booking failed.");
    }

    @Transactional(rollbackFor = Exception.class)
    private Booking bookSeatsWithTransaction(String userId, String eventId, List<String> seatNumbers, String paymentMethod) {
        // 1️⃣ Fetch latest Event (with optimistic locking)
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceMissingException("Event not found"));

        // 2️⃣ Fetch User
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new ResourceMissingException("User not found"));

        // 3️⃣ Find Requested Seats
        List<Seat> selectedSeats = event.getSeats().stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNumber()) && seat.isAvailable())
                .collect(Collectors.toList());

        if (selectedSeats.size() < seatNumbers.size()) {
            throw new RuntimeException("Some seats are already booked. Please choose different ones.");
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new RuntimeException("Payment method cannot be null or empty");
        }


        // 5️⃣ Calculate Total Price
        double totalAmount = selectedSeats.stream().mapToDouble(Seat::getPrice).sum();

        if (user.getWallet().getBalance() < totalAmount) {
            throw new RuntimeException("Insufficient balance in wallet");
        }

        // 6️⃣ Process Payment
        Payment payment = handlePayment(userId, user, totalAmount, paymentMethod);


        // 4️⃣ Mark Seats as Booked (Atomic Update)
        Query query = new Query(Criteria.where("_id").is(eventId)
                .and("seats.seatNumber").in(seatNumbers)
                .and("seats.isAvailable").is(true));

        Update update = new Update();

        // Use unique array filter names to avoid conflicts
        for (int i = 0; i < seatNumbers.size(); i++) {
            String seatNumber = seatNumbers.get(i);
            System.out.println(Thread.currentThread().getName() + " Processing seat: " + seatNumber);
            String filterName = "elem" + i; // Unique filter name for each seat
            update.set("seats.$[elem" + i + "].isAvailable", false)
                    .set("seats.$[elem" + i + "].version", 1)
                    .filterArray(Criteria.where("elem" + i + ".seatNumber").is(seatNumbers.get(i)));
        }

        // Increment totalBookedSeats atomically
        update.inc("totalBookedSeats", seatNumbers.size());

        // Execute the update
        UpdateResult result = mongoTemplate.updateFirst(query, update, Event.class);

        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Failed to book seats. Please try again.");
        }



        // 7️⃣ Save User Wallet After Payment
        userRepository.save(user);

        // 8️⃣ Create Booking
        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(eventId)
                .seats(selectedSeats)
                .status(BookingStatus.CONFIRMED)
                .payment(payment)
                .createdAt(new Date())
                .build();

        return bookingRepository.save(booking);
    }
    // handle payment
    Payment handlePayment(String userId, User user, double totalAmount, String paymentMethod){
        // Log the payment method for debugging
        System.out.println("Processing payment with method: " + paymentMethod);

        // Validate payment method


        System.out.println("Equality result " + "WALLET".equalsIgnoreCase(paymentMethod));
        System.out.println("Equality result " + "STRIPE ".equalsIgnoreCase(paymentMethod));

        if ("WALLET".equalsIgnoreCase(paymentMethod)) {

            // Check if user has enough balance in wallet
            if (user.getWallet().getBalance() < totalAmount) {
                throw new RuntimeException("Insufficient balance in wallet");
            }

            // Deduct amount from wallet
            user.getWallet().setBalance(user.getWallet().getBalance() - totalAmount);

            // Create Payment
            return Payment.builder()
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
                return Payment.builder()
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

