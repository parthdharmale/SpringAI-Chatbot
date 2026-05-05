package com.chatgptclone.app.service;

import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChatService {
	private final ChatClient chatClient;
	private final PersistentChatMemory persistentChatMemory;
	private final SemanticCacheService semanticCacheService;
	
	public ChatService(ChatClient.Builder builder, PersistentChatMemory persistentChatMemory, SemanticCacheService semanticCacheService) {
		this.chatClient = builder.build();
		this.persistentChatMemory = persistentChatMemory;
		this.semanticCacheService = semanticCacheService;
	}
	
public Flux<String> getStreamResponse(String sessionId, String userPrompt) {
        
        String region = sessionId.equals("userA") ? "US" : "IN";

        // 1. CACHE ROUTER: Define what is safe to cache.
        // If the prompt contains any digits (Order IDs) or transactional verbs, it is NOT safe to cache.
        boolean isTransactional = userPrompt.matches(".*\\d+.*") || 
                                  userPrompt.toLowerCase().contains("cancel") || 
                                  userPrompt.toLowerCase().contains("status");

        // 2. Only check the cache if it's a general knowledge/policy question
        if (!isTransactional) {
            Optional<String> cachedResponse = semanticCacheService.checkCache(userPrompt, region);
            if (cachedResponse.isPresent()) {
                return Flux.just("[CACHED] " + cachedResponse.get());
            }
        } else {
            System.out.println("🚦 ROUTER: Transactional request detected. Bypassing cache to execute tools.");
        }
        
        String systemPrompt = """
            You are a strict, secure enterprise support agent. You are currently speaking with the user whose ID is: %s.
            This user is located in the %s region.
            
            When a user asks to cancel or modify an order, you MUST follow this strict chronological workflow:
            1. VERIFY: First, use the orderStatusTool to ensure the order belongs to this specific user.
            2. FAIL-FAST: If the order status returns 'Access Denied', immediately refuse the request.
            3. CHECK RULES: If the order belongs to the user, use the policySearchTool. You MUST pass the user's region (%s) to the tool.
            4. EXECUTE: If the regional policy allows it, use the cancelOrderTool. If the policy denies it, explain why based on their region.
            """.formatted(sessionId, region, region);

        Flux<String> aiStream = this.chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .advisors(MessageChatMemoryAdvisor.builder(persistentChatMemory)
                        .conversationId(sessionId)
                        .build())
                .toolNames("orderStatusTool", "cancelOrderTool", "policySearchTool")
                .stream()
                .content();

        StringBuilder fullResponseBuilder = new StringBuilder();
        
        return aiStream
                .doOnNext(token -> fullResponseBuilder.append(token))
                .doOnComplete(() -> { 
                    // 3. Only save to cache if it wasn't a transactional request!
                    if (!isTransactional) {
                        semanticCacheService.saveToCache(userPrompt, region, fullResponseBuilder.toString());
                    }
                });
    }
}
    
