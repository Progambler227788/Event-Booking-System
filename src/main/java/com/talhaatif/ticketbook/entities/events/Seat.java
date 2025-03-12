package com.talhaatif.ticketbook.entities.events;
import lombok.*;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "seats")
public class Seat {
    private String seatNumber;
    private boolean isAvailable;
    private double price;

    @Version  // Ensures version check on updates
    private Long version;
}
