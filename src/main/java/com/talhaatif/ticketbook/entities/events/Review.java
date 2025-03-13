package com.talhaatif.ticketbook.entities.events;


import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private String userId;
    private String comment;
    private double rating;
    private Date reviewDate;
}
