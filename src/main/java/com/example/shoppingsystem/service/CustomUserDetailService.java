package com.example.shoppingsystem.service;

import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.shoppingsystem.entity.User;
import com.example.shoppingsystem.repository.UserRepository;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String[] roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .map(CustomUserDetailService::normalizeRoleName)
                .toArray(String[]::new);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(roleNames)
                .build();
    }

    /** DB should store ADMIN/USER; Spring Security expects ROLE_ADMIN via .roles() */
    private static String normalizeRoleName(String name) {
        if (name == null || name.isBlank()) {
            return "USER";
        }
        if (name.startsWith("ROLE_")) {
            return name.substring(5);
        }
        return name;
    }
}
