package com.talhaatif.ticketbook.repositories.impl;

import com.talhaatif.ticketbook.dto.SimplifiedTrendingEvent;
import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.TrendingEvent;
import com.talhaatif.ticketbook.services.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class EventRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisService redisService;

    private static final String TRENDING_EVENTS_KEY = "top5"; // Redis key

    private static final long CACHE_TTL_SECONDS = 300; // 5 minutes

    // Run updateTrendingEvents automatically at startup
    @PostConstruct
    public void init() {
        System.out.println("🚀 Application started. Running initial update of trending events...");
        updateTrendingEvents();
    }



    public List<SimplifiedTrendingEvent> getTrendingEvents() {
        // Try fetching from Redis with proper type handling
        List<TrendingEvent> cachedEvents = redisService.getList(TRENDING_EVENTS_KEY, TrendingEvent.class);

        if (cachedEvents != null && !cachedEvents.isEmpty()) {
            log.info("✅ Serving trending events from cache");
            return convertToSimplified(cachedEvents);
        }

        // Fallback to MongoDB
        List<TrendingEvent> dbEvents = mongoTemplate.findAll(TrendingEvent.class);

        if (!dbEvents.isEmpty()) {
            redisService.set(TRENDING_EVENTS_KEY, dbEvents, CACHE_TTL_SECONDS);
        }

        return convertToSimplified(dbEvents);
    }

    @Scheduled(fixedRate = 300000)
    public void updateTrendingEvents() {
        log.info("🔄 Updating trending events...");

        List<Event> trendingEvents = mongoTemplate.find(
                new Query()
                        .with(Sort.by(Sort.Direction.DESC, "totalBookedSeats"))
                        .limit(5),
                Event.class
        );

        // Atomic update operation
        mongoTemplate.remove(new Query(), TrendingEvent.class);
        List<TrendingEvent> trendingEventList = trendingEvents.stream()
                .map(this::convertToTrendingEvent)
                .collect(Collectors.toList());
        mongoTemplate.insert(trendingEventList, TrendingEvent.class);

        // Update cache
        redisService.set(TRENDING_EVENTS_KEY, trendingEventList, CACHE_TTL_SECONDS);
        log.info("✅ Updated {} trending events in DB & Redis", trendingEventList.size());
    }


    private TrendingEvent convertToTrendingEvent(Event event) {
        return TrendingEvent.builder()
                .id(event.getId())
                .title(event.getTitle())
                .imageUrl(event.getImageUrl())
                .location(event.getLocation())
                .dateTime(event.getDateTime())
                .totalSeats(event.getTotalSeats())
                .totalBookedSeats(event.getTotalBookedSeats())
                .build();
    }

    private List<SimplifiedTrendingEvent> convertToSimplified(List<TrendingEvent> events) {
        return events.stream()
                .map(this::convertToSimplified)
                .collect(Collectors.toList());
    }

    private SimplifiedTrendingEvent convertToSimplified(TrendingEvent event) {
        return SimplifiedTrendingEvent.builder()
                .id(event.getId())
                .title(event.getTitle())
                .imageUrl(event.getImageUrl())
                .location(event.getLocation())
                .dateTime(event.getDateTime())
                .totalSeats(event.getTotalSeats())
                .totalBookedSeats(event.getTotalBookedSeats())
                .build();
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