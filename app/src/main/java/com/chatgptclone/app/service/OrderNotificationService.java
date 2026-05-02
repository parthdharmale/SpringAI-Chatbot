package com.chatgptclone.app.service;

import com.chatgptclone.app.event.OrderCancelledEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationService {
	
	@Async
	@EventListener
	public void sendRefundEmail(OrderCancelledEvent event) {
		try {
			Thread.sleep(3000);
			System.out.println("ASYNC WORKER 1: Refund confirmation email sent to user: " + event.ownerId());
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	@Async
    @EventListener
    public void notifyWarehouse(OrderCancelledEvent event) {
        try {
            Thread.sleep(2000);
            System.out.println("🏭 ASYNC WORKER 2: Instructed warehouse to halt shipping for order: " + event.orderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
