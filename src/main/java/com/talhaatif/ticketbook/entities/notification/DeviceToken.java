package com.talhaatif.ticketbook.entities.notification;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "DeviceToken")
public class DeviceToken {
    @Id
    private String id;
    private String userId;
    private String token;
}

