package com.talhaatif.ticketbook.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import  com.talhaatif.ticketbook.entities.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    User findByUserName(String username);

    void deleteByUserName(String username);

    Optional<User> findByEmail(String email);
}
