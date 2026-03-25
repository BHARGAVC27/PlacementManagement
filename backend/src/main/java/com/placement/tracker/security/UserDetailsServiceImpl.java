package com.placement.tracker.security;

import com.placement.tracker.model.entity.User;
import com.placement.tracker.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

// Loads user details from DB so Spring Security can authenticate requests.
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Finds user by email and returns Spring Security UserDetails with role authority.
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        String authority = "ROLE_" + user.getRole().name();
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
