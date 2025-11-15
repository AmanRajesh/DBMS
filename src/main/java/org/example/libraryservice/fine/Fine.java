// src/main/java/com/libraryservice/fine/Fine.java
package org.example.libraryservice.fine;

import org.example.libraryservice.rental.Rental;
import org.example.libraryservice.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "fines")
@Data
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    private double amount;
    private String status; // "pending", "paid"
    private Instant fineDate;
    private Instant paymentDate;
}