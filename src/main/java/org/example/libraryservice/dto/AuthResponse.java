// src/main/java/com/libraryservice/dto/AuthResponse.java
package org.example.libraryservice.dto;

import org.example.libraryservice.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDetailsDto user;
}