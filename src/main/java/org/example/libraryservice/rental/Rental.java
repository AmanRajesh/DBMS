// src/main/java/com/libraryservice/rental/Rental.java
package org.example.libraryservice.rental;

import org.example.libraryservice.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "rentals")
@Data
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String bookId; // MongoDB Book ID

    private Instant rentalDate;
    private LocalDate dueDate;
    private Instant returnDate;

    // "issued", "returned", "overdue"
    private String status;

    // --- Denormalized data for Analytics ---
    private String bookTitle;
    private String bookAuthors;
    private String bookGenre;
}