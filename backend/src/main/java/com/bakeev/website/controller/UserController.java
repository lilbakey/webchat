package com.bakeev.website.controller;

import com.bakeev.website.config.SecurityConfig;
import com.bakeev.website.entity.User;
import com.bakeev.website.jwt.JwtAuthenticationResponse;
import com.bakeev.website.service.AuthenticationService;
import com.bakeev.website.service.JwtService;
import com.bakeev.website.service.UserService;
import com.bakeev.website.utils.UserDto;
import com.bakeev.website.utils.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @Value("${file.photo-dir}")
    private String photoDir;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/id/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/username/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userService.getByUsername(username);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        userService.updateUserFromDto(id, userDto);
        userService.updateEncodedPassword(id, passwordEncoder.encode(userDto.getReal()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<String> uploadPhoto(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.getUserById(id);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(photoDir).resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        user.setPhoto("/api/users/photo/" + filename);
        userService.save(user);

        return ResponseEntity.ok(user.getPhoto());
    }

    @GetMapping("/photo/{filename:.+}")
    public ResponseEntity<UrlResource> getPhoto(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(photoDir).resolve(filename).normalize().toAbsolutePath();
        UrlResource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) return ResponseEntity.notFound().build();

        String mimeType = Files.probeContentType(filePath);
        MediaType mediaType = mimeType != null ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }


    @DeleteMapping("/photo/{filename}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String filename) {
        userService.deletePhoto(filename);
        return ResponseEntity.noContent().build();
    }
}
