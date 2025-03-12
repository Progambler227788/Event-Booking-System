package com.talhaatif.ticketbook.entities.events;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;
import  lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "events")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Event {

    @Id
    private String id;
    private String title;
    private String description;
    private Category category; // ENUM: MOVIE, CONCERT, SPORTS, FLIGHT
    private String location;
    private Date dateTime;
    private List<Seat> seats;
    private double basePrice;
    private double rating;
    private int totalSeats; //  this field has getter and setter

}