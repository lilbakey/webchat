package com.bakeev.website.service;

import com.bakeev.website.entity.User;
import com.bakeev.website.repository.UserRepository;
import com.bakeev.website.utils.UserDto;
import com.bakeev.website.utils.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    @Value("${file.photo-dir}")
    private String photoDir;

    public User save(User user) {
        return userRepository.save(user);
    }

    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с таким email уже существует");
        }
        return save(user);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден!"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updateUser) {
        User user = getUserById(id);
        user.setUsername(updateUser.getUsername());
        user.setReal(updateUser.getReal());
        user.setEmail(updateUser.getEmail());
        user.setRole(updateUser.getRole());
        user.setLastSeen(updateUser.getLastSeen());

        return userRepository.save(user);
    }

    public User updateUserFromDto(Long id, UserDto userDto) {
        User user = getUserById(id);
        UserMapper.updateUserFromDto(userDto, user);
        return userRepository.save(user);
    }

    public void deletePhoto(String filename) {
        User user = getCurrentUser();
        String photo = String.format("%s/%s", photoDir, filename);
        if (!photo.isBlank()) {
            Path photoPath = Paths.get(photo).normalize();
            try {
                Files.deleteIfExists(photoPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            user.setPhoto(null);
            userRepository.save(user);
        }
    }

    public LocalDateTime getLastSeen(String username) {
        User user = getByUsername(username);
        return user.getLastSeen();
    }

    public void updateLastSeen(String username) {
        userRepository.updateLastSeen(username, LocalDateTime.now());
    }

    public void updateEncodedPassword(Long id, String password) {
        userRepository.updateEncodedPassword(id, password);
    }

}
