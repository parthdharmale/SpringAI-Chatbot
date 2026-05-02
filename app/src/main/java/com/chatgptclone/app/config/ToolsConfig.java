package com.chatgptclone.app.config;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import com.chatgptclone.app.entity.CustomerOrder;
import com.chatgptclone.app.event.OrderCancelledEvent;
import com.chatgptclone.app.repository.OrderRepository;


@Configuration
public class ToolsConfig {
	public record OrderRequest(String orderId, String ownerId) {}
	public record OrderStatus(String status, String deliveryDate) {}
	
	public record CancelOrderRequest(String orderId, String ownerId) {}
	public record CancelOrderResponse(String message, String previousStatus) {}
	
	public record PolicyRequest(String topic, String region) {}
	public record PolicyResponse(String rules) {}
	
	@Bean
	@Description("Get the status of customer's order using their Order ID. You MUST pass the current user's ID as the ownerId. ALWAYS use this tool FIRST to verify the order exists and belongs to the user before doing anything else.")
	public Function<OrderRequest, OrderStatus> orderStatusTool(OrderRepository repository){
		return request -> {
			System.out.println("AI Triggered Tool: Checking order " + request.orderId());
			
			return repository.findByIdAndOwnerId(request.orderId(), request.ownerId())
					.map(order -> new OrderStatus(order.getStatus(), order.getDeliveryDate()))
					.orElse(new OrderStatus("Access Denied of Not Found", "N/A"));
		};
	}
	
	@Bean
	@Description("Cancel a customer's order using their Order ID and Owner ID. You MUST pass the current user's ID as the owner ID")
	public Function<CancelOrderRequest, CancelOrderResponse> cancelOrderTool(OrderRepository repository, ApplicationEventPublisher eventPublisher){
		return request->{
			System.out.println("🛡️ SECURITY CHECK: User " + request.ownerId() + " attempting to cancel order " + request.orderId());
			
			java.util.Optional<CustomerOrder> optionalOrder = repository.findByIdAndOwnerId(request.orderId(), request.ownerId());

            if (optionalOrder.isPresent()) {
                CustomerOrder order = optionalOrder.get();
                String prevStatus = order.getStatus();
                
                order.setStatus("Cancelled");
                repository.save(order);
                
                eventPublisher.publishEvent(new OrderCancelledEvent(order.getId(), request.ownerId(), prevStatus));
                System.out.println("Event Published: Order cancellation broadcasted for order: " + order.getId());
                
                return new CancelOrderResponse("Order successfully cancelled.", prevStatus);
            } else {
                System.out.println("⛔ ACCESS DENIED: Order not found or user does not own this order.");
                return new CancelOrderResponse("Access Denied. Order not found or you do not have permission to cancel it.", "N/A");
            }
		};
	}
	
	@Bean
	@Description("Search the company policy rules. You MUST pass the user's region (e.g., 'US' or 'IN') along with the topic.")
	public Function<PolicyRequest, PolicyResponse> policySearchTool(VectorStore vectorStore){
		return request->{
			System.out.println("🔍 RAG FILTER: Searching policy for topic '" + request.topic()+ "' restricted to region: " + request.region());
			
			String policyRules = vectorStore.similaritySearch(
					SearchRequest.builder()
						.query(request.topic())
						.filterExpression("region == '" + request.region() + "'")
						.build()
					)
					.stream()
					.map(Document::getText)
					.collect(Collectors.joining("\n"));
			
			return new PolicyResponse(policyRules);
		};
	}
}
