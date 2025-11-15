// src/main/java/com/libraryservice/reservation/ReservationService.java
package org.example.libraryservice.reservation;

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
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    // You might also inject BookRepository to check if a book is reservable

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
        // We can't easily join with Mongo to get book title,
        // so the frontend will need to fetch book details separately.
        return reservationRepository.findByUserIdAndStatusOrderByReservationDateDesc(userId, "active");
    }
}