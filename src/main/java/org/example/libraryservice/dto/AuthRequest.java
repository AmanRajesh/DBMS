// src/main/java/com/libraryservice/dto/AuthRequest.java
package org.example.libraryservice.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private String name; // Used for registration
    private String role; // Used for registration, optional
}