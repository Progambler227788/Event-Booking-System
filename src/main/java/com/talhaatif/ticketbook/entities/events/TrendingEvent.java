package com.talhaatif.ticketbook.entities.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Document(collection = "trending_events")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TrendingEvent {
    @Id
    private String id;
    private String title;
    private String imageUrl;
    private String location;
    private Date dateTime;
    private int totalSeats;
    private int totalBookedSeats;



    public TrendingEvent(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.imageUrl = event.getImageUrl();
        this.location = event.getLocation();
        this.dateTime = event.getDateTime();
        this.totalSeats = event.getTotalSeats();
        this.totalBookedSeats = event.getTotalBookedSeats();
    }
}