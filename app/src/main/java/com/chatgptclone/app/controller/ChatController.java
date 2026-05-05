package com.chatgptclone.app.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatgptclone.app.service.ChatService;
import com.chatgptclone.app.service.RateLimitingService;

import io.github.bucket4j.Bucket;
import reactor.core.publisher.Flux;


@RestController
@CrossOrigin(origins = "*")
public class ChatController {
	private final ChatService chatService;
	private final RateLimitingService rateLimitingService;
	
	public ChatController(ChatService chatService, RateLimitingService rateLimitingService) {
		this.chatService = chatService;
		this.rateLimitingService = rateLimitingService;
	}
	
	@GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, String>> streamChatWithAI(@RequestParam String sessionId, @RequestParam String message) {
        
        Bucket userBucket = rateLimitingService.resolveBucket(sessionId);

        if (userBucket.tryConsume(1)) {
            // Wrap the raw string token in a JSON object!
            return chatService.getStreamResponse(sessionId, message)
                    .map(token -> Map.of("token", token));
        } else {
            System.out.println("⛔ RATE LIMIT EXCEEDED: Blocking request for user: " + sessionId);
            return Flux.just(Map.of("token", "🛑 [SYSTEM] Rate limit exceeded. Please wait 60 seconds."));
        }
    }
}
