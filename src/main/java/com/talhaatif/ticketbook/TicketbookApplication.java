package com.talhaatif.ticketbook;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@OpenAPIDefinition
public class TicketbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketbookApplication.class, args);
	}

}
