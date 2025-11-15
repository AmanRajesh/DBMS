// src/main/java/com/libraryservice/dto/UserDetailsDto.java
package org.example.libraryservice.dto;

import org.example.libraryservice.user.User;
import lombok.Data;

// A DTO to safely expose user details, excluding password
@Data
public class UserDetailsDto {
    private Long id;
    private String email;
    private String name;
    private String role;

    public UserDetailsDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
    }
}