package com.talhaatif.ticketbook.dto;

import  lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedTrendingEvent {
    private String id;
    private String title;
    private String imageUrl;
    private String location;
    private Date dateTime;
    private int totalSeats;
    private int totalBookedSeats;
}
