package com.chatgptclone.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MessageRecord {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String sessionId;
	private String messageType;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	private LocalDateTime timestamp;
	
	public MessageRecord() {}
	
	public MessageRecord(String sessionId, String messageType, String content) {
		this.sessionId = sessionId;
		this.messageType = messageType;
		this.content = content;
		this.timestamp = LocalDateTime.now();
	}
	
	public Long getId() {return id;}
	public String getSessionId() {return sessionId;}
	public String getMessageType() {return messageType;}
	public String getContent() {return content;}
	public LocalDateTime getTimestamp() {return timestamp;}
}
