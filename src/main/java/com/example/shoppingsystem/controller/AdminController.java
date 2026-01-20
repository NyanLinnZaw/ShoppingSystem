package com.example.shoppingsystem.controller;

import com.example.shoppingsystem.entity.Order;
import com.example.shoppingsystem.entity.User;
import com.example.shoppingsystem.repository.RoleRepository;
import com.example.shoppingsystem.repository.UserRepository;
import com.example.shoppingsystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public User createAdmin(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(roleRepo.findByName("ADMIN")));
        return userRepo.save(user);
    }

}
