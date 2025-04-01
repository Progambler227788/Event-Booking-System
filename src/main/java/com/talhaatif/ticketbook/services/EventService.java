package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.dto.SimplifiedTrendingEvent;
import com.talhaatif.ticketbook.dto.UpcomingEvents;
import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.Seat;
import com.talhaatif.ticketbook.entities.events.TrendingEvent;
import com.talhaatif.ticketbook.exceptions.ResourceMissingException;
import com.talhaatif.ticketbook.repositories.EventRepository;
import com.talhaatif.ticketbook.repositories.EventRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// eventId129
@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRepositoryImpl eventRepositoryImpl;

    // 🔹 Get Event by ID
    public Event getEventById(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceMissingException("Event not found with ID: " + id));
    }


    public List<SimplifiedTrendingEvent> getTrendingEvents() {
        return  eventRepositoryImpl.getTrendingEvents();
    }


    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<UpcomingEvents> getAllUpcomingEvents() {
        return eventRepository.findAll().stream().map(event ->
                UpcomingEvents.builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .category(event.getCategory().toString())
                        .imageUrl(event.getImageUrl())
                        .location(event.getLocation())
                        .dateTime(event.getDateTime())
                        .build()
        ).collect(Collectors.toList());
    }

    // ✅ Create New Event (Admin Only)
    @Transactional(rollbackFor = Exception.class)
    public Event createEvent(Event event) {
        // Initialize Seats
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= event.getTotalSeats(); i++) {
            seats.add(new Seat("Seat-" + i, true, 100.0,0L));
        }
        event.setSeats(seats);

        // first save
        Event savedEvent = eventRepository.save(event);

        System.out.println("Updating trending event because new event created");

        // then update for trending events
        eventRepositoryImpl.updateTrendingEvents();

        return savedEvent;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Event> addEvents(List<Event> events) {
        for (Event event : events) {
            // Initialize Seats for each event
            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= event.getTotalSeats(); i++) {
                seats.add(new Seat("Seat-" + i, true, event.getBasePrice(), 0L));
            }
            event.setSeats(seats);
        }
        List<Event> savedEvents = eventRepository.saveAll(events); // Save all events>

        eventRepositoryImpl.updateTrendingEvents();

        return savedEvents;
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



    // ------------------------- Filter and Search Section


    // 🔍 Search events by location with pagination
    public List<Event> searchEventsByLocation(String location, int page, int size) {
        return eventRepositoryImpl.searchEventsByLocation(location, page, size);
    }

    // 🔍 Search events by category with pagination
    public List<Event> searchEventsByCategory(String category, int page, int size) {
        Category eventCategory = Category.valueOf(category.toUpperCase());
        return eventRepositoryImpl.searchEventsByCategory(eventCategory, page, size);
    }

    // 🔍 Search events by date range with pagination
    public List<Event> searchEventsByDateRange(Date startTime, Date endTime, int page, int size) {
        return eventRepositoryImpl.searchEventsByDateRange(startTime, endTime, page, size);
    }

    // 🔍 Search events by title or description with pagination
    public List<Event> searchEventsByTitleOrDescription(String queryText, int page, int size) {
        return eventRepositoryImpl.searchEventsByTitleOrDescription(queryText, page, size);
    }

    // 🔍 Search events by minimum rating with pagination
    public List<Event> searchEventsByRating(double minRating, int page, int size) {
        return eventRepositoryImpl.searchEventsByRating(minRating, page, size);
    }

    // 🔍 Search events with available seats with pagination
    public List<Event> searchEventsWithAvailableSeats(int page, int size) {
        return eventRepositoryImpl.searchEventsWithAvailableSeats(page, size);
    }

    // 🔍 Search events sorted by date, rating, or price with pagination
    public List<Event> searchEventsSorted(String sortBy, boolean ascending, int page, int size) {
        return eventRepositoryImpl.searchEventsSorted(sortBy, ascending, page, size);
    }
}
