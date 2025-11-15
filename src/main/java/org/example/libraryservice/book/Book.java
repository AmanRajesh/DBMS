// src/main/java/com/libraryservice/book/Book.java
package org.example.libraryservice.book;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Document(collection = "books")
@Data
public class Book {
    @Id
    private String id;
    private String isbn;
    private String title;
    private String authors;
    private String genre;
    private String publisher;
    private int totalCopies;
    private int availableCopies;
    private String description;
    private List<String> tags;
    private boolean isAvailable = true;
    private Instant createdAt = Instant.now();
}