package org.example.libraryservice.reservation;

import lombok.RequiredArgsConstructor;
import org.example.libraryservice.book.Book;
import org.example.libraryservice.book.BookRepository;
import org.example.libraryservice.dto.ReservationDto;
import org.example.libraryservice.notification.NotificationService;
import org.example.libraryservice.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository; // SQL
    private final NotificationService notificationService;
    private final BookRepository bookRepository;             // MongoDB (Added this!)

    @Transactional
    public Reservation createReservation(String bookId, User user) {
        Reservation res = new Reservation();
        res.setUser(user);
        res.setBookId(bookId);
        res.setReservationDate(Instant.now());
        res.setPickupDeadline(LocalDate.now().plus(3, ChronoUnit.DAYS)); // 3 day deadline
        res.setStatus("active");

        Reservation savedRes = reservationRepository.save(res);
        notificationService.sendToUser(user, "reservation-created", savedRes);
        return savedRes;
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, User user) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!res.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User not authorized to cancel this reservation");
        }

        res.setStatus("cancelled");
        Reservation savedRes = reservationRepository.save(res);
        notificationService.sendToUser(user, "reservation-cancelled", savedRes);
        return savedRes;
    }

    public List<Reservation> getActiveUserReservations(Long userId) {
        return reservationRepository.findByUserIdAndStatusOrderByReservationDateDesc(userId, "active");
    }

    public List<Reservation> getAllActiveReservations() {
        return reservationRepository.findByStatusOrderByReservationDateAsc("active");
    }

    // ⭐️ This is the BRIDGE method that merges SQL + MongoDB data
    public List<ReservationDto> getUserReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);

        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper to merge data
    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();

        // --- Fill SQL Data ---
        dto.setId(reservation.getId());
        dto.setBookId(reservation.getBookId());
        // Handle conversion from Instant to LocalDate if your DTO expects LocalDate
        // (Assuming reservationDate is Instant in Entity and LocalDate in DTO, or similar)
        // If strict types differ, use .atZone(ZoneId.systemDefault()).toLocalDate()
        // For simplicity here, assuming types match or simple string conversion:
        dto.setReservationDate(LocalDate.ofInstant(reservation.getReservationDate(), java.time.ZoneId.systemDefault()));

        dto.setPickupDeadline(reservation.getPickupDeadline());
        dto.setStatus(reservation.getStatus());

        // --- Fill MongoDB Data ---
        // Uses the injected bookRepository to find the name
        Book book = bookRepository.findById(reservation.getBookId()).orElse(null);

        if (book != null) {
            dto.setBookTitle(book.getTitle());
            dto.setBookAuthors(book.getAuthors());
            dto.setImageUrl(book.getImageUrl());
        } else {
            dto.setBookTitle("Unknown Book");
            dto.setBookAuthors("Unknown Author");
        }

        return dto;
    }
}