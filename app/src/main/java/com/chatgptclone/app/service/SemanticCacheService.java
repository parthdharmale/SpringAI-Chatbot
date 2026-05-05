package com.chatgptclone.app.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SemanticCacheService {
	private final VectorStore vectorStore;
	
	public SemanticCacheService(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}
	
	public Optional<String> checkCache(String userPrompt, String region){
		System.out.println("🧠 CACHE: Checking for mathematically similar previous questions...");
		
		List<Document> similarQuestions = vectorStore.similaritySearch(
				SearchRequest.builder()
				.query(userPrompt)
				.topK(1)
				.similarityThreshold(0.75)
				.filterExpression("type == 'cache' AND region == '"+ region + "'")
				.build()
		);
		
		if(!similarQuestions.isEmpty()) {
			String cachedAnswer = similarQuestions.get(0).getMetadata().get("answer").toString();
			System.out.println("⚡ CACHE HIT: Bypassing LLM. Returning cached response.");
			return Optional.of(cachedAnswer);
		}
		
		System.out.println("🐌 CACHE MISS: No similar questions found. Routing to OpenAI...");
		return Optional.empty();
	}
	
	public void saveToCache(String userPrompt, String region, String aiResponse) {
        Document cacheRecord = new Document(
                userPrompt,
                Map.of(
                        "type", "cache",
                        "region", region,
                        "answer", aiResponse
                )
        );
        
        vectorStore.add(List.of(cacheRecord));
        System.out.println("💾 CACHE SAVED: Stored Q&A pair in Postgres for future use.");
    }
}
