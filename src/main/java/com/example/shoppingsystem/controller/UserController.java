package com.example.shoppingsystem.controller;

import com.example.shoppingsystem.entity.Order;
import com.example.shoppingsystem.entity.User;
import com.example.shoppingsystem.repository.UserRepository;
import com.example.shoppingsystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/order")
    public Order placeOrder(
            @RequestBody Order order,
            Authentication authentication) {

        String username = authentication.getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        order.setUser(user);

        return orderService.placeOrder(order);
    }

//    @PostMapping("/order")
//    public Order placeOrder(@RequestBody Order order) {
//        return orderService.placeOrder(order);
//    }
}
