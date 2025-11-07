package com.bakeev.website.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String real;
    private String email;
    private Role role;
    private LocalDateTime lastSeen;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserDto [id=")
                .append(id)
                .append(", username=")
                .append(username)
                .append(", password=")
                .append(password)
                .append(", real=")
                .append(real)
                .append(", email=")
                .append(email)
                .append(", role=")
                .append(role)
                .append("]");
        return builder.toString();
    }

}
