package com.bakeev.website.service;

import com.bakeev.website.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
@RequiredArgsConstructor
public class OnlineUserService {

    private final Map<String, LocalDateTime> onlineUsers = new ConcurrentHashMap<>();

    private final UserService userService;

    public void userOnline(String username) {
        onlineUsers.put(username, userService.getLastSeen(username));
    }

    public void updatePing(String username) {
        onlineUsers.put(username, userService.getLastSeen(username));
    }

    public void userOffline(String username) {
        onlineUsers.remove(username);
    }
}
