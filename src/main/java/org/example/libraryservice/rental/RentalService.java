// src/main/java/com/libraryservice/rental/RentalService.java
package org.example.libraryservice.rental;

import org.example.libraryservice.book.Book;
import org.example.libraryservice.book.BookRepository;
import org.example.libraryservice.fine.FineService;
import org.example.libraryservice.notification.NotificationService;
import org.example.libraryservice.reservation.Reservation;
import org.example.libraryservice.reservation.ReservationRepository;
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
    private final org.example.libraryservice.reservation.ReservationRepository reservationRepository;

    @Transactional
    public Rental requestBook(String bookId, User user) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book not available");
        }

        // Update Mongo
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Create Postgres record
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBookId(book.getId());
        rental.setRentalDate(Instant.now());
        rental.setDueDate(LocalDate.now().plus(14, ChronoUnit.DAYS));

        // 2. IMPORTANT: Status must be 'issued' (not 'requested')
        rental.setStatus("issued");

        // Denormalize data
        rental.setBookTitle(book.getTitle());
        rental.setBookAuthors(book.getAuthors());
        rental.setBookGenre(book.getGenre());

        Rental savedRental = rentalRepository.save(rental);

        // 3. Your Reservation Closing Logic (This is perfect!)
        List<org.example.libraryservice.reservation.Reservation> reservations =
                reservationRepository.findByUserIdAndStatusOrderByReservationDateDesc(user.getId(), "active");

        for (org.example.libraryservice.reservation.Reservation res : reservations) {
            if (res.getBookId().equals(bookId)) {
                res.setStatus("fulfilled"); // Mark as done
                reservationRepository.save(res);
                break;
            }
        }

        notificationService.sendToUser(user, "rental-issued", savedRental);
        return savedRental;
    }

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

        // 4. Close the Reservation (Clean Version)
        List<Reservation> reservations = reservationRepository
                .findByUserIdAndStatusOrderByReservationDateDesc(user.getId(), "active");

        for (Reservation res : reservations) {
            // Check IDs safely (trim to ensure no whitespace issues)
            if (res.getBookId().trim().equals(bookId.trim())) {
                res.setStatus("fulfilled");
                reservationRepository.save(res);
                break; // Close only one reservation per issued book
            }
        }

        // 5. Send Notification (Safe Mode)
        try {
            notificationService.sendToUser(user, "rental-issued", savedRental);
        } catch (Exception e) {
            System.err.println("WARNING: Notification failed, but book issued successfully: " + e.getMessage());
        }

        return savedRental;

    }

    @Transactional
    public Rental returnBook(Long rentalId, User user) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (!rental.getStatus().equals("issued") && !rental.getStatus().equals("overdue")) {
            throw new RuntimeException("Book has already been returned or processed");
        }

        Book book = bookRepository.findById(rental.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // 1. Stock Update
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        // 2. Set Return Date
        rental.setReturnDate(Instant.now());
        LocalDate today = LocalDate.now();

        // 3. Check for Fines
        if (rental.getDueDate().isBefore(today)) {
            // It IS late, so we create a fine...
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(rental.getDueDate(), today);
            double fineAmount = daysOverdue * 5.0; // $5 per day

            fineService.createFine(rental.getUser(), rental, fineAmount);

            // ...BUT we still mark the rental as 'returned' because the book is back!
            rental.setStatus("returned");
        } else {
            // On time
            rental.setStatus("returned");
        }

        // 4. Save Rental & Notify
        Rental updatedRental = rentalRepository.save(rental);
        try {
            notificationService.sendToUser(rental.getUser(), "rental-returned", updatedRental);
        } catch (Exception e) {
            // ignore notification errors
        }

        return updatedRental;
    }

    public List<Rental> getUserRentalHistory(Long userId) {
        return rentalRepository.findByUserIdOrderByRentalDateDesc(userId);
    }
    public List<Rental> getAllIssuedBooks() {
        return rentalRepository.findByStatus("issued");
    }
}