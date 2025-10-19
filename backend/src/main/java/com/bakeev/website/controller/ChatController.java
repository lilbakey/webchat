package com.bakeev.website.controller;

import com.bakeev.website.entity.ChatMessage;
import com.bakeev.website.entity.FileEntity;
import com.bakeev.website.repository.ChatMessageRepository;
import com.bakeev.website.service.ChatService;
import com.bakeev.website.service.FileService;
import com.bakeev.website.service.OnlineUserService;
import com.bakeev.website.utils.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;
    private final FileService fileService;
    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/chat")
    public void processMessage(@Payload MessageDto dto) {
        ChatMessage message = new ChatMessage();
        message.setSender(dto.getSender());
        message.setReceiver(dto.getReceiver());
        message.setContent(dto.getContent());
        message.setTimestamp(LocalDateTime.now());

        if (dto.getFileId() != null) {
            FileEntity file = fileService.getFileById(dto.getAttachmentId());
            message.setFiles(List.of(file));
        }

        chatMessageRepository.save(message);

        messagingTemplate.convertAndSendToUser(
                dto.getReceiver(),
                "/queue/messages",
                chatService.toDto(message)
        );
    }

    @MessageMapping("/connect")
    public void connectUser(String username) {
        onlineUserService.userOnline(username);
        simpMessagingTemplate.convertAndSend("/topic/public",
                Map.of("username", username, "type", "connect"));
        
        simpMessagingTemplate.convertAndSendToUser(username, "/queue/online-users",
                Map.of("type", "online-list", "users", onlineUserService.getOnlineUsers()));
    }

    @Scheduled(fixedRate = 10000)
    public void checkInactiveUsers() {
        LocalDateTime now = LocalDateTime.now();
        onlineUserService.getOnlineUsers().forEach((username, lastPing) -> {
            if (lastPing.isBefore(now.minusSeconds(30))) {
                onlineUserService.userOffline(username);
                simpMessagingTemplate.convertAndSend("/topic/public",
                        Map.of("username", username, "type", "disconnect"));
            }
        });
    }

    @GetMapping("/messages/{withUser}")
    public ResponseEntity<List<MessageDto>> getMessageHistory(
            @PathVariable String withUser,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String currentUsername = userDetails.getUsername();
        List<ChatMessage> messages = chatService.getChatHistory(currentUsername, withUser);

        List<MessageDto> dtos = messages.stream()
                .map(chatService::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }
}
