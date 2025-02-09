package com.talhaatif.ticketbook.entities.user;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet {

    private double balance;
    private String currencyType;
}
