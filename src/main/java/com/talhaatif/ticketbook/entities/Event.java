package com.talhaatif.ticketbook.entities;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;
import  lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    private String id;
    private String title;
    private Category category; // ENUM: MOVIE, CONCERT, SPORTS, FLIGHT
    private String location;
    private Date dateTime;
    private List<Seat> seats;
    private double basePrice;
    private double rating;
}
