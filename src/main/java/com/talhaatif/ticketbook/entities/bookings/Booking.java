package com.talhaatif.ticketbook.entities.bookings;

import com.talhaatif.ticketbook.entities.events.Seat;
import com.talhaatif.ticketbook.entities.payments.Payment;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    private String id;
    private String userId;
    private String eventId;
    private List<Seat> seats;
    private BookingStatus status; // ENUM: PENDING, CONFIRMED, CANCELLED
    private Payment payment;
    private Date createdAt;
}

// how to handle if user book all seats for particular event