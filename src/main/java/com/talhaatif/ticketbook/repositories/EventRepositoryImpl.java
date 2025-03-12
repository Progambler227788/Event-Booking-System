package com.talhaatif.ticketbook.repositories;

import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.events.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.List;

public class EventRepositoryImpl {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private  EventRepository eventRepository;

    public List<Event> searchEventsByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("location").regex(location, "i")); // Case-insensitive regex
        return mongoTemplate.find(query, Event.class);
    }

    public List<Event> searchEventsByCategory(String category) {
        Query query = new Query();
        query.addCriteria(Criteria.where("category").is(Category.valueOf(category.toUpperCase())));
        return mongoTemplate.find(query, Event.class);
    }

    public List<Event> searchEventsByDateRange(
           Date startTime, Date endTime) {

        Query query = new Query();
        query.addCriteria(Criteria.where("dateTime").gte(startTime).lte(endTime));
        return mongoTemplate.find(query, Event.class);
    }
}
