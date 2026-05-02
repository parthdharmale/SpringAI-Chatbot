package com.chatgptclone.app.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class RagConfig {

    @Bean
    public ApplicationRunner loadPolicyData(VectorStore vectorStore) {
        return args -> {
            List<Document> existing = vectorStore.similaritySearch(
            		SearchRequest.builder().query("cancellation").filterExpression("region == 'US'").build()
            		);
            
            if(existing.isEmpty()) {
            	System.out.println("📚 RAG SYSTEM: Loading Regional Policies into Vector Database...");
            	
            	Document usPolicy = new Document(
            			"US REGION POLICY: Customers in the US can cancel orders at any time while the status is 'Processing'. Refunds take 2 days.",
            			Map.of("region","US")
            			);
            	
            	Document indiaPolicy = new Document(
                        "INDIA REGION POLICY: Customers in India CANNOT cancel orders once they reach 'Processing' status. They can only cancel if 'Pending'.",
                        Map.of("region", "IN")
                    );
            	
            	vectorStore.add(List.of(usPolicy, indiaPolicy));
                System.out.println("📚 RAG SYSTEM: Regional Policies permanently saved with metadata!");
            } else {
            	System.out.println("📚 RAG SYSTEM: Regional policies already exist. Skipping upload.");
            }
            
            
        };
    }
}