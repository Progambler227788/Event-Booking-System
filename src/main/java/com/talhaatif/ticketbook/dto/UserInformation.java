package com.talhaatif.ticketbook.dto;


import com.talhaatif.ticketbook.entities.user.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserInformation {

    private String id;
    private String userName;
    private String email;
    private String location;
    private boolean gender; // true for Male, false for Female
    private String phoneNumber; // +92 so on
    private Wallet wallet;
}
