package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.Seat;
import com.talhaatif.ticketbook.exceptions.ResourceMissingException;
import com.talhaatif.ticketbook.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// eventId129
@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    // 🔹 Get Event by ID
    public Event getEventById(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceMissingException("Event not found with ID: " + id));
    }


    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // ✅ Create New Event (Admin Only)
    public Event createEvent(Event event) {
        // Initialize Seats
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= event.getTotalSeats(); i++) {
            seats.add(new Seat("Seat-" + i, true, 100.0,0L));
        }
        event.setSeats(seats);

        return eventRepository.save(event);
    }

    // 🔎 Get Events by Category
    public List<Event> getEventsByCategory(Category category) {
        return eventRepository.findByCategory(category);
    }

    // ✏️ Update Event
    public Event updateEvent(String id, Event updatedEvent) {
        Event event = getEventById(id);
        event.setTitle(updatedEvent.getTitle());
        event.setLocation(updatedEvent.getLocation());
        event.setBasePrice(updatedEvent.getBasePrice());
        return eventRepository.save(event);
    }

    // ❌ Delete Event
    public void deleteEvent(String id) {
        Event event = getEventById(id);
        eventRepository.delete(event);
    }

   //  ❌ Delete all events
    public void deleteAllEvent() {
        eventRepository.deleteAll();
    }
}
