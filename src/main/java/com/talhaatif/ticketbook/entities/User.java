package com.talhaatif.ticketbook.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private List<String> role; // ADMIN, ORGANIZER, CUSTOMER
    private List<String> bookings; // List of Booking IDs
    private  Wallet wallet;
}
