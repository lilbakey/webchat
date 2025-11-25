package com.bakeev.website.controller;

import com.bakeev.website.entity.ChatMessage;
import com.bakeev.website.entity.FileEntity;
import com.bakeev.website.repository.ChatMessageRepository;
import com.bakeev.website.repository.FileRepository;
import com.bakeev.website.service.ChatService;
import com.bakeev.website.service.FileService;
import com.bakeev.website.service.OnlineUserService;
import com.bakeev.website.service.UserService;
import com.bakeev.website.utils.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;
    private final FileService fileService;
    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final FileRepository fileRepository;


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

        ChatMessage saved = chatMessageRepository.save(message);

        simpMessagingTemplate.convertAndSendToUser(
                dto.getReceiver(),
                "/queue/messages",
                chatService.toDto(saved)
        );

        simpMessagingTemplate.convertAndSendToUser(
                dto.getSender(),
                "/queue/messages",
                chatService.toDto(saved)
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
            if (lastPing.isBefore(now.minusSeconds(10))) {
                onlineUserService.userOffline(username);
                simpMessagingTemplate.convertAndSend("/topic/public",
                        Map.of("username", username, "type", "disconnect"));
            }
        });
    }

    @MessageMapping("/update-ping")
    public void updatePing(String username) {
        onlineUserService.updatePing(username);
        userService.updateLastSeen(username);
        simpMessagingTemplate.convertAndSend("/topic/public", Map.of("username", username,
                "type", "time", "time", userService.getLastSeen(username)));
    }

    @MessageMapping("/typing")
    public void typing(@Payload Map<String, Object> payload, Principal principal) {
        String sender = principal.getName();
        String receiver = (String) payload.get("receiver");
        boolean isTyping = (Boolean) payload.get("typing");
        simpMessagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/typing",
                Map.of("username", sender, "typing", isTyping)
        );
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

    @DeleteMapping("/messages/{id}")
    @Transactional
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        ChatMessage msg = chatMessageRepository.findById(id).orElseThrow();
        List<FileEntity> files = new ArrayList<>(msg.getFiles());
        msg.getFiles().clear();
        chatService.deleteMessage(id);

        for (FileEntity file : files) {
            long count = chatMessageRepository.countByFiles_Id(file.getId());
            if (count == 0) {
                deletePhysicalFile(file.getPath());
                fileRepository.delete(file);
            }
        }

        simpMessagingTemplate.convertAndSendToUser(
                msg.getReceiver(),
                "/queue/delete-message",
                Map.of("id", id)
        );

        simpMessagingTemplate.convertAndSendToUser(
                msg.getSender(),
                "/queue/delete-message",
                Map.of("id", id)
        );

        return ResponseEntity.noContent().build();
    }

    private void deletePhysicalFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("Failed to delete file: " + path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }

}
