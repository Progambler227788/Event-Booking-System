package com.talhaatif.ticketbook.repositories;

import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.TrendingEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class EventRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    // Run updateTrendingEvents automatically at startup
    @PostConstruct
    public void init() {
        System.out.println("🚀 Application started. Running initial update of trending events...");
        updateTrendingEvents();
    }

    @Cacheable(value = "trendingEvents", key = "'top15'")
    public List<TrendingEvent> getTrendingEvents() {
        return mongoTemplate.findAll(TrendingEvent.class);
    }

    // Scheduled task to update trending events every 5 minutes
    @Scheduled(fixedRate = 3000) // Every 5 minutes
    public void updateTrendingEvents() {
        // Fetch top 15 trending events from the events collection
        List<Event> trendingEvents = mongoTemplate.find(
                new Query().with(Sort.by(Sort.Direction.DESC, "totalBookedSeats")).limit(15),
                Event.class
        );

        // Clear old trending events
        mongoTemplate.remove(new Query(), TrendingEvent.class);

        // Convert Event objects to TrendingEvent objects and save them
        List<TrendingEvent> trendingEventList = trendingEvents.stream()
                .map(TrendingEvent::new)
                .collect(Collectors.toList());

        mongoTemplate.insert(trendingEventList, TrendingEvent.class);

        System.out.println("✅ Trending events updated successfully!");
    }

    // 🔍 Search events by location (case-insensitive regex) with pagination
    public List<Event> searchEventsByLocation(String location, int page, int size) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("location").regex(location, "i")); // Case-insensitive regex
        query.with(PageRequest.of(page, size)); // Add pagination
        return mongoTemplate.find(query, Event.class);
    }

    // 🔍 Search events by category with pagination
    public List<Event> searchEventsByCategory(Category category, int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("category").is(category));
        query.with(PageRequest.of(page, size)); // Add pagination
        return mongoTemplate.find(query, Event.class);
    }

    // 🔍 Search events by date range with pagination
    public List<Event> searchEventsByDateRange(Date startTime, Date endTime, int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("dateTime").gte(startTime).lte(endTime));
        query.with(PageRequest.of(page, size)); // Add pagination
        return mongoTemplate.find(query, Event.class);
    }

    // 🔍 Search events by title or description (fuzzy search) with pagination
    public List<Event> searchEventsByTitleOrDescription(String queryText, int page, int size) {
        Query query = new Query();
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("title").regex(queryText, "i"), // Case-insensitive regex
                Criteria.where("description").regex(queryText, "i")
        );
        query.addCriteria(criteria);
        query.with(PageRequest.of(page, size)); // Add pagination
        return mongoTemplate.find(query, Event.class);
    }

    // 🔍 Search events by minimum rating with pagination
    public List<Event> searchEventsByRating(double minRating, int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("rating").gte(minRating));
        query.with(PageRequest.of(page, size)); // Add pagination
        return mongoTemplate.find(query, Event.class);
    }

    // 🔍 Search events with available seats with pagination
    public List<Event> searchEventsWithAvailableSeats(int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("seats.isAvailable").is(true));
        query.with(PageRequest.of(page, size)); // Add pagination
        return mongoTemplate.find(query, Event.class);
    }

    // 🔍 Search events sorted by date, rating, or price with pagination
    public List<Event> searchEventsSorted(String sortBy, boolean ascending, int page, int size) {
        Query query = new Query();
        query.with(Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy)); // Add sorting
        query.with(PageRequest.of(page, size)); // Add pagination
        return mongoTemplate.find(query, Event.class);
    }


    // With Pagination and Sizing
}