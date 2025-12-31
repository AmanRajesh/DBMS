package org.example.libraryservice.rental;

import org.example.libraryservice.book.Book;
import org.example.libraryservice.book.BookRepository;
import org.example.libraryservice.dto.RentalDto;
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
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository; // SQL
    private final BookRepository bookRepository;     // Mongo
    private final FineService fineService;           // SQL
    private final NotificationService notificationService;
    private final ReservationRepository reservationRepository; // SQL

    @Transactional
    public Rental issueBook(String bookId, User user) {

        // 1. Check Availability in MongoDB
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book not available");
        }

        // 2. Update Stock in MongoDB
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // 3. Create Rental Record in SQL
        // (We ONLY store the bookId here, no titles/authors needed in SQL)
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBookId(book.getId()); // Store the Link
        rental.setRentalDate(Instant.now());
        rental.setDueDate(LocalDate.now().plus(14, ChronoUnit.DAYS));
        rental.setStatus("issued");

        Rental savedRental = rentalRepository.save(rental);

        // 4. Close any Active Reservation for this user/book
        List<Reservation> reservations = reservationRepository
                .findByUserIdAndStatusOrderByReservationDateDesc(user.getId(), "active");

        for (Reservation res : reservations) {
            // Trim IDs to be safe
            if (res.getBookId().trim().equals(bookId.trim())) {
                res.setStatus("fulfilled");
                reservationRepository.save(res);
                break; // Only close one reservation
            }
        }

        // 5. Send Notification
        try {
            notificationService.sendToUser(user, "rental-issued", savedRental);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Notification failed: " + e.getMessage());
        }

        return savedRental;
    }

    @Transactional
    public Rental requestBook(String bookId, User user) {
        // 1. Check Availability in MongoDB
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book not available");
        }

        // 2. Decrement Stock (MongoDB)
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // 3. Create Rental Record (SQL)
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setBookId(book.getId());
        rental.setRentalDate(Instant.now());
        // Default: 14 days to return
        rental.setDueDate(LocalDate.now().plus(14, ChronoUnit.DAYS));


        rental.setStatus("issued");

        Rental savedRental = rentalRepository.save(rental);

        // 4. Close any existing reservations for this book
        List<Reservation> reservations = reservationRepository
                .findByUserIdAndStatusOrderByReservationDateDesc(user.getId(), "active");

        for (Reservation res : reservations) {
            if (res.getBookId().trim().equals(bookId.trim())) {
                res.setStatus("fulfilled");
                reservationRepository.save(res);
                break;
            }
        }

        // 5. Notify
        try {
            notificationService.sendToUser(user, "rental-requested", savedRental);
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }

        return savedRental;
    }

    @Transactional
    public Rental returnBook(Long rentalId, User user) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (!rental.getStatus().equals("issued") && !rental.getStatus().equals("overdue")) {
            throw new RuntimeException("Book is not currently issued");
        }

        // 1. Get Book from Mongo to update stock
        Book book = bookRepository.findById(rental.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found in database"));

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        // 2. Process Return
        LocalDate today = LocalDate.now();
        rental.setReturnDate(Instant.now());

        // 3. Calculate Fines
        if (rental.getDueDate().isBefore(today)) {
            long daysOverdue = ChronoUnit.DAYS.between(rental.getDueDate(), today);
            if (daysOverdue > 0) {
                double fineAmount = daysOverdue * 5.0; // $5 per day
                fineService.createFine(rental.getUser(), rental, fineAmount);
            }
        }

        rental.setStatus("returned");
        Rental updatedRental = rentalRepository.save(rental);

        try {
            notificationService.sendToUser(rental.getUser(), "rental-returned", updatedRental);
        } catch (Exception e) {
            // Ignore
        }

        return updatedRental;
    }

    public List<Rental> getAllIssuedBooks() {
        return rentalRepository.findByStatus("issued");
    }

    // ⭐️ The Bridge: Fetch SQL History -> Populate with Mongo Details
    public List<RentalDto> getUserRentalHistory(Long userId) {
        List<Rental> rentals = rentalRepository.findByUserId(userId);

        return rentals.stream()
                .map(rental -> {
                    RentalDto dto = new RentalDto();
                    dto.setId(rental.getId());
                    dto.setBookId(rental.getBookId());
                    dto.setStatus(rental.getStatus());

                    // Fix dates
                    if (rental.getRentalDate() != null) dto.setRentalDate(java.time.LocalDate.ofInstant(rental.getRentalDate(), java.time.ZoneId.systemDefault()));
                    dto.setDueDate(rental.getDueDate());


                    System.out.println("DEBUG: Processing Rental ID: " + rental.getId());
                    System.out.println("DEBUG: Searching Mongo for Book ID: '" + rental.getBookId() + "'");

                    Book book = bookRepository.findById(rental.getBookId()).orElse(null);

                    if (book != null) {
                        System.out.println("DEBUG: FOUND! Title: " + book.getTitle());
                        dto.setBookTitle(book.getTitle());
                        dto.setBookAuthors(book.getAuthors());
                        dto.setImageUrl(book.getImageUrl());
                    } else {
                        System.out.println("DEBUG: NOT FOUND in MongoDB.");
                        dto.setBookTitle("Unknown Book (ID: " + rental.getBookId() + ")");
                        dto.setBookAuthors("Unknown Author");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private RentalDto convertToDto(Rental rental) {
        RentalDto dto = new RentalDto();

        // --- SQL Data ---
        dto.setId(rental.getId());
        dto.setBookId(rental.getBookId());

        // Fix: Convert Instant (SQL) to LocalDate (DTO)
        if (rental.getRentalDate() != null) {
            dto.setRentalDate(LocalDate.ofInstant(rental.getRentalDate(), ZoneId.systemDefault()));
        }
        dto.setDueDate(rental.getDueDate());
        if (rental.getReturnDate() != null) {
            dto.setReturnDate(LocalDate.ofInstant(rental.getReturnDate(), ZoneId.systemDefault()));
        }

        dto.setStatus(rental.getStatus());

        // --- Mongo Data (The Bridge) ---
        // Fetch book details using the ID
        Book book = bookRepository.findById(rental.getBookId()).orElse(null);

        if (book != null) {
            dto.setBookTitle(book.getTitle());
            dto.setBookAuthors(book.getAuthors());
            dto.setImageUrl(book.getImageUrl());
        } else {
            dto.setBookTitle("Unknown Book (Removed)");
            dto.setBookAuthors("Unknown");
        }

        return dto;
    }
}