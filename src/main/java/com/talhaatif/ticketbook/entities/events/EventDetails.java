package com.talhaatif.ticketbook.entities.events;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetails {
    private String organizer; // Name of the organizer
    private String organizerContact; // Contact information of the organizer
    private String eventLanguage; // Language of the event
    private int minAge; // Minimum age required to attend
    private List<String> tags; // Tags like "family-friendly", "outdoor", "live-music"
    private List<String> highlights; // List of event highlights
}
