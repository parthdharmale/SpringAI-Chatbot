package com.chatgptclone.app.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
	private final ChatClient chatClient;
	private final PersistentChatMemory persistentChatMemory;
	
	public ChatService(ChatClient.Builder builder, PersistentChatMemory persistentChatMemory) {
		this.chatClient = builder.build();
		this.persistentChatMemory = persistentChatMemory;
	}
	
	public String getResponse(String sessionId, String userPrompt) {
	        
	        // Simulating a database lookup: User A lives in the US, User B lives in India
	        String region = sessionId.equals("userA") ? "US" : "IN";
	        
	        String systemPrompt = """
	            You are a strict, secure enterprise support agent. You are currently speaking with the user whose ID is: %s.
	            This user is located in the %s region.
	            
	            When a user asks to cancel or modify an order, you MUST follow this strict chronological workflow:
	            1. VERIFY: First, use the orderStatusTool to ensure the order belongs to this specific user.
	            2. FAIL-FAST: If the order status returns 'Access Denied', immediately refuse the request.
	            3. CHECK RULES: If the order belongs to the user, use the policySearchTool. You MUST pass the user's region (%s) to the tool.
	            4. EXECUTE: If the regional policy allows it, use the cancelOrderTool. If the policy denies it, explain why based on their region.
	            """.formatted(sessionId, region, region);
	
	        return this.chatClient.prompt()
	                .system(systemPrompt)
	                .user(userPrompt)
	                .advisors(MessageChatMemoryAdvisor.builder(persistentChatMemory)
	                        .conversationId(sessionId)
	                        .build())
	                .toolNames("orderStatusTool", "cancelOrderTool", "policySearchTool")
	                .call()
	                .content();
	    }
	}
