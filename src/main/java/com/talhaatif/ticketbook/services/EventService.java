package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.exceptions.ResourceMissingException;
import com.talhaatif.ticketbook.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    //  Get Event by ID
    public Event getEventById(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceMissingException("Event not found with ID: " + id));
    }

    // Create New Event
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    // Get Events by Category
    public List<Event> getEventsByCategory(Category category) {
        return eventRepository.findByCategory(category);
    }

    // Update Event
    public Event updateEvent(String id, Event updatedEvent) {
        //
        Event event = getEventById(id);
        if(!event.getTitle().equalsIgnoreCase(updatedEvent.getTitle())){
            event.setTitle(updatedEvent.getTitle());
        }
        if(!event.getLocation().equalsIgnoreCase(updatedEvent.getLocation())){
            event.setLocation(updatedEvent.getLocation());
        }

        return eventRepository.save(event);
    }

    // Delete Event
    public void deleteEvent(String id) {
        Event event = getEventById(id);
        eventRepository.delete(event);
    }
}
