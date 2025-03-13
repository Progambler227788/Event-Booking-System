package com.talhaatif.ticketbook.entities.events;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSocialMedia {
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
}
