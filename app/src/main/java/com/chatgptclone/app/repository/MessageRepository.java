package com.chatgptclone.app.repository;

import com.chatgptclone.app.entity.MessageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<MessageRecord, Long>{
	
	List<MessageRecord> findBySessionIdOrderByTimestampAsc(String sessionId);
}
