package com.example.shoppingsystem.service;

import com.example.shoppingsystem.entity.Order;
import com.example.shoppingsystem.entity.Product;
import com.example.shoppingsystem.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;

//    public Order placeOrder(Order order){
//        return orderRepository.save(order);
//    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    @Transactional
    public Order placeOrder(Order order) {

        Product product = productService.getProductById(order.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int requestedQty = order.getQuantity();

        // Not enough stock
        if (product.getStock() < requestedQty) {
            throw new RuntimeException("Not enough stock available");
        }

        // Reduce stock
        product.setStock(product.getStock() - requestedQty);
        productService.saveProduct(product);

        order.setProduct(product);

        return orderRepository.save(order);
    }
}
