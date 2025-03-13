package com.talhaatif.ticketbook.entities.events;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPolicies {
    private String termsAndConditions; // Terms and conditions for the event
    private String cancellationPolicy; // Cancellation policy details
}
