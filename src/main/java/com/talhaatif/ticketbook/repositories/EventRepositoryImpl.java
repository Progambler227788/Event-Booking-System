package com.talhaatif.ticketbook.repositories;

import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.events.TrendingEvent;
import com.talhaatif.ticketbook.services.RedisService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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

    @Autowired
    private RedisService redisService;

    private static final String TRENDING_EVENTS_KEY = "top5"; // Redis key

    // Run updateTrendingEvents automatically at startup
    @PostConstruct
    public void init() {
        System.out.println("🚀 Application started. Running initial update of trending events...");
        updateTrendingEvents();
    }



    public List<TrendingEvent> getTrendingEvents() {
        // Step 1: Try fetching from Redis
        List<TrendingEvent> cachedEvents = redisService.get(TRENDING_EVENTS_KEY, List.class);

        if (cachedEvents != null) {
            System.out.println("✅ Fetched from Redis cache!");
            return cachedEvents;
        }

        // Step 2: If not found in Redis, fetch from MongoDB
        List<TrendingEvent> dbEvents = mongoTemplate.findAll(TrendingEvent.class);

        if (!dbEvents.isEmpty()) {
            redisService.set(TRENDING_EVENTS_KEY, dbEvents, 300L); // Store in Redis (TTL: 5 mins)
        }

        return dbEvents;
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void updateTrendingEvents() {
        System.out.println("🚀 Updating trending events...");

        // Step 1: Fetch from MongoDB
        List<Event> trendingEvents = mongoTemplate.find(
                new Query().with(Sort.by(Sort.Direction.DESC, "totalBookedSeats")).limit(5),
                Event.class
        );

        // Step 2: Clear old records in MongoDB
        mongoTemplate.remove(new Query(), TrendingEvent.class);

        // Step 3: Convert & store in MongoDB
        List<TrendingEvent> trendingEventList = trendingEvents.stream()
                .map(TrendingEvent::new)
                .collect(Collectors.toList());
        mongoTemplate.insert(trendingEventList, TrendingEvent.class);

        // Step 4: Store in Redis for fast access
        redisService.set(TRENDING_EVENTS_KEY, trendingEventList, 300L); // Cache for 5 minutes

        System.out.println("✅ Trending events updated successfully in DB & Redis!");
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