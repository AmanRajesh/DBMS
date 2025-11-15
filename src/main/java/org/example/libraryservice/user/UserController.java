// src/main/java/com/libraryservice/user/UserController.java
package org.example.libraryservice.user;

import org.example.libraryservice.dto.AuthRequest;
import org.example.libraryservice.dto.AuthResponse;
import org.example.libraryservice.dto.UserDetailsDto;
import org.example.libraryservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final org.example.libraryservice.user.UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest authRequest) {
        try {
            User user = userService.register(authRequest);
            String token = tokenProvider.generateTokenFromUser(user);
            return ResponseEntity.ok(new AuthResponse(token, new UserDetailsDto(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new AuthResponse(token, new UserDetailsDto(user)));
    }
}