package com.chatgptclone.app.service;

import com.chatgptclone.app.entity.MessageRecord;
import com.chatgptclone.app.repository.MessageRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersistentChatMemory implements ChatMemory {

    private final MessageRepository repository;

    public PersistentChatMemory(MessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        for (Message msg : messages) {
            String type = msg instanceof UserMessage ? "USER" : "ASSISTANT";
            MessageRecord record = new MessageRecord(conversationId, type, msg.getText());
            repository.save(record);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        List<MessageRecord> records = repository.findBySessionIdOrderByTimestampAsc(conversationId);
        return records.stream().map(record -> {
            if ("USER".equals(record.getMessageType())) {
                return new UserMessage(record.getContent());
            } else {
                return new AssistantMessage(record.getContent());
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        // Optional: We can leave this empty for now so we never lose history!
    }
}