// src/main/java/com/libraryservice/reservation/Reservation.java
package org.example.libraryservice.reservation;

import org.example.libraryservice.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String bookId; // MongoDB Book ID

    private Instant reservationDate;
    private LocalDate pickupDeadline;
    private String status; // "active", "cancelled", "fulfilled"
}