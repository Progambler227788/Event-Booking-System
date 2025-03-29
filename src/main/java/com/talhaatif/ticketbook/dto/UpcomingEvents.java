package com.talhaatif.ticketbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingEvents {
    private String id;
    private String title;
    private String imageUrl;
    private String location;
    private Date dateTime;
    private String category;
}
