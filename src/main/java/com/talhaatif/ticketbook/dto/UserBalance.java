package com.talhaatif.ticketbook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserBalance {
    double balance;
    String currencyType;
    String userName;

}
