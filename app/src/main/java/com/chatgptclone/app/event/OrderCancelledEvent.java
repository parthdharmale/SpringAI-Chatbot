package com.chatgptclone.app.event;

public record OrderCancelledEvent(String orderId, String ownerId, String previousStatus) {

}
