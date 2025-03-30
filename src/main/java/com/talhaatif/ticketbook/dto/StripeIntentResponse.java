package com.talhaatif.ticketbook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StripeIntentResponse {
    private String clientSecret;
    private String ephemeralKey;
    private String customerId;
    private String publishableKey;
}
