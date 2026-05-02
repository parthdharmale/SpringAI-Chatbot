package com.chatgptclone.app.repository;

import com.chatgptclone.app.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder,String>{
	// Spring data JPA write SQL queries automatically.
	
	Optional<CustomerOrder> findByIdAndOwnerId(String id, String ownerId);
}
