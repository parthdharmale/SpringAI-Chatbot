package com.chatgptclone.app.entity;
import jakarta.persistence.*;

@Entity
public class CustomerOrder {
	@Id
	private String id;
	private String status;
	private String deliveryDate;
	private String ownerId;
	
	public CustomerOrder() {}
	
	public CustomerOrder(String id, String status, String deliveryDate) {
		this.id = id;
		this.status = status;
		this.deliveryDate = deliveryDate;
	}
	
	public String getId() {return id;}
	public String getStatus() {return status;}
	public String getDeliveryDate() {return deliveryDate;}
	
	public void setStatus(String status) {
		this.status = status;
	}
	public void setDeliveryDate(String delivery) {
		this.deliveryDate = delivery;
	}
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
}
