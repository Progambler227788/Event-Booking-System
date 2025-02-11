package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.Seat;
import com.talhaatif.ticketbook.exceptions.ResourceMissingException;
import com.talhaatif.ticketbook.repositories.BookingRepository;
import com.talhaatif.ticketbook.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    // Book Seats for an Event
    public Booking bookSeats(String userId, String eventId, int numSeats) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceMissingException("Event not found"));

        // 1️⃣ Find Available Seats
        List<Seat> availableSeats = event.getSeats().stream()
                .filter(seat -> seat.isAvailable())
                .limit(numSeats)
                .toList();

        if (availableSeats.size() < numSeats) {
            throw new RuntimeException("Only " + availableSeats.size() + " seats left");
        }

        // 2️⃣ Mark seats as BOOKED
        availableSeats.forEach(seat -> seat.setAvailable(false));

        // 3️⃣ Create Booking
        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(eventId)
                .seats(availableSeats) // Store booked seats
                .status(BookingStatus.PENDING)
                .createdAt(new Date())
                .build();

        // 4️⃣ Remove booked seats from event's available seats
        event.setSeats(event.getSeats().stream()
                .filter(seat -> seat.isAvailable() == true)
                .toList());

        // 5️⃣ Update totalSeats
        event.setTotalSeats(event.getTotalSeats() - numSeats);

        // 6️⃣ Save changes
        eventRepository.save(event);
        return bookingRepository.save(booking);
    }

    // ✅ Get all bookings for a user
    public List<Booking> getBookingsByUser(String userId) {
        return bookingRepository.findByUserId(userId);
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
    public void cancelBooking(String bookingId) {
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

        // 3️⃣ Save changes
        eventRepository.save(event);
        bookingRepository.delete(booking);
    }

}

