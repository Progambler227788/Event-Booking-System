package com.talhaatif.ticketbook.entities.events;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSeating {
    private List<Seat> seats; // List of seats
    private Map<String, Double> dynamicPricing; // Key: Seat type, Value: Price
}
