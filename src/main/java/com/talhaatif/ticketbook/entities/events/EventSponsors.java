package com.talhaatif.ticketbook.entities.events;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSponsors {
    private List<String> sponsors; // List of sponsor names or logos
}
