package com.talhaatif.ticketbook.entities;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet {

    private double balance;
    private String currencyType;
}
