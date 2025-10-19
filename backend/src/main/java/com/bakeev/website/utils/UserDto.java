package com.bakeev.website.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String real;
    private String email;
    private Role role;
}
