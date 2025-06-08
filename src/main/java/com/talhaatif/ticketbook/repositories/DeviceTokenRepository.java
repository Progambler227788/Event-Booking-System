package com.talhaatif.ticketbook.repositories;

import com.talhaatif.ticketbook.entities.notification.DeviceToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {
    List<DeviceToken> findByUserId(String userId);
}
