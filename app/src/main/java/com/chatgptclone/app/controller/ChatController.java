package com.chatgptclone.app.controller;

import com.chatgptclone.app.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {
	private final ChatService chatService;
	
	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}
	
	@GetMapping("/chat")
    public String chatWithAI(@RequestParam String sessionId, @RequestParam String message) {
        // Pass both the sessionId and the message to the AI
        return chatService.getResponse(sessionId, message);
    }
}
