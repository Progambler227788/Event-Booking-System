package com.talhaatif.ticketbook.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatUpdate {
    private String eventId;
    private List<String> seatNumbers;
    private String status; // BOOKED, AVAILABLE, etc.
}