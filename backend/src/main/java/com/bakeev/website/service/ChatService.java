package com.bakeev.website.service;

import com.bakeev.website.entity.ChatMessage;
import com.bakeev.website.repository.ChatMessageRepository;
import com.bakeev.website.utils.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> getChatHistory(String user1, String user2) {
        return chatMessageRepository.findMessagesBetweenUsers(user1, user2);
    }

    public MessageDto toDto(ChatMessage message) {
        MessageDto dto = new MessageDto();
        dto.setSender(message.getSender());
        dto.setReceiver(message.getReceiver());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());

        if (message.getFiles() != null && !message.getFiles().isEmpty()) {
            dto.setAttachmentId(message.getFiles().get(0).getId());
            dto.setAttachmentName(message.getFiles().get(0).getOriginalName());
        }
        return dto;
    }

    public void deleteMessage(Long id) {
        chatMessageRepository.deleteChatMessageById(id);
    }
}
