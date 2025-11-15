// src/main/java/com/libraryservice/book/BookController.java
package org.example.libraryservice.book;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<?> getAllBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String genre) {

        Page<Book> bookPage = bookService.getAllBooks(genre, page, limit);

        // Create a custom response to match the original API's pagination structure
        Map<String, Object> pagination = Map.of(
                "current_page", bookPage.getNumber() + 1,
                "total_pages", bookPage.getTotalPages(),
                "total_books", bookPage.getTotalElements()
        );

        Map<String, Object> response = Map.of(
                "books", bookPage.getContent(),
                "pagination", pagination
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable String id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Maps to routes/search.js
    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(@RequestParam String query) {
        List<Book> results = bookService.searchBooks(query);
        // Match original API response
        return ResponseEntity.ok(Map.of(
                "results", results,
                "count", results.size()
        ));
    }
}