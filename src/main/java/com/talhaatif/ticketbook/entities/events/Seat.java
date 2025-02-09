package com.talhaatif.ticketbook.entities.events;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    private String seatNumber;
    private boolean isAvailable;
    private double price;
}
