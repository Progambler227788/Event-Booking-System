package com.talhaatif.ticketbook.entities;
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
