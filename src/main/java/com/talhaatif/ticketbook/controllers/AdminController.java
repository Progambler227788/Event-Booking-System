package com.talhaatif.ticketbook.controllers;

import com.talhaatif.ticketbook.entities.events.Category;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.services.EventService;
import com.talhaatif.ticketbook.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") //

// @RequestMapping is an annotation in Spring Boot
// that is used to map HTTP requests to specific controller classes or methods.
public class AdminController {
    private final EventService eventService;
    private final UserService userService;

    // ✅ Create New Event (Only Admins)
    @PostMapping("/events/addEvent")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    // 🔎 Search Events by Category
    @GetMapping("/events/category/{category}")
    public ResponseEntity<List<Event>> getEventsByCategory(@PathVariable Category category) {
        return ResponseEntity.ok(eventService.getEventsByCategory(category));
    }


    // 🔎 Get all events
    @GetMapping("/events/getAllEvents")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // Get all users
    @GetMapping("/users/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    // ❌ Delete Event by ID
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}

