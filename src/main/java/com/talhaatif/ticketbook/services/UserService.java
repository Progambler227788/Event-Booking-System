package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.repositories.UserRepository;
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

   // for Encrypting user password
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveNewUser(User object){
        try {
            object.setPassword(passwordEncoder.encode(object.getPassword()));
            object.setRole(Arrays.asList("USER"));
            userRepository.save(object);
        }

        catch (Exception e){
            // by default print --> error, warn, info
            // for debug and trace we need customization
            // if we use sl4j annotation then use log object
//            logger.info("Error occurred in saving user with user-name --> {}", object.getUserName(), e);
            log.info("Error occurred in saving user with user-name --> {}", object.getUsername(), e);
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


    public List<User> findUsers()
    {
        return userRepository.findAll();
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


}
