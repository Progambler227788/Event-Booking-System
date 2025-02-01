package com.talhaatif.ticketbook.entities;


import  lombok.*;

import java.util.Date;


//@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private double amount;
    private String method; // ENUM: CARD, PAYPAL, WALLET
    private PaymentStatus status; // ENUM: SUCCESS, FAILED, PENDING
    private Date timestamp;
}

