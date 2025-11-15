// src/main/java/com/libraryservice/dto/BookDto.java
package org.example.libraryservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookDto {
    private String isbn;
    private String title;
    private String authors;
    private String genre;
    private String publisher;
    private int totalCopies;
    private String description;
    private List<String> tags;

    private int availableCopies;
}