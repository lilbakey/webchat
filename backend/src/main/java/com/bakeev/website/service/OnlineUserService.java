package com.bakeev.website.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
public class OnlineUserService {

    private final Map<String, LocalDateTime> onlineUsers = new ConcurrentHashMap<>();

    public void userOnline(String username) {
        onlineUsers.put(username, LocalDateTime.now());
    }

    public void updatePing(String username){
        onlineUsers.put(username, LocalDateTime.now());
    }

    public void userOffline(String username) {
        onlineUsers.remove(username);
    }
}
