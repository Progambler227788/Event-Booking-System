package com.talhaatif.ticketbook.repositories;

import com.talhaatif.ticketbook.entities.bookings.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId);
    Optional<Booking> findByQrPayload(String qrCode);


}
