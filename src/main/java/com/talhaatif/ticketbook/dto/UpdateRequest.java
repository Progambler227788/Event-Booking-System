package com.talhaatif.ticketbook.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateRequest {
    private String userName;
    private String email;
    private String phoneNumber;
}
