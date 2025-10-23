package com.bakeev.website.utils;

import com.bakeev.website.entity.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setReal(user.getReal());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setLastSeen(user.getLastSeen());
        return dto;
    }

    public static void updateUserFromDto(UserDto dto, User user) {
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setReal(dto.getReal());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setLastSeen(dto.getLastSeen());
    }
}
