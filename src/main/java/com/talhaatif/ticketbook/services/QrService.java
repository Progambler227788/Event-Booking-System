package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.exceptions.BusinessRuleException;
import com.talhaatif.ticketbook.exceptions.ConflictException;
import com.talhaatif.ticketbook.exceptions.ResourceMissingException;
import com.talhaatif.ticketbook.repositories.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class QrService {



    @Autowired
    private BookingRepository bookingRepository;


    private final SecretKey jwtSecretKey = Keys.hmacShaKeyFor(
            "your-256-bit-secret-key-123456789012".getBytes(StandardCharsets.UTF_8)
    );

    public Booking verifyAndGetBooking(String qrToken) {
        // 1. Verify JWT
        Claims claims = validateQrToken(qrToken);

        // 2. Get booking from verified claims
        Booking booking = bookingRepository.findById(claims.get("bookingId", String.class))
                .orElseThrow(() -> new ResourceMissingException("Booking not found"));

        // 3. Validate booking state
        validateBookingState(booking);

        return booking;
    }

    private Claims validateQrToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw new BusinessRuleException("QR code has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessRuleException("Invalid QR code");
        }
    }

    private void validateBookingState(Booking booking) {
        if (booking.isCheckedIn()) {
            throw new ConflictException("Ticket already checked in");
        }
        if (!booking.getStatus().equals(BookingStatus.CONFIRMED)) {
            throw new BusinessRuleException("Booking not confirmed");
        }
        if (booking.getEventDate().before(new Date())) {
            throw new BusinessRuleException("Event has already occurred");
        }
    }


    public Booking findByQrCode(String qrPayLoad) {
        Booking booking = bookingRepository.findByQrPayload(qrPayLoad)
                .orElseThrow(() -> new ResourceMissingException("Booking not found with QR Code: " + qrPayLoad));

        return booking;
    }


    private String generateQrPayload(Booking booking) {
        // 1. Use JWT for security and data integrity
        SecretKey key = Keys.hmacShaKeyFor(
                "-256-bit-secret-key-123456789012".getBytes(StandardCharsets.UTF_8)
        );

        // 2. Include essential verification data
        Map<String, Object> claims = new HashMap<>();
        claims.put("bookingId", booking.getId());
        claims.put("eventId", booking.getEventId());
        claims.put("userId", booking.getUserId());

        // 3. Human-readable info for quick validation
        claims.put("eventName", booking.getEventName());
        claims.put("eventDate", booking.getEventDate().getTime()); // Store as timestamp

        // 4. System information
        claims.put("system", "tickojet");
        claims.put("generatedAt", System.currentTimeMillis());

        // 5. Set expiration (e.g., 30 days from now)
        Date expiration = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    public void ensureQrPayloadExists(Booking booking) {
        // Check if the booking already has a QR payload
        if (booking.getQrPayload() != null && !booking.getQrPayload().isEmpty()) {
            return; // QR payload already exists, no need to generate
        }

        // Generate payload if missing
        String qrPayload = booking.getQrPayload();
        if (qrPayload == null || qrPayload.isEmpty()) {
            qrPayload = generateQrPayload(booking);  // New method
            booking.setQrPayload(qrPayload);
            bookingRepository.save(booking);  // Save the updated booking
        }

    }

    public boolean isBookingBelongsToUser(String bookingId, String userId) {
        // Find the booking by ID (you might need to handle cases where booking doesn't exist)
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

        if (bookingOptional.isEmpty()) {
            return false; // or throw an exception if you prefer
        }

        Booking booking = bookingOptional.get();

        // Check if the booking's userId matches the provided userId
        return userId.equals(booking.getUserId());
    }



}
