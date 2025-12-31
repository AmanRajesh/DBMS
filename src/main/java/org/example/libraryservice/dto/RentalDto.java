package org.example.libraryservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RentalDto {
    // --- Data from SQL (PostgreSQL) ---
    private Long id;              // The Rental ID
    private String bookId;        // The link to MongoDB
    private LocalDate rentalDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fine;
    private String status;        // e.g., "issued", "returned"

    // --- Data from NoSQL (MongoDB) ---
    // These fields will be filled manually by the Service
    private String bookTitle;
    private String bookAuthors;
    private String imageUrl;
}