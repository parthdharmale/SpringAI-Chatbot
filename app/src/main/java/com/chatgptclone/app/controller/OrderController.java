package com.chatgptclone.app.controller;

import com.chatgptclone.app.entity.*;
import com.chatgptclone.app.repository.OrderRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
	private final OrderRepository orderRepository;
	
	public OrderController(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}
	
	@PostMapping("/addOrder")
	public CustomerOrder createOrder(@RequestBody CustomerOrder order) {
		return orderRepository.save(order);
	}
}
