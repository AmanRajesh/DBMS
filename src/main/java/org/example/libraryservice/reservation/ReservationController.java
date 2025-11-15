// src/main/java/com/libraryservice/reservation/ReservationController.java
package org.example.libraryservice.reservation;

import org.example.libraryservice.dto.RentalRequest;
import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // The original API used POST / with { book_id }
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody RentalRequest request, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(reservationService.createReservation(request.getBookId(), user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(reservationService.cancelReservation(reservationId, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/active")
    public ResponseEntity<List<Reservation>> getActiveReservations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reservationService.getActiveUserReservations(user.getId()));
    }
}