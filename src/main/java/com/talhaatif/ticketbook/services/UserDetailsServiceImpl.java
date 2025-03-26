package com.talhaatif.ticketbook.services;

import com.talhaatif.ticketbook.entities.user.User;
import com.talhaatif.ticketbook.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


// I added these comments so that I don't forget my codebase XD :)
// UserDetailsServiceImpl is only responsible for fetching authentication-related user details.
// UserDetailsService is a Spring Security interface that provides a method loadUserByUsername(String username)


@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUserName(username);
        if (u != null) {
            List<GrantedAuthority> authorities = u.getRole().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))  // Ensure prefix "ROLE_"
                    .collect(Collectors.toList());

            return org.springframework.security.core.userdetails.User.builder()
                    .username(u.getUsername())
                    .password(u.getPassword())
                    .authorities(authorities)
                    .build();
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }


}
