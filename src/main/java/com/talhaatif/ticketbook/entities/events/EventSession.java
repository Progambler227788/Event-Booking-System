package com.talhaatif.ticketbook.entities.events;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSession {
    private String sessionId;
    private Date startTime;
    private Date endTime;
    private String description;
}