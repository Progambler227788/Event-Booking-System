package com.talhaatif.ticketbook.entities.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "trending_events")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TrendingEvent {

    @Id
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String organizer;
    private String organizerContact;
    private String eventLanguage;
    private int minAge;
    private List<String> tags;
    private Category category;
    private String location;
    private Date dateTime;
    private List<Seat> seats;
    private double basePrice;
    private double rating;
    private int totalBookedSeats;
    private int totalSeats;
    private String termsAndConditions;

    public TrendingEvent(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.imageUrl = event.getImageUrl();
        this.organizer = event.getOrganizer();
        this.organizerContact = event.getOrganizerContact();
        this.eventLanguage = event.getEventLanguage();
        this.minAge = event.getMinAge();
        this.tags = event.getTags();
        this.category = event.getCategory();
        this.location = event.getLocation();
        this.dateTime = event.getDateTime();
        this.seats = event.getSeats();
        this.basePrice = event.getBasePrice();
        this.rating = event.getRating();
        this.totalBookedSeats = event.getTotalBookedSeats();
        this.totalSeats = event.getTotalSeats();
        this.termsAndConditions = event.getTermsAndConditions();
    }

}
