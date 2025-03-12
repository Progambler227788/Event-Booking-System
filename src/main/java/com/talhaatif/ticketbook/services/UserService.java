package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.dto.UserBalance;
import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.entities.user.Wallet;
import com.talhaatif.ticketbook.repositories.UserRepository;
import com.talhaatif.ticketbook.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.talhaatif.ticketbook.entities.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class UserService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JwtUtil jwtUtil;

   // for Encrypting user password
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();




    //--------------- Ticket Section
    public Booking bookTickets(String userId, String eventId, int totalTickets, String paymentMethod){


        return bookingService.bookSeats(userId, eventId, totalTickets,paymentMethod);

    }

    public void cancelBooking(String userId, String bookingId){
        Booking booking = bookingService.getBookingById(bookingId); // 🔥 If not found, exception is auto-handled globally

        if (!booking.getUserId().equals(userId)) {
            throw new SecurityException("You are not authorized to cancel this booking"); // 🔥 Auto-handled
        }

        bookingService.cancelBooking(bookingId);

    }

    public List<Booking> filterBookingsByUserId(Integer month, Integer year, BookingStatus status, int page, int size, String userId){
        return  bookingService.filterBookingsByUserId(month, year, status, page, size, userId);
    }


    //----------------- User section

    public void saveNewUser(User object){
        try {
            object.setPassword(passwordEncoder.encode(object.getPassword()));
            object.setRole(Arrays.asList("USER"));

            if (object.getWallet() == null) {
                object.setWallet(new Wallet(0.0, "USD"));  // Default values (change as needed)
            }
            userRepository.save(object);
        }

        catch (Exception e){
            // by default print --> error, warn, info
            // for debug and trace we need customization
            // if we use sl4j annotation then use log object
//            logger.info("Error occurred in saving user with user-name --> {}", object.getUserName(), e);
            log.error("Error occurred in saving user with user-name --> {}", object.getUsername(), e);
        }
    }

    // for creating admin
    public void saveAdminUser(User object){
        try {
            object.setPassword(passwordEncoder.encode(object.getPassword()));
            object.setRole(Arrays.asList("ADMIN"));
            userRepository.save(object);

        }

        catch (Exception e){
            // by default print --> error, warn, info
            // for debug and trace we need customization
            // if we use sl4j annotation then use log object

            log.info("Error occurred in saving user as an admin with user-name --> {}", object.getUsername(), e);
        }
    }


    public List<User> getAllUsers()
    {
        return userRepository.findAll();
    }

    public String updateUserProfile(String userId, User updatedUser) {
        User existingUser = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Only update fields that are not null (to avoid overwriting existing data)
        boolean credentialsChanged= false;
        if (updatedUser.getUsername() != null) {
            existingUser.setUserName(updatedUser.getUsername());
            credentialsChanged=true;
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
            credentialsChanged=true;
        }

        if (updatedUser.getPhoneNumber() != null)
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());



        userRepository.save(existingUser);

        // Regenerate new token

        if(credentialsChanged){
           return jwtUtil.generateToken(existingUser.getUsername(),existingUser.getRole().get(0));
        }
        return "";
    }


    public Optional<User> getUserById(ObjectId id){
        return  userRepository.findById(id);
    }

    public void deleteUserById(ObjectId id){
        userRepository.deleteById(id);
    }

    public void deleteUserByName(String username){
        userRepository.deleteByUserName(username);
    }

    public User findByUserName(String userName){
        return userRepository.findByUserName(userName);
    }

    public String getUserIdByUserName(String userName) {
        User user = userRepository.findByUserName(userName);
        return user != null ? user.getId() : null;
    }

    // ---------------Wallet section


    public UserBalance getBalance(String userId) {
        User user = userRepository.findById(new ObjectId(userId))  // Convert String to ObjectId
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserBalance object = new UserBalance();
        object.setBalance(user.getWallet().getBalance());
        object.setCurrencyType(user.getWallet().getCurrencyType());
        object.setUserName(user.getUsername());

        return object;

    }



    public void addBalance(String userId, double amount) {
        User user = userRepository.findById(new ObjectId(userId))  // Convert String to ObjectId
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getWallet() == null) {
            user.setWallet(new Wallet(0.0, "PKR"));
        }

        user.getWallet().setBalance(user.getWallet().getBalance() + amount);
        userRepository.save(user);
    }


    public void deductBalance(String userId, double amount) {
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getWallet().getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        user.getWallet().setBalance(user.getWallet().getBalance() - amount);
        userRepository.save(user);
    }

    public void updateCurrencyType(String userId, String currencyType) {
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getWallet() == null) {
            user.setWallet(new Wallet(0.0, currencyType));
        }
        // if currency is not same then save else do nothing
        if (!currencyType.equalsIgnoreCase(user.getWallet().getCurrencyType())) {
            user.getWallet().setCurrencyType(currencyType);
            userRepository.save(user);
        }

    }





}
