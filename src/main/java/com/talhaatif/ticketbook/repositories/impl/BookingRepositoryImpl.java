package com.talhaatif.ticketbook.repositories.impl;

import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BookingRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private EventRepository eventRepository;
    // filter booking for a specific user id

    public List<Booking> filterBookingsByUserId(Integer month, Integer year, BookingStatus status, int page, int size, String userId) {
        Criteria criteria = new Criteria();

        // 📅 Filter by Month & Year
        if (month != null && year != null) {
            Calendar startDate = Calendar.getInstance();
            // month -1 due to zero based and year is not zero based
            // starting date of that month starting from 12:00 am so 0:00 am
            startDate.set(year, month - 1, 1, 0, 0, 0);
            Date startOfMonth = startDate.getTime();

            Calendar endDate = Calendar.getInstance();
            // ending date of that month tarting from 11:00 pm so 23:59 pm
            endDate.set(year, month - 1, startDate.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            Date endOfMonth = endDate.getTime();

            criteria.and("createdAt").gte(startOfMonth).lte(endOfMonth);
        }


        // ✅ Filter by Booking Status
        if (status != null) {
            criteria.and("status").is(status);
        }

        if (userId!=null){
            criteria.and("userId").is(userId);
        }

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createdAt")) // 📌 Sort by latest bookings
                .skip(page * size) // 📌 Pagination
                .limit(size);
        return mongoTemplate.find(query, Booking.class);
    }

    // provides abstraction to interact with database
    public List<Booking> filterBookings(Integer month, Integer year, BookingStatus status) {
        Criteria criteria = new Criteria();

        // 📅 Filter by Month & Year
        if (month != null && year != null) {
            Calendar startDate = Calendar.getInstance();
            // month -1 due to zero based and year is not zero based
            // starting date of that month starting from 12:00 am so 0:00 am
            startDate.set(year, month - 1, 1, 0, 0, 0);
            Date startOfMonth = startDate.getTime();

            Calendar endDate = Calendar.getInstance();
            // ending date of that month tarting from 11:00 pm so 23:59 pm
            endDate.set(year, month - 1, startDate.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            Date endOfMonth = endDate.getTime();

            criteria.and("createdAt").gte(startOfMonth).lte(endOfMonth);
        }


        // ✅ Filter by Booking Status
        if (status != null) {
            criteria.and("status").is(status);
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Booking.class);
    }



    public List<Booking> filterBookingsByPaging(Integer month, Integer year, BookingStatus status
    , int page, int size) {
        Criteria criteria = new Criteria();

        // 📅 Filter by Month & Year
        if (month != null && year != null) {
            Calendar startDate = Calendar.getInstance();
            startDate.set(year, month - 1, 1, 0, 0, 0);
            Date startOfMonth = startDate.getTime();

            Calendar endDate = Calendar.getInstance();
            endDate.set(year, month - 1, startDate.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            Date endOfMonth = endDate.getTime();

            criteria.and("createdAt").gte(startOfMonth).lte(endOfMonth);
        }


        // ✅ Filter by Booking Status
        if (status != null) {
            criteria.and("status").is(status);
        }

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createdAt")) // 📌 Sort by latest bookings
                .skip(page * size) // 📌 Pagination
                .limit(size);
        return mongoTemplate.find(query, Booking.class);
    }



}
