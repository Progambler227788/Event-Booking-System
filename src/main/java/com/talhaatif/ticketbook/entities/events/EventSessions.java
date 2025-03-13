package com.talhaatif.ticketbook.entities.events;


import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSessions {
    private List<EventSession> sessions; // List of sessions or schedules
}