// src/main/java/com/libraryservice/admin/AdminController.java
package org.example.libraryservice.admin;

import org.example.libraryservice.book.Book;
import org.example.libraryservice.book.BookService;
import org.example.libraryservice.dto.BookDto;
import org.example.libraryservice.dto.UserDetailsDto;
import org.example.libraryservice.fine.FineService;
import org.example.libraryservice.notification.NotificationService;
import org.example.libraryservice.user.User;
import org.example.libraryservice.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Secures all methods in this controller
public class AdminController {

    private final BookService bookService;
    private final UserService userService;
    private final FineService fineService;
    private final NotificationService notificationService;

    @PostMapping("/books")
    public ResponseEntity<Book> addBook(@RequestBody BookDto bookDto) {
        Book newBook = bookService.addBook(bookDto);
        notificationService.sendToAll("book-added", newBook); // Broadcast
        return ResponseEntity.ok(newBook);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody BookDto bookDetails) {
        Book updatedBook = bookService.updateBook(id, bookDetails);
        notificationService.sendToAll("book-updated", updatedBook); // Broadcast
        return ResponseEntity.ok(updatedBook);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDetailsDto>> getAllUsers() {
        // Use DTO to avoid sending password hash
        List<UserDetailsDto> userDtos = userService.findAllUsers().stream()
                .map(UserDetailsDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/defaulters")
    public ResponseEntity<List<Map<String, Object>>> getDefaulters() {
        return ResponseEntity.ok(fineService.getDefaulters());
    }
}