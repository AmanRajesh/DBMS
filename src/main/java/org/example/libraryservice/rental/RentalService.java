// src/main/java/com/libraryservice/rental/RentalService.java
package org.example.libraryservice.rental;

import org.example.libraryservice.book.Book;
import org.example.libraryservice.book.BookRepository;
import org.example.libraryservice.fine.FineService;
import org.example.libraryservice.notification.NotificationService;
import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository; // Postgres
    private final BookRepository bookRepository;     // Mongo
    private final FineService fineService;           // Postgres
    private final NotificationService notificationService;

    @Transactional
    public Rental issueBook(String bookId, User user) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book not available");
        }

        // 1. Update Mongo
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // 2. Create Postgres record
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBookId(book.getId());
        rental.setRentalDate(Instant.now());
        rental.setDueDate(LocalDate.now().plus(14, ChronoUnit.DAYS));
        rental.setStatus("issued");

        // 3. Denormalize data
        rental.setBookTitle(book.getTitle());
        rental.setBookAuthors(book.getAuthors());
        rental.setBookGenre(book.getGenre());

        Rental savedRental = rentalRepository.save(rental);

        notificationService.sendToUser(user, "rental-issued", savedRental);
        return savedRental;
    }

    @Transactional
    public Rental returnBook(Long rentalId, User user) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (!rental.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User not authorized to return this rental");
        }

        if (!rental.getStatus().equals("issued")) {
            throw new RuntimeException("Book has already been returned or processed");
        }

        Book book = bookRepository.findById(rental.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // 1. Update Mongo
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        // 2. Update Postgres record
        rental.setReturnDate(Instant.now());
        LocalDate today = LocalDate.now();

        if (rental.getDueDate().isBefore(today)) {
            rental.setStatus("overdue");
            // Create a fine
            long daysOverdue = ChronoUnit.DAYS.between(rental.getDueDate(), today);
            double fineAmount = daysOverdue * 10.0; // $10 per day, from original code
            fineService.createFine(user, rental, fineAmount);
        } else {
            rental.setStatus("returned");
        }

        Rental updatedRental = rentalRepository.save(rental);

        notificationService.sendToUser(user, "rental-returned", updatedRental);
        return updatedRental;
    }

    public List<Rental> getUserRentalHistory(Long userId) {
        return rentalRepository.findByUserIdOrderByRentalDateDesc(userId);
    }
}