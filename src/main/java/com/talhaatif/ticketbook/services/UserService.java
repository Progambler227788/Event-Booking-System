package com.talhaatif.ticketbook.services;

import com.stripe.exception.StripeException;
import com.talhaatif.ticketbook.dto.StripeIntentResponse;
import com.talhaatif.ticketbook.dto.UpdateRequest;
import com.talhaatif.ticketbook.dto.UserBalance;
import com.talhaatif.ticketbook.dto.UserInformation;
import com.talhaatif.ticketbook.entities.bookings.Booking;
import com.talhaatif.ticketbook.entities.bookings.BookingStatus;
import com.talhaatif.ticketbook.entities.events.Event;
import com.talhaatif.ticketbook.entities.user.Wallet;
import com.talhaatif.ticketbook.repositories.UserRepository;
import com.talhaatif.ticketbook.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.talhaatif.ticketbook.entities.user.User;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private PaymentService paymentService;

   // for Encrypting user password
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    //--------------- Ticket Section

    // For wallet payments
    public Booking bookWithWallet(String userId, String eventId, List<String> seatNumbers) {
        return bookingService.bookWithWallet(userId, eventId, seatNumbers);
    }

    // For Stripe payments - Step 1: Create payment intent
    public StripeIntentResponse createStripePaymentIntent(String userId, String eventId,
                                                          List<String> seatNumbers) {
        // First verify seat availability
        // First verify seat availability and calculate amount
        Event event = bookingService.verifySeatAvailability(eventId, seatNumbers);
        double amount = bookingService.calculateTotal(event, seatNumbers);

        // Then create payment intent
        return paymentService.createPaymentIntent(amount, "usd", userId);
    }

    // For Stripe payments - Step 2: Confirm booking after payment
    public Booking confirmStripeBooking(String userId, String paymentIntentId,
                                        String eventId, List<String> seatNumbers)  {
        return bookingService.createBookingAfterStripePayment(
                userId,
                eventId,
                seatNumbers,
                paymentService.confirmPayment(paymentIntentId).getAmount() / 100.0
        );
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


    public void deleteAllUsers() {
         userRepository.deleteAll();
    }

    public String updateUserProfile(String userId, UpdateRequest updateRequest) {
        User existingUser = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean credentialsChanged = false;


        if (StringUtils.hasText(updateRequest.getUserName())) {
            existingUser.setUserName(updateRequest.getUserName());
            credentialsChanged = true;
        }
        if (StringUtils.hasText(updateRequest.getEmail())) {
            existingUser.setEmail(updateRequest.getEmail());
            credentialsChanged = true;
        }
        if (StringUtils.hasText(updateRequest.getPhoneNumber())) {
            existingUser.setPhoneNumber(updateRequest.getPhoneNumber());
        }

        if (StringUtils.hasText(updateRequest.getLocation())) {
            existingUser.setLocation(updateRequest.getLocation());
        }

        userRepository.save(existingUser);

        return credentialsChanged
                ? jwtUtil.generateToken(existingUser.getUsername(), existingUser.getRole().get(0))
                : "";
    }


    public UserInformation getUserById(ObjectId id){

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()){
            UserInformation userInformation = new UserInformation();
            userInformation.setId(user.get().getId());
            userInformation.setUserName(user.get().getUsername());
            userInformation.setEmail(user.get().getEmail());
            userInformation.setPhoneNumber(user.get().getPhoneNumber());
           userInformation.setWallet(user.get().getWallet());
            userInformation.setLocation(user.get().getLocation());
            return userInformation;

        }
        // no user found
        return null;
    }


    public User findByUserName(String userName){
        return userRepository.findByUserName(userName);
    }

    public String getUserIdByUserName(String userName) {
        User user = userRepository.findByUserName(userName);
        return user != null ? user.getId() : null;
    }

    public void deleteUserById(ObjectId id){
        userRepository.deleteById(id);
    }

    public void deleteUserByName(String username){
        userRepository.deleteByUserName(username);
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

    public void updateLocation(String userId, String location) {
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String userLocation = user.getLocation();
        // if currency is not same then save else do nothing
        if ( !userLocation.equalsIgnoreCase(location) ) {
            user.setLocation(location);
            userRepository.save(user);
        }

    }



}

/* StringUtils is a utility class provided by Spring Framework under
org.springframework.util.StringUtils. It helps with common string operations,
such as checking if a string is empty, trimming whitespace, and more.*/


/*Why Use StringUtils.hasText() Instead of != null && !isEmpty()?
Using StringUtils.hasText() is better than manually checking for null and isEmpty() because:

It automatically trims the string before checking.
It returns false for whitespace-only strings, which " ".isEmpty() does not.
It makes code cleaner & more readable.

 */
