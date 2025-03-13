package com.talhaatif.ticketbook.entities.events;

import com.stripe.model.Review;
import lombok.*;
import java.util.List;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventReviews {
    private List<Review> reviews; // List of reviews
}