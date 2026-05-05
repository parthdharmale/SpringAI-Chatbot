package com.chatgptclone.app.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {
	
	private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
	
	public Bucket resolveBucket(String sessionId) {
		return userBuckets.computeIfAbsent(sessionId, this::createNewBucket);
	}
	
	private Bucket createNewBucket(String sessionId) {
		System.out.println("🛡️ RATE LIMITER: Creating new token bucket for user: " + sessionId);
		
		Bandwidth limit = Bandwidth.builder()
				.capacity(5)
				.refillIntervally(5,  Duration.ofMinutes(1))
				.build();
		
		return Bucket.builder().addLimit(limit).build();
	}
}
