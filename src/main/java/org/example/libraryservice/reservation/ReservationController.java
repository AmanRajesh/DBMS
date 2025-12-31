package org.example.libraryservice.reservation;

import org.example.libraryservice.dto.RentalRequest;
import org.example.libraryservice.dto.ReservationDto; // ✅ Import DTO
import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // ✅ KEPT EXACTLY AS IS (Preserves your POST body logic)
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody RentalRequest request, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(reservationService.createReservation(request.getBookId(), user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ KEPT EXACTLY AS IS
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(reservationService.cancelReservation(reservationId, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 🔄 UPDATED THIS ONE ONLY
    // We changed List<Reservation> -> List<ReservationDto> to fix "Unknown Title"
    @GetMapping("/user/active")
    public ResponseEntity<List<ReservationDto>> getActiveReservations(@AuthenticationPrincipal User user) {
        // We call 'getUserReservations' because that's the method we created
        // in the Service that does the SQL+Mongo bridge to get the titles.
        return ResponseEntity.ok(reservationService.getUserReservations(user.getId()));
    }

    // ✅ KEPT EXACTLY AS IS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/active")
    public ResponseEntity<List<Reservation>> getAllActiveReservations() {
        return ResponseEntity.ok(reservationService.getAllActiveReservations());
    }
}