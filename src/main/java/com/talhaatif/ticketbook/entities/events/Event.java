package com.talhaatif.ticketbook.entities.events;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;
import  lombok.*;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "events")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Event {

    @Id
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private Category category; // ENUM: MOVIE, CONCERT, SPORTS, FLIGHT
    private String location;
    private Date dateTime;
    private List<Seat> seats;
    private double basePrice;
    private double rating;
    @Indexed(direction = IndexDirection.DESCENDING)
    private int totalBookedSeats;
    private int totalSeats; //  this field has getter and setter
    private String termsAndConditions; // Terms and conditions for the event

}
//  private List<String> tags; // Tags like "family-friendly", "outdoor", "live-music"
//     private int minAge; // Minimum age required to attend
//private String organizer; // Name of the organizer
//private String organizerContact; // Contact information of the organizer
//private String eventLanguage; // Language of the event

/*
* // Create a new event
Event event = Event.builder()
        .title("Concert Night")
        .description("A night of live music and fun")
        .category(Category.CONCERT)
        .location("City Hall")
        .dateTime(new Date())
        .basePrice(50.0)
        .rating(4.5)
        .totalSeats(100)
        .status(EventStatus.ACTIVE)
        .eventType(EventType.OFFLINE)
        .durationInMinutes(120)
        .maxCapacity(200)
        .details(EventDetails.builder()
                .organizer("Music Inc.")
                .organizerContact("contact@musicinc.com")
                .eventLanguage("English")
                .minAge(18)
                .tags(List.of("live-music", "outdoor"))
                .highlights(List.of("Live band", "Fireworks"))
                .build())
        .socialMedia(EventSocialMedia.builder()
                .facebookUrl("https://facebook.com/event")
                .twitterUrl("https://twitter.com/event")
                .instagramUrl("https://instagram.com/event")
                .build())
        .seating(EventSeating.builder()
                .seats(List.of(new Seat("A1", true, 50.0)))
                .dynamicPricing(Map.of("VIP", 100.0))
                .build())
        .accessibility(EventAccessibility.builder()
                .isWheelchairAccessible(true)
                .hasSignLanguageInterpretation(false)
                .build())
        .policies(EventPolicies.builder()
                .termsAndConditions("No refunds")
                .cancellationPolicy("Cancellations allowed up to 24 hours before the event")
                .build())
        .reviews(EventReviews.builder()
                .reviews(List.of(new Review("user123", "Great event!", 5.0, new Date())))
                .build())
        .sessions(EventSessions.builder()
                .sessions(List.of(new EventSession("session1", new Date(), new Date(), "Main Event")))
                .build())
        .sponsors(EventSponsors.builder()
                .sponsors(List.of("Sponsor A", "Sponsor B"))
                .build())
        .build();

// Save the event
eventRepository.save(event);
* */