package org.example.libraryservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationDto {
    // --- Data from SQL (PostgreSQL) ---
    private Long id;              // The Reservation ID
    private String bookId;        // The link to MongoDB
    private LocalDate reservationDate;
    private LocalDate pickupDeadline;
    private String status;        // e.g., "active", "cancelled"

    // --- Data from NoSQL (MongoDB) ---
    // These fields will be filled manually by the Service
    private String bookTitle;
    private String bookAuthors;
    private String imageUrl;
}