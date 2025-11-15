// src/main/java/com/libraryservice/user/UserService.java
package org.example.libraryservice.user;

import org.example.libraryservice.dto.AuthRequest;
import lombok.RequiredArgsConstructor;
import org.example.libraryservice.user.User;
import org.example.libraryservice.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Ensure role starts with "ROLE_"
        String role = (request.getRole() != null ? request.getRole() : "STUDENT").toUpperCase();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        user.setRole(role);

        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}